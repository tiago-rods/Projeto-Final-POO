package events;

public class BaseGameEvent implements GameEvent {
    private final EventType type;
    private final long time;
    private final Object source;

    protected BaseGameEvent(EventType type, Object source) {
        this.type = type;
        this.source = source;
        this.time = System.currentTimeMillis();
    }

    @Override
    public EventType getType() { return type; }

    @Override
    public long getTime() { return time; }

    @Override
    public Object getSource() { return source; }
}
