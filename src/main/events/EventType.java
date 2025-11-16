package events;

// Ao publicar seria assim: eventBus.publish(EventType.CARD_DRAWN);
// Tipos de eventos que podem ocorrer no jogo
public enum EventType {
    // ===== EVENTOS DE TURNO =====
    TURN_STARTED,           // Início do turno
    TURN_ENDED,             // Fim do turno
    PHASE_CHANGED,          // Mudança de fase (draw, main, attack, end)
    TURN_SWITCHED,          // Turno trocado para outro jogador

    // ===== EVENTOS DE CARTAS =====
    CARD_DRAWN,             // Carta comprada
    CARD_PLAYED,            // Carta jogada no tabuleiro
    CARD_MOVED,             // Carta movida (posicionamento -> ataque)
    CARD_SACRIFICED,        // Carta sacrificada
    CARD_DESTROYED,         // Carta destruída (sem gerar ossos extras)
    CARD_SELECTED,          // Carta selecionada na mão
    CARD_DESELECTED,        // Carta desselecionada

    // ===== EVENTOS DE COMBATE =====
    ATTACK_PHASE_STARTED,   // Fase de ataque iniciada
    ATTACK_DECLARED,        // Ataque declarado por uma criatura
    DAMAGE_DEALT,           // Dano causado
    CREATURE_DAMAGED,       // Criatura recebeu dano
    CREATURE_DIED,          // Criatura morreu
    COMBAT_RESOLVED,        // Combate resolvido
    DIRECT_DAMAGE,          // Dano direto ao jogador

    // ===== EVENTOS DE RECURSOS =====
    BONES_GAINED,           // Ossos ganhos
    BONES_SPENT,            // Ossos gastos
    LIFE_LOST,              // Vida perdida
    RESOURCE_CHANGED,       // Recurso alterado (genérico)

    // ===== EVENTOS DE SACRIFÍCIO =====
    SACRIFICE_MODE_STARTED, // Modo de sacrifício iniciado
    SACRIFICE_MODE_ENDED,   // Modo de sacrifício encerrado
    SACRIFICE_SELECTED,     // Carta marcada para sacrifício
    SACRIFICE_CANCELLED,    // Sacrifício cancelado

    // ===== EVENTOS DE SIGILOS =====
    SIGIL_ACTIVATED,        // Sigilo ativado
    SIGIL_TRIGGERED,        // Sigilo disparado (efeito executado)

    // ===== EVENTOS DE JOGO =====
    PLAYER_ACTION,          // Ação genérica do jogador
    GAME_STATE_CHANGED,     // Estado do jogo mudou
    GAME_ENDED,             // Jogo terminou
    GAME_PAUSED,            // Jogo pausado
    GAME_RESUMED,           // Jogo retomado

    // ===== EVENTOS DE UI =====
    UI_UPDATE_REQUESTED,    // Solicitação de atualização da UI
    SLOT_CLICKED,           // Slot do tabuleiro clicado
    DECK_CLICKED,           // Deck clicado
    HAND_UPDATED,           // Mão atualizada
    BOARD_UPDATED,          // Tabuleiro atualizado

    // ===== EVENTOS DE VALIDAÇÃO =====
    INVALID_ACTION,         // Ação inválida tentada
    COST_CHECK_FAILED,      // Verificação de custo falhou
    POSITION_INVALID        // Posição inválida
}
