package cards;

import sigils.Sigil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CreatureCard extends Card {
    private int attack;
    private int health;
    private final int bloodCost;
    private final int bonesCost;
    private final List<Sigil> sigils = new ArrayList<>();
    private boolean justPlayed = false;

    public CreatureCard(String id, String name, int attack, int health, int bloodCost, int bonesCost,
            String imagePath) {
        super(id, name, imagePath);
        this.attack = attack;
        this.health = health;
        this.bloodCost = bloodCost;
        this.bonesCost = bonesCost;
    }

    public int getAttack() {
        return attack;
    }

    public int getHealth() {
        return health;
    }

    public int getBloodCost() {
        return bloodCost;
    }

    public int getBonesCost() {
        return bonesCost;
    }

    public boolean isFree() {
        return bloodCost == 0 && bonesCost == 0;
    }

    public void addSigil(Sigil sigil) {
        sigils.add(sigil);
    }

    public List<Sigil> getSigils() {
        return Collections.unmodifiableList(sigils);
    }

    public boolean isJustPlayed() {
        return justPlayed;
    }

    public void setJustPlayed(boolean justPlayed) {
        this.justPlayed = justPlayed;
    }

    public void takeDamage(int dmg) {
        this.health -= dmg;
    }
}