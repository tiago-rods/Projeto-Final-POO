package sigils;

import cards.CreatureCard;
import events.Event;
import events.EventBus;
import events.EventType;

public class SprinterSigil implements Sigil {
    
    private boolean movingRight = true; // Defaults to right

    @Override
    public String name() {
        return "Sprinter";
    }

    @Override
    public String description() {
        return "At the end of the owner's turn, this card moves in the direction of the sigil.";
    }
    
    public boolean isMovingRight() {
        return movingRight;
    }
    
    public void toggleDirection() {
        this.movingRight = !this.movingRight;
    }

    @Override
    public void register(EventBus bus, CreatureCard host) {
       // Logic handled in GameLogic to ensure Left-to-Right execution order
    }
}
