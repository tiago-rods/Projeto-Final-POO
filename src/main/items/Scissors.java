package items;

public class Scissors implements Items {
    @Override
    public String name() {
        return "Scissors";
    }

    @Override
    public String description() {
        return "When used, the player chooses one card of the opponent to be cut";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        return true;
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        // TODO: Implement selection logic
    }

}
