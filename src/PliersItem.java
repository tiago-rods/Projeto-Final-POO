public class PliersItem implements Items {

    @Override
    public String name() { return "Pliers"; }

    @Override
    public String description() { return "Gains 1 weight in the balance (life)"}

    @Override
    public boolean canUse(GameContext context, Player user) { return true; }
    
    @Override 
    public void use(GameContext, Player User){ // preciso fazer ainda
       // user.addBalance(1)
       // context.log.publish(new BalanceGainedEvent(user, 1));
       user.getItem().remove(this);
    }
    //
}
