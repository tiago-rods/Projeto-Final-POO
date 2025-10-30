package Items;

public class Scissors implements Items {
    @Override
    public String name() {return "Scissors"; }

    @Override
    public String description() {return "When used, the player chooses one card of the opponent to be cut";}

    public boolean canUse() {;}

    public void use() {;}
}
