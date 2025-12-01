package cards;

import sigils.FlySigil;
import sigils.Sigil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
    // normal deck, draw pile and discard pile
    private final List<CreatureCard> deck;
    private final List<CreatureCard> squirrelDeck;
    private final List<CreatureCard> discardPile; // just in case we need it
    private final Random random;

    // List of Sigils
    Sigil fly = new FlySigil();

    public Deck() {
        this.deck = new ArrayList<>();
        this.squirrelDeck = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.random = new Random();

        initializeSquirrelDeck();
        initializeDeck();
    }

    // Adding all other cards to normal deck
    // Initialize normal deck
    public void initializeDeck() {
        CreatureCard coyote1 = new CreatureCard("coyote1", "Coyote", 2, 1, 0, 4, "/img/regular/coyote.png");
        deck.add(coyote1);

        CreatureCard grizzly1 = new CreatureCard("grizzly1", "Grizzly", 4, 6, 3, 0, "/img/regular/grizzly.png");
        deck.add(grizzly1);

        CreatureCard opossum1 = new CreatureCard("opossum1", "Opossum", 1, 1, 0, 2, "/img/regular/opossum.png");
        deck.add(opossum1);

        CreatureCard rabbit1 = new CreatureCard("rabbit1", "Rabbit", 0, 1, 0, 0, "/img/regular/rabbit.png");
        deck.add(rabbit1);

        CreatureCard stoat1 = new CreatureCard("stoat1", "Stoat", 1, 3, 1, 0, "/img/regular/stoat_talking.png");
        deck.add(stoat1);
        CreatureCard stoat2 = new CreatureCard("stoat2", "Stoat", 1, 3, 1, 0, "/img/regular/stoat_talking.png");
        deck.add(stoat2);

        CreatureCard wolf1 = new CreatureCard("wolf1", "Wolf", 3, 2, 2, 0, "/img/regular/wolf.png");
        deck.add(wolf1);

        CreatureCard stuntedWolf1 = new CreatureCard("stuntedWolf1", "StuntedWolf", 2, 2, 1, 0,
                "/img/regular/wolf_talking.png");
        deck.add(stuntedWolf1);

        CreatureCard amalgam1 = new CreatureCard("amalgam1", "Amalgam", 3, 3, 2, 0,
                "/img/regular/amalgam.png");
        deck.add(amalgam1);

        CreatureCard geck1 = new CreatureCard("geck1", "Geck", 1, 1, 0, 0,
                "/img/regular/geck.png");
        deck.add(geck1);

        CreatureCard leshy = new CreatureCard("leshy", "Leshy", 0, 0, 0, 0,
                "/img/regular/leshy.png");
        deck.add(leshy);

        CreatureCard rattler1 = new CreatureCard("rattler1", "Rattler", 3, 1, 0, 6,
                "/img/regular/rattler.png");
        deck.add(rattler1);

        CreatureCard ringWorm1 = new CreatureCard("ringWorm1", "Ring Worm", 0, 1, 1, 0,
                "/img/regular/ring_worm.png");
        deck.add(ringWorm1);

        CreatureCard riverSnapper1 = new CreatureCard("riverSnapper1", "River Snapper", 1, 6, 2, 0,
                "/img/regular/river_snapper.png");
        deck.add(riverSnapper1);

        CreatureCard urayuli1 = new CreatureCard("urayuli1", "Urayuli", 7, 7, 4, 0, "/img/regular/urayuli.png");
        deck.add(urayuli1);


        //Sigils.FlySigil
        CreatureCard raven1 = new CreatureCard("raven1", "Raven", 2, 3, 2, 0, "/img/regular/raven.png");
        raven1.addSigil(fly);
        deck.add(raven1);

        CreatureCard sparrow1 = new CreatureCard("sparrow1", "Sparrow", 1, 2, 1, 0, "/img/regular/sparrow.png");
        sparrow1.addSigil(fly);
        deck.add(sparrow1);

        CreatureCard bat1 = new CreatureCard("bat1", "Bat", 2, 1, 0, 4, "/img/regular/bat.png");
        bat1.addSigil(fly);
        deck.add(bat1);

        CreatureCard bee1 = new CreatureCard("bee1", "Bee", 1, 1, 0, 0, "/img/regular/bee.png");
        bee1.addSigil(fly);
        deck.add(bee1);

        CreatureCard mothman1 = new CreatureCard("mothman1", "Mothman", 7, 3, 1, 0, "/img/regular/mothman.png");
        mothman1.addSigil(fly);
        deck.add(mothman1);

        CreatureCard turkeyVulture1 = new CreatureCard("turkeyVulture1", "Turkey Vulture", 3, 3, 0, 8,
                "/img/regular/turkey_vulture.png");
        deck.add(turkeyVulture1);
    }

    // Initialize squirrelDeck
    public void initializeSquirrelDeck() {
        // We can change the amount of squirrels
        int qntSquirrel = 10;
        for (int i = 0; i < qntSquirrel; i++) {
            CreatureCard squirrel = new CreatureCard(
                    "squirrel_" + i,
                    "Squirrel",
                    0,
                    1,
                    0,
                    0,
                    "/img/regular/squirrel.png");
            squirrelDeck.add(squirrel);
        }
    }

    // Adds a card to deck
    public void addCardToDeck(CreatureCard card) {
        deck.add(card);
    }

    // draw a Squirrel
    public CreatureCard drawSquirrel(List<Card> hand) {
        if (squirrelDeck.isEmpty())
            return null;
        // Remove do início (pode embaralhar antes se quiser aleatoriedade)
        CreatureCard squirrelCard = squirrelDeck.remove(0);
        if (squirrelCard != null) {
            hand.add(squirrelCard);
        }
        return squirrelCard;
    }

    // Maybe we will need it
    public void shuffleSquirrels() {
        Collections.shuffle(squirrelDeck, random);
    }

    // Removes a card to deck
    public boolean removeCard(CreatureCard card) {
        return deck.remove(card);
    }

    // returns a copy of unmodifiable deck
    public List<CreatureCard> getCardDeck() {
        return Collections.unmodifiableList(deck);
    }

    // returns a copy of unmodifiable squirrel deck
    public List<CreatureCard> getSquirrelCardDeck() {
        return Collections.unmodifiableList(squirrelDeck);
    }

    // shuffles the deck
    public void shuffle() {
        Collections.shuffle(deck, random);
    }

    // draw a card from the "top" and adds it to hand
    public CreatureCard draw(List<Card> hand) {
        if (deck.isEmpty())
            return null;
        CreatureCard card = deck.remove(0);
        if (card != null) {
            hand.add(card);
        }
        return card;
    }

    // Draw multiple cards
    public void draw(int amount, List<Card> hand) {
        for (int i = 0; i < amount; i++) {
            CreatureCard card = deck.remove(0);
            if (card != null) {
                hand.add(card);
            } else {
                break; // No more cards
            }
        }
    }

    // Discard a card
    public void discard(CreatureCard card) {
        discardPile.add(card);
    }

    // Number of remaining cards
    public int getRemainingCards() {
        return deck.size();
    }

    // Retorna o número de cartas na pilha de descarte
    public int getDiscardedCards() {
        return discardPile.size();
    }

    // Retorna uma cópia da pilha de descarte
    public List<CreatureCard> getDiscardPile() {
        return Collections.unmodifiableList(discardPile);
    }

    // Returns a copy of drawPile (useful for visualization)
    public List<CreatureCard> getDrawPile() {
        return Collections.unmodifiableList(deck);
    }

    // Reset deck completely
    public void reset() {
        deck.clear();
        discardPile.clear();
        squirrelDeck.clear();
    }

}
