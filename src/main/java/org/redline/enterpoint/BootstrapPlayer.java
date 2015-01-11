package org.redline.enterpoint;

import akka.actor.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redline.HazelcastManager;
import org.redline.actors.PlayerActor;

import java.util.Random;

/**
 * Created by Andrey on 22.11.2014.
 */
public class BootstrapPlayer {
    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) {
        log.info("Bootstrap was started. Going to start actors system");

        System.setProperty("config.resource", "application-player.conf");
        ActorSystem system = ExtendedActorSystem.create("BlackJack");
        HazelcastManager.getInstance().addActorsSystem((ExtendedActorSystem)system);
        ActorSelection actorSelection = system.actorSelection("akka.tcp://BlackJack@127.0.0.1:8469/user/dealerActor");
        system.actorOf(Props.create(PlayerActor.class, actorSelection), "player"+ new Random().nextInt(1000));

        log.info("Exiting bootstrap");
    }
}
