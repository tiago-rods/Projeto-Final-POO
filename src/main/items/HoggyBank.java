package items;

public class HoggyBank implements Items {
    @Override
    public String name() {
        return "HoggyBank";
    }

    @Override
    public String description() {
        return "When used, it breaks the hoggy bank revelling 4 bones that are granted to the player";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        return true;
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        // player.addBones(4); // Removed to avoid double addition
        game.grantBones(player, 4);
    }

}
