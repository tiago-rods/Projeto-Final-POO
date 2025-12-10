package sigils;

import cards.CreatureCard;
import events.Event;
import events.EventBus;
import events.EventType;

public class RabbitHoleSigil implements Sigil {

    @Override
    public String name() {
        return "Rabbit Hole";
    }

    @Override
    public String description() {
        return "When played, a Rabbit is created in your hand.";
    }

    @Override
    public void register(EventBus bus, CreatureCard host) {
        bus.subscribe(EventType.CARD_PLAYED, event -> {
            // Check if the card played is THIS host card
            if (event.getCard() == host) {
                System.out.println("🐇 Rabbit Hole triggered!");
                
                // Creates a Rabbit card
                CreatureCard rabbit = new CreatureCard(
                        "rabbit_from_hole_" + System.currentTimeMillis(),
                        "Rabbit",
                        0, 1, 0, 0,
                        "/img/regular/rabbit.png"
                );

                // Adds to the player's hand
                event.getPlayer().getHand().add(rabbit);
                
                // Publish State Change to update UI
                bus.publish(new Event(EventType.GAME_STATE_CHANGED, event.getPlayer()));
                bus.publish(new Event(EventType.SIGIL_ACTIVATED, event.getPlayer(), host, "Rabbit Hole"));
            }
        });
    }
}
