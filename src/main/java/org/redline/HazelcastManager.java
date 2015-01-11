package org.redline;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.serialization.JavaSerializer;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.redline.gambling.Card;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by astanovskiy on 11/20/2014.
 */
public final class HazelcastManager {
    private static final String CARDS_TABLE = "Table";
    private static final String TURNS = "Turns";
    private static final String BETS = "Bets";

    private static class Initializer {
        static HazelcastManager INSTANCE = new HazelcastManager();
    }

    public static HazelcastManager getInstance() {
        return Initializer.INSTANCE;
    }

    private final HazelcastInstance hazelcastInstance;
    public ExtendedActorSystem system;

    private HazelcastManager() {
        Config config = new Config();
        config.setProperty("hazelcast.logging.type", "slf4j");
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }

    public void addActorsSystem(ExtendedActorSystem system) {
        this.system = system;
    }

    public <K, V> Map<K, V> getMap(String mapName) {
        JavaSerializer.currentSystem().value_$eq(system);
        return hazelcastInstance.getMap(mapName);
    }

    public <E> Queue<E> getQueue(String s) {
        JavaSerializer.currentSystem().value_$eq(system);
        return hazelcastInstance.getQueue(s);
    }

    public Map<ActorRef, List<Card>> getCardTable(){
        return getMap(HazelcastManager.CARDS_TABLE);
    }

    public Map<ActorRef, Integer> getBetsTable(){
        return getMap(HazelcastManager.BETS);
    }

    public Queue<ActorRef> getTurnsQueue(){
        return getQueue(HazelcastManager.TURNS);
    }
}
