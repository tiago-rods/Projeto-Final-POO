package sigils;

import cards.CreatureCard;
import events.Event;
import events.EventBus;
import events.EventType;

public class FledglingSigil implements Sigil {
    
    private int turnsSurvived = -1; // deixei -1 pra q só conte depois de 2 rodadas, fiz assim p ficar mais difícil
    private boolean evolved = false;

    @Override
    public String name() {
        return "Fledgling";
    }

    @Override
    public String description() {
        return "After surviving 1 turn, this card evolves into a stronger form.";
    }

    @Override
    public void register(EventBus bus, CreatureCard host) {
        bus.subscribe(EventType.TURN_ENDED, event -> {
             if (evolved) return;
             if (host.getPosLine() == -1 || host.getPosCol() == -1) return; // Not on board
             
             // Check ownership
             int line = host.getPosLine();
             int playerOrder = event.getPlayer().getOrder();
             boolean isOwner = false;
             if (playerOrder == 1 && (line == 2 || line == 3)) isOwner = true;
             else if (playerOrder == 2 && (line == 0 || line == 1)) isOwner = true;
             
             if (isOwner) {
                 System.out.println("🐣 " + host.getName() + " survived " + turnsSurvived + " turns.");
                 if (turnsSurvived >= 1) {
                     // Evolve!
                     evolved = true;
                     bus.publish(new Event(EventType.SIGIL_TRIGGERED, event.getPlayer(), host, "Fledgling_Action"));
                 }
                 turnsSurvived++; // só aumenta dps da primeira vez q é jogado
             }
        });
    }
}
