package events;

import cards.*;
import java.util.Objects;

public class CardPlayedEvent extends BaseGameEvent {
    private final Card card;
    private final Player player;
    private final int position;

    public CardPlayedEvent(Object source, Card card, Player player, int position) {
        super(EventType.CARD_PLAYED, Objects.requireNonNull(source));
        this.card = Objects.requireNonNull(card);
        this.player = Objects.requireNonNull(player);
        this.position = position;
    }

    public Card getCard() { return card; }
    public Player getPlayer() { return player; }
    public int getPosition() { return position; }
}