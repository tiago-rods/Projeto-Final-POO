package items;

public class Pliers implements Items {
    @Override
    public String name() {
        return "Pliers";
    }

    @Override
    public String description() {
        return "Removes one of your teeth and adds to the scale";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        return true;
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        // Pliers deal 1 damage to the scale in favor of the player
        // This is equivalent to dealing 1 direct damage to the opponent
        game.triggerItemEffect(this, player, null);
    }
}
