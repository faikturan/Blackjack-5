package org.redline.gambling;

import org.redline.utils.RandomUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by astanovskiy on 11/19/2014.
 */
public class BlackJackDeck implements CardsDeck {

    private Queue<Card> cards;

    public BlackJackDeck() {
        LinkedList<Card> list = new LinkedList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            list.add(new Card(Card.Type.Ace, suit));
            for (int x = 0; x < 3; x++) {
                list.add(new Card(Card.Type.FaceCard, suit));
            }
            for (int weight = 2; weight <= 10; weight++) {
                list.add(new Card(Card.Type.Numeric, suit, weight));
            }
        }
        list.addAll(list); // duplicating...
        RandomUtils.shuffle(list);
        this.cards = list;
    }

    @Override
    public Card nextCard() {
        return this.cards.poll();
    }
}
