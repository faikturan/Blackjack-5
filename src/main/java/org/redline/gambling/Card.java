package org.redline.gambling;

import java.io.Serializable;

/**
 * Created by astanovskiy on 11/19/2014.
 */
public class Card implements Serializable {
    public static enum Type {
        Ace(1),
        //using those two for simplicity
        FaceCard(10),
        Numeric(0) {
            @Override
            public int getDefaultWeight() {
                throw new UnsupportedOperationException("Numeric cards type has no default value");
            }
        };

        private final int defaultWeight;

        Type(int defaultWeight) {
            this.defaultWeight = defaultWeight;
        }

        public int getDefaultWeight() {
            return defaultWeight;
        }
    }

    public static enum Suit {
        Club, Diamond, Heart, Spade
    }

    private final Type type;
    private final Suit suit;
    private final int weight;

    public Card(Type type, Suit suit, int weight) {
        this.type = type;
        this.suit = suit;
        this.weight = weight;
    }

    public Card(Type type, Suit suit) {
        this(type, suit, type.getDefaultWeight());
    }

    public Type getType() {
        return type;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "" + getSuit() + " " + getType() + " (" + getWeight() + ")";
    }
}
