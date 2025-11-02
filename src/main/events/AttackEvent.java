package events;

import cards.*;
import jdk.jfr.Event;

public class AttackEvent extends BaseGameEvent{
    private final Card attacker;
    private final Card target;
    private final int damage;


    public AttackEvent(Card attacker, Card target, int damage) {
        super(EventType.ATTACK_DECLARED, attacker);
        this.attacker = attacker;
        this.target = target;
        this.damage = damage;
    }
        public Card getAttacker() { return attacker; }
        public Card getTarget() { return target; }
        public int getDamage() { return damage; }
}

