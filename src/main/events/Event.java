package events;
import cards.*;

// Essa classe tem com objetivo especificar os dados do Evento
// Ou seja, um evento pode ser chamado passando outros parâmetros com ele
// Ao invés dele só ter o nome (Enum), terá também dados importantes para analise
// Ao passar os eventos, deve chamar essa classe. E para pegar o evento tem o getType

public class Event {
    private final EventType type;   // Tipo de evento
    private final Player player;    // Qual player
    private final Card card;        // Qual carta
    private final Object data;      // Outras infos
    private final long timestamp;   // Tempo p histórico

    public Event(EventType type) {
        this(type, null, null, null);
    }

    public Event(EventType type, Player player) {
        this(type, player, null, null);
    }

    public Event(EventType type, Player player, Card card) {
        this(type, player, card, null);
    }

    public Event(EventType type, Player player, Card card, Object data) {
        this.type = type;
        this.player = player;
        this.card = card;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public EventType getType() { return type; }
    public Player getPlayer() { return player; }
    public Card getCard() { return card; }
    public Object getData() { return data; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s - Player: %s, Card: %s",
                timestamp, type,
                player != null ? player.getName() : "N/A",
                card != null ? card.getName() : "N/A");
    }
}
