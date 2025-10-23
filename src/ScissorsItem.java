public class ScissorsItem implements Items {

    @Override
    public String name() { return "Scissors"; }

    @Override
    public String description() { return "Cuts one card of your oponent"; }

    @Override
    public boolean canUse(GameContext context, Player user) {
        //verificar se há alguma carta do oponente no campo
        //se tiver, escolher uma para cortar
    }

    @Override 
    public void use(GameContext context, Player user){
        //fazer a lógica
    }
 }