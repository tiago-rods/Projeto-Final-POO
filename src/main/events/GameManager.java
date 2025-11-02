package events;

import cards.*;

public class GameManager {
    private final EventBus eventBus = EventBus.getInstance();
    private Player currentPlayer;

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public void playCard(Card card, int position) {
        if (currentPlayer == null) {
            throw new IllegalStateException("currentPlayer não definido");
        }

        // lógica para jogar a carta...

        eventBus.publish(new CardPlayedEvent(this, card, currentPlayer, position));
    }
}