package sigils;
import cards.CreatureCard;

public class FlySigil implements Sigil {
    @Override
    public String name() { return "Fly"; }

    @Override
    public String description() { return "This card attacks the opponent directly."; }

    @Override
    public void register(events.EventBus bus, CreatureCard host) {
        // Logic is handled by GameLogic flight check for now
    }
}
