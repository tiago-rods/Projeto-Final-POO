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

    public Deck() {
        this.deck = new ArrayList<>();
        this.squirrelDeck = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.random = new Random();

        // We can change the amount of squirrels, but we used 6 just to make testing easier later.
        int qntSquirrel = 6;
        for (int i = 0; i < qntSquirrel; i++) {
            CreatureCard squirrel = new CreatureCard(
                    "squirrel_" + i,
                    "Squirrel",
                    0,
                    1,
                    0,
                    0,
                    "/img/regular/squirrel.png"
            );
            squirrelDeck.add(squirrel);
        }

        // Adding all other cards to normal deck

    }


    // Adds a card to deck
    public void addCard(CreatureCard card) {
        deck.add(card);
    }

    // draw a Squirrel
    public CreatureCard drawSquirrel() {
        if (squirrelDeck.isEmpty()) {
            return null;
        }
        // Remove do início (pode embaralhar antes se quiser aleatoriedade)
        return squirrelDeck.remove(0);
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
    public List<CreatureCard> getCards() {
        return Collections.unmodifiableList(deck);
    }

    // shuffles the deck
    public void shuffle() {
        Collections.shuffle(deck, random);
    }

    // draw a card from the "top"
    public CreatureCard draw() {
        if (deck.isEmpty()) {
            return null; // No cards to draw
        }

        return deck.remove(0);
    }

    // Draw multiple cards
    public List<CreatureCard> draw(int amount) {
        List<CreatureCard> drawnCards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            CreatureCard card = draw();
            if (card != null) {
                drawnCards.add(card);
            } else {
                break; // No more cards
            }
        }
        return drawnCards;
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