package items;

public class Hook implements Items {
    @Override
    public String name() {
        return "Hook";
    }

    @Override
    public String description() {
        return "Brings one of your opponents card to your side";
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
