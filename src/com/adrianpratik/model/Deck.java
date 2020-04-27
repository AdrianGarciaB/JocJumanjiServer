package com.adrianpratik.model;

import com.adrianpratik.sprites.Card;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    List<Card> mainDeck;
    Card lastCardDiscarted;

    public Deck(){
        mainDeck = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= 13; j++) {
                mainDeck.add(new Card(Card.getTypeById(i+1), j));
            }
        }
        lastCardDiscarted = getRandomCard();
    }
    public Card getRandomCard(){
        int cardId = (int) (Math.random()*(mainDeck.size()-1));

        Card randomCard = mainDeck.get(cardId);
        return randomCard;
    }

    public Card getLastCardDiscarted() {
        return lastCardDiscarted;
    }

    public void setLastCardDiscarted(Card lastCardDiscarted) {
        mainDeck.add(this.lastCardDiscarted);
        this.lastCardDiscarted = lastCardDiscarted;
    }
}
