import java.util.ArrayList;
// We are using collections just for safety
import java.util.Collections;
import java.util.List;

public final class CreatureCard extends Card {
    private int attack;
    private int health;
    private final int bloodCost;
    private final int bonesCost;
    // List type is for interface type (an array of elements of Sigil type inside)
    private final List<Sigil> sigils = new ArrayList<>();

    public CreatureCard(String id, String name, int attack, int health, int bloodCost, int bonesCost, String imagePath) {
        // super calls Card constructor
        super(id, name, imagePath);
        this.attack = attack;
        this.health = health;
        this.bloodCost = bloodCost;
        this.bonesCost = bonesCost;
    }

    public int getAttack() { return attack; }
    public int getHealth() { return health; }

    public int getBloodCost() { return bloodCost; }
    public int getBonesCost() { return bonesCost; }

    public boolean isFree() { return bloodCost == 0 && bonesCost == 0; }

    public void addSigil(Sigil sigil) {
        // a card can have more than one sigil
        sigils.add(sigil);
    }

    // Collections is used to encapsulate the list, that is: when calling getSigils() you can see, iterate and read, but cant add, remove or change itens
    public List<Sigil> getSigils() { return Collections.unmodifiableList(sigils); }


     public void takeDamage(int dmg /* , GameContext ctx */) {
        this.health -= dmg;
        /*
        ctx.getEventBus().publish(new CreatureDamagedEvent(this, dmg));
        if (health <= 0) {
            die(ctx);
        }
        * */

    }

    /*
    private void die(GameContext ctx) {
        ctx.getEventBus().publish(new CreatureDiedEvent(this));
        // Remoção do tabuleiro/transferência ao cemitério será tratada pelo Board/Engine
    }
    * */

}