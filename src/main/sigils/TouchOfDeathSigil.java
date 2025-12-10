package sigils;

import cards.CreatureCard;
import events.EventBus;

public class TouchOfDeathSigil implements Sigil {

    @Override
    public String name() {
        return "Touch of Death";
    }

    @Override
    public String description() {
        return "Instantly destroys any card damaged by this card.";
    }

    @Override
    public void register(EventBus bus, CreatureCard host) {
        // Logic handled in GameLogic directly
    }
}
