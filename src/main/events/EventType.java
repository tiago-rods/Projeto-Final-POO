package events;

// Ao publicar seria assim: eventBus.publish(EventType.CARD_DRAWN);
// Tipos de eventos que podem ocorrer no jogo
public enum EventType {
    // Eventos de turno
    TURN_STARTED,
    TURN_ENDED,
    PHASE_CHANGED,

    // Eventos de cartas
    CARD_DRAWN,
    CARD_PLAYED,
    CARD_MOVED,
    CARD_SACRIFICED,
    CARD_DESTROYED,

    // Eventos de combate
    ATTACK_DECLARED,
    DAMAGE_DEALT,
    CREATURE_DAMAGED,
    CREATURE_DIED,
    COMBAT_RESOLVED,

    // Eventos de recursos
    BONES_GAINED,
    BONES_SPENT,
    LIFE_LOST,

    // Eventos de sigilos
    SIGIL_ACTIVATED,
    SIGIL_TRIGGERED,

    // Eventos de jogo
    PLAYER_ACTION,
    GAME_STATE_CHANGED,
    GAME_ENDED,
    ITEM_USED
}
