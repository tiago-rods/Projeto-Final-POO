package items;

public class HourGlass implements Items {
    @Override
    public String name() {
        return "HourGlass";
    }

    @Override
    public String description() {
        return "This item skips your opponent next turn ";
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
