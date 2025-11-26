package items;

public class MagpiesLens implements Items {
    @Override
    public String name() {
        return "MagpiesLens";
    }

    @Override
    public String description() {
        return "Search in the Creatures deck for one card of interest";
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
