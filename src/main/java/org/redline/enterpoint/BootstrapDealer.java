package org.redline.enterpoint;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redline.HazelcastManager;
import org.redline.actors.DealerActor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by astanovskiy on 11/19/2014.
 */
public class BootstrapDealer {
    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) {
        log.info("Bootstrap was started. Going to start actors system");

        System.setProperty("config.resource", "application-dealer.conf");
        ActorSystem system = ExtendedActorSystem.create("BlackJack");
        HazelcastManager.getInstance().addActorsSystem((ExtendedActorSystem) system);
        ActorRef dealerActor = system.actorOf(Props.create(DealerActor.class), "dealerActor");
        dealerActor.tell(readConsole(), ActorRef.noSender());

        log.info("Exiting bootstrap");
    }

    private static String readConsole() {
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String result = "Stop";
        try {
            result = bufferRead.readLine();
        } catch (IOException e) {
            log.catching(e);
        }
        return result;
    }
}
