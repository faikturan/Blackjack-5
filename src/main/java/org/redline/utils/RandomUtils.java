package org.redline.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by astanovskiy on 11/20/2014.
 */
public class RandomUtils {

    private static Random RND = new Random();

    public static Integer getRandomBet() {
        return (RND.nextInt(10) + 1) * 10; //producing 10, 20, 30, ..., 100
    }

    public static void shuffle(List<?> collection) {
        Collections.shuffle(collection, RND);
    }

    public static boolean getRandomBoolean(){
        return RND.nextBoolean();
    }
}
