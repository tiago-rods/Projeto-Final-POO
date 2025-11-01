package items;

public class HourGlass implements Items {
    @Override
    public String name() {return "Hour Glass";}
    @Override
    public String description() {return "This item skips your opponent next turn ";}

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA



}
