package items;

public class Hook implements Items {
    @Override
    public String name() {return "Hook";}

    @Override
    public String description() {return "Brings one of your opponents card to your side";}

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA


}
