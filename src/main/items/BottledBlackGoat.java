package items;

public class BottledBlackGoat implements Items {
    @Override
    public String name() {return "Bottled Black Goat";}

    @Override
    public String description() {return "When used, the player receives a free black goat";}

    @Override
    public boolean canUse(){
        return true;
    } // FAZER QUANDO LÓGICA ESTIVER CORRETA

    @Override
    public void use(){} // FAZER QUANDO LÓGICA ESTIVER CORRETA

}
