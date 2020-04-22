package com.adrianpratik;

import com.adrianpratik.model.Deck;
import com.adrianpratik.sprites.Card;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {
/* Servidor TCP que genera un número perquè ClientTcpAdivina.java jugui a encertar-lo 
 * i on la comunicació dels diferents jugador passa per el Thread : ThreadServidorAdivina.java
 * */
	
	int port;
	public static HashMap<Long, ClientThread> clientList;
	public boolean gameStarted;
	public static Deck deck;
	public static int playerTurnId = 1;
	public boolean discardCardChanged;


	public Server(int port ) {
		this.port = port;
		clientList = new HashMap<>();
	}

	public static void notifyUsers(int code) {
		switch (code){
			case 102: clientList.forEach((aLong, clientThread) -> clientThread.discardCardUpdate()); break;
		}
	}

	public void listen() {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		int playerNumber = 1;
		try {
			serverSocket = new ServerSocket(port);
			// Thread para verificar que los clientes sigan conectados
			new Thread(() -> {
				while (true){
					try {
						if (clientList.size() == 2 && !gameStarted) {
							gameStarted = true;
							deck = new Deck();
							clientList.forEach((aLong, clientThread) -> clientThread.startGame());
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			while(true) { //esperar connexió del client i llançar thread
				clientSocket = serverSocket.accept();
				//Llançar Thread per establir la comunicació
				ClientThread clientInfo = new ClientThread(clientSocket, playerNumber);
				if (gameStarted) {
					clientInfo.clientSocket.close();
					System.out.println("[FULL] " + clientInfo.getName());
				}
				else {
					clientInfo.start();
					clientList.put(clientInfo.getId(), clientInfo);
					System.out.println("[NEW] " + clientInfo.getName());
					playerNumber++;
				}

			}
		} catch (IOException ex) {
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) {
		/*if (args.length != 1) {
            System.err.println("Usage: java SrvTcpAdivina <port number>");
            System.exit(1);
        }*/

        //int port = Integer.parseInt(args[0]);
        Server srv = new Server(20200);
        srv.listen();
	}

}
