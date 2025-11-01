package items;

public class Pliers implements Items {
    @Override
    public String name() {return "Pliers";}

    @Override
    public String description() {return "Removes one of your teeth and adds to the scale";}

    @Override
    public boolean canUse() {return true;}

    @Override
    public void use() {}
}
