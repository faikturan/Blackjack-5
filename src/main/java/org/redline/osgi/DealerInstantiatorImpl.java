package org.redline.osgi;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.actor.Props;
import org.redline.HazelcastManager;
import org.redline.actors.DealerActor;

/**
 * Created by Andrey on 22.11.2014.
 */
public class DealerInstantiatorImpl implements DealerInstantiator {
    @Override
    public boolean instantiate(String actorSytemName, String dealerID) {
        try {
            System.setProperty("config.resource", "application-dealer.conf");
            ActorSystem system = ExtendedActorSystem.create(actorSytemName);
            HazelcastManager.getInstance().addActorsSystem((ExtendedActorSystem) system);
            system.actorOf(Props.create(DealerActor.class), dealerID);
            return true;
        } catch (Exception e) {
            //oops
        }
        return false;
    }
}
