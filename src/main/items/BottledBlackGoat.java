package items;

public class BottledBlackGoat implements Items {
    @Override
    public String name() {
        return "Bottled Black Goat";
    }

    @Override
    public String description() {
        return "When used, the player receives a free black goat";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        return true;
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        // TODO: Implement logic
    }

}
