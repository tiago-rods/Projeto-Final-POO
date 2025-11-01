package sigils;
import cards.CreatureCard;

public class FlySigil implements Sigil {
    @Override
    public String name() { return "Fly"; }

    @Override
    public String description() { return "This card attacks the opponent directly."; }

    @Override
        public void register(/* EventBus bus,  */CreatureCard host) {
        // code for what sigil will do

    }
}
