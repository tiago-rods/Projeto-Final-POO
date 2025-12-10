package sigils;
import cards.CreatureCard;
import events.EventBus;

public interface Sigil {
    // Methods that will be override for each sigil
    String name();
    String description();

    // register sigil
    default void register(EventBus bus, CreatureCard host) {
        // What sigil will do ... 
    }
}
