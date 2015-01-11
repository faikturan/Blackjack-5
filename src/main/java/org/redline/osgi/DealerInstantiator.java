package org.redline.osgi;

/**
 * Created by Andrey on 22.11.2014.
 */
public interface DealerInstantiator {
    boolean instantiate(String actorSytemName, String dealerID);
}
