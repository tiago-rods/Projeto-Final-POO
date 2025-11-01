package items;

public class HoggyBank implements Items{
    @Override
    public String name() {return "Hoggy Bank";}

    @Override
    public String description() {return "When used, it breaks the hoggy bank revelling 4 bones that are granted to the player";}

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA



}
