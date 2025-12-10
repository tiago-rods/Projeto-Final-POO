package sigils;

import cards.CreatureCard;
import events.Event;
import events.EventBus;
import events.EventType;
import cards.Player;

public class BeesWithinSigil implements Sigil {

    @Override
    public String name() {
        return "Bees Within";
    }

    @Override
    public String description() {
        return "When this card is damaged, a Bee is created in your hand.";
    }

    @Override
    public void register(EventBus bus, CreatureCard host) {
        bus.subscribe(EventType.CREATURE_DAMAGED, event -> {
            // Check if the damaged card is THIS host card
            if (event.getCard() == host) {
                System.out.println("🐝 Bees Within triggered (Damaged)!");
                giveBee(host, event.getPlayer(), bus);
            }
        });

        // Also trigger on death
        bus.subscribe(EventType.CREATURE_DIED, event -> {
            if (event.getCard() == host) {
                System.out.println("🐝 Bees Within triggered (Died)!");
                giveBee(host, event.getPlayer(), bus);
            }
        });
    }

    private void giveBee(CreatureCard host, Player player, EventBus bus) {
        CreatureCard bee = new CreatureCard(
                        "bee_from_within_" + System.currentTimeMillis(),
                "Bee",
                1, 1, 0, 0,
                "/img/regular/bee.png"
        );
        bee.addSigil(new FlySigil());

        player.getHand().add(bee);

        bus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
        bus.publish(new Event(EventType.SIGIL_ACTIVATED, player, host, "Bees Within"));
    }
}
