package items;

public class Scissors implements Items {
    @Override
    public String name() {return "Scissors"; }

    @Override
    public String description() {return "When used, the player chooses one card of the opponent to be cut";}

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA

}
