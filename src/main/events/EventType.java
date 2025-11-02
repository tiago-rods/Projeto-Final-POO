package events;

public enum EventType {
    //Combat events
    CARD_PLAYED,
    ATTACK_DECLARED,
    DAMAGE_DEALT,
    CREATURE_DEATH,

    //Turn events
    TURN_START,
    TURN_END,
    PHASE_CHANGE,

    //Game events
    GAME_START,
    GAME_OVER,

    //Other events
    SACRIFICE_MADE,
    SIGIL_TRIGGERED,
    ITEM_USED,

    //VER SE EXISTEM MAIS EVENTOS A SEREM FEITOS
}
