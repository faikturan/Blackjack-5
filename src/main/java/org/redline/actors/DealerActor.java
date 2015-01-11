package org.redline.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redline.HazelcastManager;
import org.redline.gambling.BlackJackDeck;
import org.redline.gambling.Card;
import org.redline.gambling.CardsDeck;
import org.redline.utils.CardUtils;

import java.util.*;

import static org.redline.messages.Messages.*;
/**
 * Created by astanovskiy on 11/19/2014.
 */
public class DealerActor extends UntypedActor {
    private static final Logger log = LogManager.getLogger();
    private static final int DEALER_OUT_THRESHOLD = 100;
    private static final int DEALER_HIT_THRESHOLD = 17;

    private List<ActorRef> players = new ArrayList<>();
    private CardsDeck cardsDeck;
    private boolean isGameStarted;
    private Card holeCard;
    private int betCounter;
    private int money = 10000;

    @Override
    public void onReceive(Object msg) throws Exception {
        log.debug("Received a message: {}, from {}.", msg, getSender().path().name());
        if (msg instanceof String) {
            if (!isGameStarted) {
                switch ((String) msg) {
                    case START: startBlackjack(); break;
                    case REGISTER: register(); break;
                    default: unhandled(msg);
                }
            } else {
                switch ((String) msg) {
                    case BUSTED: playerBusted(); break;
                    case TURN: endRound(); break;
                    case HIT: hitHandler(); break;
                    case BET_DONE: applyBet(); break;
                    case QUIT: removePlayer(); break;
                    case STOP: stopBlackJack(); break;
                    default: unhandled(msg);
                }
            }
        } else if (msg instanceof Terminated) {
            removePlayer();
        } else
            unhandled(msg);
    }

    private void playerBusted() {
        log.info("Removing {}'s cards from the table", getSender().path().name());
        Map<ActorRef, List<Card>> cardTable = HazelcastManager.getInstance().getCardTable();
        cardTable.remove(getSender());
        Map<ActorRef, Integer> betsTable = HazelcastManager.getInstance().getBetsTable();
        takeMoney(betsTable, getSender());
        Queue<ActorRef> turns = HazelcastManager.getInstance().getTurnsQueue();
        turns.remove().tell(TURN, getSelf());
    }

    private void takeMoney(Map<ActorRef, Integer> betsTable, ActorRef ref) {
        Integer playerBet = betsTable.remove(ref);
        this.money += playerBet;
    }

    private void endRound() {
        Map<ActorRef, List<Card>> cardTable = HazelcastManager.getInstance().getCardTable();
        List<Card> cards = cardTable.get(getSelf());
        cards.add(holeCard);
        //facing up hole card
        log.info("Revealing the hole card: {}", holeCard);
        cardTable.put(getSelf(), cards);
        Integer dealersHandWeight = CardUtils.calculateWeight(cards);
        while (dealersHandWeight < DEALER_HIT_THRESHOLD) {
            Card nextCard = this.cardsDeck.nextCard();
            log.info("Dealer is hitting: {}", nextCard);
            cards.add(nextCard);
            cardTable.put(getSelf(), cards);
            dealersHandWeight += nextCard.getWeight();
        }
        log.info("Dealer has the following cards: {}, with hand weight: {}", cards, dealersHandWeight);
        Map<ActorRef, Integer> betsTable = HazelcastManager.getInstance().getBetsTable();
        if (dealersHandWeight > 21) {
            log.info("I've busted! =C");
            cardTable.keySet().stream().filter(a -> !a.equals(getSelf())).forEach(a -> giveMoney(betsTable, a));
            log.info("Got now: {}$", this.money);
        } else {
            handleResults(cardTable, betsTable, dealersHandWeight);
        }
        log.info("-------------------------ROUND---END---------------------------");
        startRound();
    }

    private void handleResults(Map<ActorRef, List<Card>> cardTable, Map<ActorRef, Integer> betsTable, Integer dealersHandWeight) {
        ArrayList<ActorRef> leftPlayers = new ArrayList<>(cardTable.keySet());
        leftPlayers.remove(getSelf());
        for (ActorRef player : leftPlayers) {
            List<Card> playerCards = cardTable.get(player);
            int comparison = CardUtils.calculateWeight(playerCards).compareTo(dealersHandWeight);
            if (comparison > 0) {
                log.info("{} wins!", player.path().name());
                giveMoney(betsTable, player);
            } else if (comparison < 0) {
                log.info("{} loses!", player.path().name());
                takeMoney(betsTable, player);
            } else {
                log.info("{}, tie game!", player.path().name());
                //TODO: figure out how to divide tie money in next rounds
            }
        }
    }

    private void giveMoney(Map<ActorRef, Integer> betsTable, ActorRef player) {
        Integer win = betsTable.get(player) * 2;
        betsTable.put(player, win);
        money -= win;
        player.tell(WIN, getSelf());
    }

    private void hitHandler() {
        Map<ActorRef, List<Card>> map = HazelcastManager.getInstance().getCardTable();
        List<Card> senderCards = map.get(getSender());
        Card hitCard = this.cardsDeck.nextCard();
        log.info("Dealing {} to {}", hitCard, getSender().path().name());
        senderCards.add(hitCard);
        map.put(getSender(), senderCards);
        getSender().tell(TURN, getSelf());
    }

    private void removePlayer() {
        log.info("{} has quit", getSender().path().name());
        this.getContext().unwatch(getSender());
        this.players.remove(getSender());
        betCounter--;
        HazelcastManager.getInstance().getCardTable().remove(getSender());
        HazelcastManager.getInstance().getBetsTable().remove(getSender());
        applyBet();
    }

    private void applyBet() {
        log.info("applying bet from {}", getSender().path().name());
        if (++betCounter == players.size()) {
            log.info("Starting to deal cards to players");
            Map<ActorRef, List<Card>> cardTable = HazelcastManager.getInstance().getCardTable();
            cardTable.clear();
            dealCardsToPlayers(cardTable);
            cardTable.put(getSelf(), new ArrayList<>(Collections.singletonList(this.cardsDeck.nextCard())));
            dealCardsToPlayers(cardTable);
            Queue<ActorRef> turns = HazelcastManager.getInstance().getTurnsQueue();
            cardTable.entrySet().stream().filter(a -> !a.equals(getSelf())).sorted(new CardWeightsComparator()).forEach(entry -> turns.add(entry.getKey()));
            turns.add(getSelf());
            turns.remove().tell(TURN, getSelf());
        }
    }

    private void dealCardsToPlayers(Map<ActorRef, List<Card>> map) {
        this.players.stream().forEach(p -> {
            List<Card> cards = map.get(p);
            if (cards == null) {
                cards = new ArrayList<>();
            }
            cards.add(this.cardsDeck.nextCard());
            map.put(p, cards);
        });
    }

    private void register() {
        this.getContext().watch(getSender());
        this.players.add(getSender());
    }

    private void stopBlackJack() {
        this.isGameStarted = false;
        this.players = new ArrayList<>();
    }

    private void startRound() {
        if (players.size() > 1) {
            this.betCounter = 0;
            this.cardsDeck = new BlackJackDeck();
            this.holeCard = this.cardsDeck.nextCard();
            HazelcastManager.getInstance().getCardTable().clear();
            HazelcastManager.getInstance().getBetsTable().clear();
            HazelcastManager.getInstance().getTurnsQueue().clear();
            this.players.stream().forEach(a -> a.tell(MAKE_BET, getSelf()));
        } else {
            stopBlackJack();
        }
    }

    private void startBlackjack() {
        if (!players.isEmpty() && this.money > DEALER_OUT_THRESHOLD) { //let's keep some money to dealer =D
            startRound();
            this.isGameStarted = true;
        }
    }

    @Override
    public void preStart() throws Exception {
        log.info("Starting...");
    }

    /**
     * Comparing in descending order way: from the highest hand to the lowest one.
     */
    private static class CardWeightsComparator implements Comparator<Map.Entry<ActorRef, List<Card>>> {
        @Override
        public int compare(Map.Entry<ActorRef, List<Card>> o1, Map.Entry<ActorRef, List<Card>> o2) {
            return CardUtils.calculateWeight(o2.getValue()).compareTo(CardUtils.calculateWeight(o1.getValue()));
        }
    }
}
