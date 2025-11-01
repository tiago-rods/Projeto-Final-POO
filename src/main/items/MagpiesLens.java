package items;

public class MagpiesLens implements Items{
    @Override
    public String name() {return "Magpies Lens";}

    @Override
    public String description() {return "Search in the Creatures deck for one card of interest";}

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA


}
