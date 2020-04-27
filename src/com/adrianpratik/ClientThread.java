package com.adrianpratik;

import com.adrianpratik.model.CardListResponse;
import com.adrianpratik.model.Packet;
import com.adrianpratik.sprites.Card;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientThread extends Thread {
/* Thread que gestiona la comunicació de SrvTcPAdivina.java i un cllient ClientTcpAdivina.java */
	
	Socket clientSocket = null;
	ObjectInputStream in = null;
	ObjectOutputStream out = null;
	boolean continueConnected;
	int threadId;
	int playerNumber;
	long timeout;

	public ClientThread(Socket clientSocket, int playerNumber) throws IOException {
		this.playerNumber = playerNumber;
		this.clientSocket = clientSocket;
		continueConnected = true;
		// Primero se declara el output ya que si no, no funciona.
		// https://stackoverflow.com/questions/8377291/objectinputstreamsocket-getinputstream-does-not-work
		out= new ObjectOutputStream(clientSocket.getOutputStream());
		in = new ObjectInputStream(clientSocket.getInputStream());

	}

	@Override
	public void run() {
		Packet connectionResponse;
		System.out.println(playerNumber);
		timeout = System.currentTimeMillis() + 10000;
		// Paquete para decirle al cliente que se ha conectado correctamente.
		try {
			out.writeObject(new Packet(200, playerNumber));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Thread independiente para saber si el cliente sigue conectado.
		new Thread(() -> {
			while (continueConnected){
				try {
					out.writeObject(new Packet(100, null));
					out.flush();
					Thread.sleep(5000);
					if (System.currentTimeMillis() > timeout) {
						System.out.println("[TIMEOUT]");
						continueConnected = false;
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		try {
			while(continueConnected) {
				connectionResponse = (Packet) in.readObject();
				switch (connectionResponse.responseCode){
					case 100: {
						timeout = System.currentTimeMillis() + 10000;
						break;
					}
					case 104: {
						if (playerNumber != Server.playerTurnId){
							out.writeObject(new Packet(400, null));
						}
						else {
							out.writeObject(new Packet(104, Server.deck.getRandomCard()));
						}
						out.flush();
						break;
					}
					case 105: {
						passTurn();
					}
					case 106: {
						Card discartedCard = (Card) connectionResponse.data;
						Server.deck.setLastCardDiscarted(discartedCard);
						Server.notifyUsers(102, null);
						break;
					}
					case 107: {
						CardListResponse cardListResponse = new CardListResponse();
						for (int i = 0; i < 4; i++) cardListResponse.getCardList().add(Server.deck.getRandomCard());
						out.writeObject(new Packet(107, cardListResponse));
						out.flush();
						break;
					}
					case 108: {
						Server.notifyUsers(108, connectionResponse.data);
						break;
					}
					case 112: {
						Server.notifyUsers(112, null);
						break;
					}
				}
			}
			Server.clientList.remove(this);
			clientSocket.close();
		} catch (SocketException se){
			try {
				System.out.println("[DISCONECTED] " + playerNumber);
				continueConnected = false;
				if (Server.playerTurnId == playerNumber) passTurn();
				Server.notifyUsers(102, null);

				Server.clientList.remove(this.getId());

			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			continueConnected = false;
		}
	}

	private void passTurn() {
		do {
			System.out.println(Server.playerTurnId);
			if (Server.playerTurnId >= Server.numberOfPlayers) {
				Server.playerTurnId = 1;
			} else {
				Server.playerTurnId++;
			}
			System.out.println("Turno de " + playerNumber);
		}
		while ((Server.clientList.get(Server.playerTurnId) == null));
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClientThread that = (ClientThread) o;
		return threadId == that.threadId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(threadId);
	}

	public void startGame(){
		try {
			// Enviamos el paquete que indica que empieza el juego junto a 2 cartas aleatorias
			CardListResponse response = new CardListResponse();
			for (int i = 0; i < 4; i++) {
				response.getCardList().add(Server.deck.getRandomCard());
			}
			out.writeObject(new Packet(101, response));
			out.flush();

			out.writeObject(new Packet(102, Server.deck.getLastCardDiscarted()));
			out.flush();

			out.writeObject(new Packet(103, Server.playerTurnId));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void discardCardUpdate() {
		try {
			out.writeObject(new Packet(102, Server.deck.getLastCardDiscarted()));
			out.flush();

			out.writeObject(new Packet(103, Server.playerTurnId));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeCard(Object data) {
		try {
			int[] cardData = (int[]) data;

			out.writeObject(new Packet(108, cardData));
			out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void finishGame() {
		try {
			out.writeObject(new Packet(112, null));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
