package com.adrianpratik.model;

import com.adrianpratik.sprites.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CardListResponse implements Serializable {
    public static final long serialVersionUID = 101L;
    List<Card> cardList;

    public CardListResponse(){
        cardList = new ArrayList<>();
    }

    public List<Card> getCardList() {
        return cardList;
    }
}
