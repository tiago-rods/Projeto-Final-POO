package sigils;
import cards.CreatureCard;

public interface Sigil {
    // Methods that will be override for each sigil
    String name();
    String description();

    // register sigil
    default void register(/* EventBus bus,  */CreatureCard host) {
        //
    }
}
