package items;

public class BottledSquirrel implements Items {
    @Override
    public String name() {
        return "BottledSquirrel";
    }

    @Override
    public String description() {
        return "When used, the player receives a free squirrel ";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        return player.getHand().size() < 7; // Check if hand is not full
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        game.drawSquirrelFromItem(player);
    }

}
