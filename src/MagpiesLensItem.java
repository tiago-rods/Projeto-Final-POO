//Item Magpies Len's permite que você compre uma carta a sua escolha do seu baralho e a coloque na sua mão.

public class MagpiesLensItem implements Items {
    @Override

    public String name() { return "Magpie's Lens"; }
    
    @Override
    public String description() { return "Search your deck for 1 card and add it to your hand (one use)."; }

    @Override
    public boolean canUse(GameContext ctx, Player user) { //se o deck estiver vazio, não pode usar
        return !user.getDeck().isEmpty();
    }

    @Override
    public void use(GameContext context, Player user){
        Card drawn = user.getDeck().find(c -> true); //encontra a carta no baralho
        if(drawn != null){
            user.getHand().add(drawn);
            context.log().publish(new CardMovedEvent(drawn, "DECK", "HAND"));
            user.getItems().remove(this);
        }
    }
}
