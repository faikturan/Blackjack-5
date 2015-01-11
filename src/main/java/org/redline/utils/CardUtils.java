package org.redline.utils;

import org.redline.gambling.Card;

import java.util.List;

/**
 * Created by Andrey on 20.11.2014.
 */
public class CardUtils {
    public static Integer calculateWeight(List<Card> cards){
        Integer result = 0;
        if (cards != null) {
            for (Card card : cards) {
                result += card.getWeight();
            }
        }
        return result;
    }
}
