package org.redline.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redline.HazelcastManager;
import org.redline.gambling.Card;
import org.redline.utils.CardUtils;
import org.redline.utils.RandomUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.redline.messages.Messages.*;
/**
 * Created by astanovskiy on 11/19/2014.
 */
public class PlayerActor extends UntypedActor {
    private static final Logger log = LogManager.getLogger();

    private int money = 1000;
    private ActorSelection dealerActor;

    public PlayerActor(ActorSelection dealerActor) {
        this.dealerActor = dealerActor;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        log.debug("{}. Received a message: {}, from {}.", getSelf().path().name(), msg, getSender().path().name());
        if (msg instanceof String){
            switch ((String)msg) {
                case WIN: grabTheWin(); break;
                case TURN: makeTurn(); break;
                case MAKE_BET: makeBet(); break;
                default: unhandled(msg);
            }
        } else
            unhandled(msg);

    }

    private void grabTheWin() {
        this.money += HazelcastManager.getInstance().getBetsTable().get(getSelf());
        log.info("{}: Yay, I got now: {}$", getSelf().path().name(), money);
    }

    private void makeTurn() {
        List<Card> myCards = HazelcastManager.getInstance().getCardTable().get(getSelf());
        log.info("{}: Got the following cards: {}", getSelf().path().name(), myCards);
        Integer myHandWeight = CardUtils.calculateWeight(myCards);
        //TODO: add other players cards weight factor to make a decision
        if (myHandWeight < 16 || (myHandWeight < 21 && tryLuck())) {
            log.info("{}: HITing with hand weight equals to: {}", getSelf().path().name(), myHandWeight);
            this.dealerActor.tell(HIT, getSelf());
        } else if (myHandWeight > 21) {
            log.info("{}: Busted! =C", getSelf().path().name());
            this.dealerActor.tell(BUSTED, getSelf());
        } else {
            log.info("{}: STANDing with hand weight equals to: {}", getSelf().path().name(), myHandWeight);
            Queue<ActorRef> turns = HazelcastManager.getInstance().getTurnsQueue();
            turns.remove().tell(TURN, getSelf());
        }
    }

    private boolean tryLuck() {
        return RandomUtils.getRandomBoolean();
    }

    private void makeBet() {
        Map<ActorRef, Integer> map = HazelcastManager.getInstance().getBetsTable();
        if (money > 0) {
            int moneyToBet = money > 100 ? RandomUtils.getRandomBet() : money;
            log.info("{}: Betting {} from {}", getSelf().path().name(), moneyToBet, money);
            map.put(getSelf(), moneyToBet);
            money = money - moneyToBet;
            getSender().tell(BET_DONE, getSelf());
        } else {
            getSender().tell(QUIT, getSelf());
            log.info("{}: I've lost, quitting...", getSelf().path().name());
        }
    }

    @Override
    public void preStart() throws Exception {
        log.info("Starting...");
        this.dealerActor.tell(REGISTER, getSelf());
    }

    @Override
    public void postStop() throws Exception {
        log.info("Stopping...");
    }
}
