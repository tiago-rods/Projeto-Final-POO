package events;

public interface GameEvent {
    EventType getType();
    long getTime();
    Object getSource();
}



