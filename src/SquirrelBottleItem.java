// o item esquilo engarrafado cria um esquilo na mão do jogador quando usado

public class SquirrelBottleItem implements Items {
    @Override
    public String name() { return "Squirrel Bottle"; }

    @Override
    public String description() { return "Creates 1 Squirrel in hand (one use)."; }

    @Override 
    public boolean canUse(GameContext context, Player user) { return true; }
    
    @Override
    public void use(GameContext context, Player user){
        CreatureCard squirrel = CardFactory.createSquirrel(user);
        user.getHand().add(squirrel);
        context.log().publish(new CardCreatedEvent(squirrel, "HAND"));
        user.getItems().remove(this);
    }
}
