public class BlackGoatBottle implements Items {
    @Override
    public String name() { return "Black Goat Bottle"; }

    @Override
    public String description() { return "Creates 1 Black Goat in hand (one use)."; }

    @Override
    public boolean canUse(GameContext context, Player user) { return true; }

    @Override
    public void use(GameContext context, Player user) {
        CreatureCard goat = CardFactory.createBlackGoat(user);
        user.getHand().add(goat);
        context.log().publish(new CardCreatedEvent(goat, "HAND"));
        user.getItems().remove(this);
    }
}

