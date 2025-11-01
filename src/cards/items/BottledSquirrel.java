package items;

public class BottledSquirrel implements Items {
    @Override
    public String name() {return "BottledSquirrel"; }

    @Override
    public String description() {return "When used, the player receives a free squirrel "; }

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA

}
