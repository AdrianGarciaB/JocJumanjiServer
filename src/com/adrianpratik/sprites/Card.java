package com.adrianpratik.sprites;

import javafx.scene.image.Image;

import java.io.Serializable;

public class Card implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final int cardWidthSize = 0;
    public static final int cardHeightSize = 0;
    public static final Image cardFlipped = null;
    public Type type;
    public enum Type{Club, Diamond, Heart, Spade}
    public int cardNumber;
    private double x, y;
    private Image sprite;
    private boolean flipped;
    private boolean hide;
    public int cardPosition;
    public boolean discarted;

    public Card(Type type, int cardNumber) {
        this.type = type;
        this.cardNumber = cardNumber;
    }

    public static Type getTypeById(int typeId){
        switch (typeId){
            case 1: return Type.Club;
            case 2: return Type.Diamond;
            case 3: return Type.Heart;
            case 4: return Type.Spade;
        }
        return null;
    }
}

