package events;
import cards.*;
import org.jetbrains.annotations.NotNull;
import sigils.*;

public class EventLogics {

    private EventBus eventBus;
    private GameLogic game;

    public EventLogics(GameLogic game, EventBus eventbus) {
        this.game = game;
        this.eventBus = eventbus;

        setupSubscribers();
    }

    // esse event, é aquele chamado ao clicar no botão
    private void setupSubscribers() {
        // === EVENTOS DE TURNO ===
        eventBus.subscribe(EventType.TURN_STARTED, this::handleTurnStarted);
        eventBus.subscribe(EventType.TURN_ENDED, this::handleTurnEnded);
        eventBus.subscribe(EventType.PHASE_CHANGED, this::handlePhaseChanged);

        // === EVENTOS DE CARTAS ===
        eventBus.subscribe(EventType.CARD_DRAWN, this::handleCardDrawn);
        eventBus.subscribe(EventType.CARD_MOVED, this::handleCardMoved);
        eventBus.subscribe(EventType.CARD_PLAYED, this::handleCardPlayed);
        eventBus.subscribe(EventType.CARD_DESTROYED, this::handleCardDestroyed);
        eventBus.subscribe(EventType.CARD_SACRIFICED, this::handleCardSacrificed);


        // === EVENTOS DE COMBATE ===
        eventBus.subscribe(EventType.ATTACK_DECLARED, this::handleAttackDeclared);
        eventBus.subscribe(EventType.CREATURE_DAMAGED, this::handleCreatureDamaged);
        eventBus.subscribe(EventType.CREATURE_DIED, this::handleCreatureDied);
        eventBus.subscribe(EventType.DAMAGE_DEALT, this::handleDamageDealt);
        eventBus.subscribe(EventType.COMBAT_RESOLVED, this::handleCombatResolved);


        // === EVENTOS DE RECURSOS ===
        eventBus.subscribe(EventType.BONES_GAINED, this::handleBonesGained);
        eventBus.subscribe(EventType.LIFE_LOST, this::handleLifeLost);
        eventBus.subscribe(EventType.BONES_SPENT, this::handleBonesSpent);

        // === EVENTOS DE SIGILOS ===
        eventBus.subscribe(EventType.SIGIL_ACTIVATED, this::handleSigilActivated);

        // === EVENTOS DE JOGO ===
        eventBus.subscribe(EventType.PLAYER_ACTION, this::handlePlayerAction);
        eventBus.subscribe(EventType.GAME_STATE_CHANGED, this::handleGameStateChanged);
        eventBus.subscribe(EventType.GAME_ENDED, this::handleGameEnded);

    }


    // === TURN HANDLERS ===

    private void handleTurnStarted(@NotNull Event event) {
        Player player = event.getPlayer();
        System.out.println("🎯" + player.getName() + "'s Turn Started");

        // Ativar efeitos de início de turno
        activateStartOfTurnEffects(player);

        // Publicar mudança de estado para atualizar UI
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleTurnEnded(@NotNull Event event) {
        Player player = event.getPlayer();
        System.out.println("🏁" + player.getName() +"'s turn ended!");

        // Verificar condições de vitória antes de passar o turno
        if (game.isGameOver()) {
            Player winner = game.getWinner();
            eventBus.publish(new Event(EventType.GAME_ENDED, winner));
            return;
        }
    }

    private void handlePhaseChanged(@NotNull Event event) {
        Object phaseData = event.getData();
        Player player = event.getPlayer();

        System.out.println("📅 Phase changed " + phaseData + " (Player: " + player.getName() + ")");

        // TODO: Lógica específica para cada fase será implementada quando definirmos as fases
        // Por enquanto apenas log e notificação de mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === CARDS HANDLERS ===

    private void handleCardDrawn(@NotNull Event event) {
        Player player = event.getPlayer();
        Card card = event.getCard();

        if (player == game.getPlayer1()) {
            game.drawCard(game.getPlayer1(), game.getDeckP1());
        } else if (player == game.getPlayer2()) {
            game.drawCard(game.getPlayer2(), game.getDeckP2());
        } else {
            System.out.println("Erro ao comprar carta");
        }

        System.out.println("📋 " + player.getName() + " drew: " + card.getName());

        // Como não há limite de mão, apenas log
        System.out.println("📝 " + player.getName() + " now has " + player.getHand().size() + " cards in hand.");

        // Publicar mudança de estado para atualizar UI
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));

    }

    private void handleCardPlayed(@NotNull Event event) {
        Player player = event.getPlayer();
        Card card = event.getCard();

        System.out.println("🃏 " + player.getName() + " played " + card.getName());

        // TODO: Se for CreatureCard, ativar efeitos de entrada (quando implementarmos sigils)
        if (card instanceof CreatureCard creature) {
            System.out.println("🦎 Creature " + creature.getName() + " was played!");
            // activateOnPlayEffects(creature, player); // TODO: Ativar quando sigils estiverem prontos

            // Atualizar ícone de vida da carta
            creature.changeLifeIcon(creature.getHealth());
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }


    private void handleCardMoved(@NotNull Event event) {
        Card card = event.getCard();
        Player player = event.getPlayer();
        Object data = event.getData(); // Pode conter informações sobre posição antiga/nova

        System.out.println("🔄 " + player.getName() +"'s" + card.getName() + " moved forward ");

        // Se moveu para zona de ataque, pode ativar efeitos específicos
        if (card instanceof CreatureCard creature) {
            // Verificar se moveu para zona de ataque
            if (creature.getPosLine() == 1 || creature.getPosLine() == 2) {
                System.out.println("⚔️ " + creature.getName() + " is now in the attack tile!");
            }
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleCardDestroyed(@NotNull Event event) {
        Card card = event.getCard();
        Player owner = event.getPlayer();

        System.out.println("💥 " + card.getName() + " was destroyed!");

        // Mover para o cemitério
        owner.getGraveyard().add(card);

        // Se for criatura, gerar ossos
        if (card instanceof CreatureCard) {
            owner.addBones(1);
            eventBus.publish(new Event(EventType.BONES_GAINED, owner, null, 1));
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, owner));
    }


    private void handleCardSacrificed(@NotNull Event event) {
        Card card = event.getCard();
        Player player = event.getPlayer();

        System.out.println("⚰️ " + card.getName() + " was sacrificed by " + player.getName());

        // Mover para o cemitério
        player.getGraveyard().add(card);

        // Gerar ossos (sacrifício sempre gera ossos)
        player.addBones(1);
        eventBus.publish(new Event(EventType.BONES_GAINED, player, null, 1));

        // Ativar efeitos de morte/sacrifício quando implementarmos sigils
        if (card instanceof CreatureCard creature) {
            System.out.println("💀 Death effects from " + creature.getName() + " will be activated.");
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }


    // === COMBAT HANDLERS ===

    private void handleAttackDeclared(@NotNull Event event) {
        Card attackerCard = event.getCard();
        Player attacker = event.getPlayer();

        System.out.println("⚔️ " + attackerCard.getName() + " declared an attack!");

        // TODO: Ativar efeitos de ataque quando implementarmos sigils
        if (attackerCard instanceof CreatureCard creature) {
            System.out.println("🗡️ " + creature.getName() + " (ATK: " + creature.getAttack() + ") is attacking!");
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, attacker));
    }

    private void handleCreatureDamaged(@NotNull Event event) {
        Card damagedCard = event.getCard();
        Player owner = event.getPlayer();
        Object damageAmount = event.getData();

        if (damagedCard instanceof CreatureCard creature) {
            System.out.println("💢 " + creature.getName() + " received " + damageAmount + " damage points!");
            System.out.println("❤️ Current life: " + creature.getHealth());

            // Atualizar ícone de vida
            creature.changeLifeIcon(Math.max(0, creature.getHealth()));

            // Verificar se morreu
            if (creature.getHealth() <= 0) {
                eventBus.publish(new Event(EventType.CREATURE_DIED, owner, creature));
            }
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, owner));
    }

    private void handleCreatureDied(@NotNull Event event) {
        Card deadCard = event.getCard();
        Player owner = event.getPlayer();

        System.out.println("💀 " + deadCard.getName() + " has died!");

        // Remover do tabuleiro e adicionar ao cemitério
        if (deadCard.getPosLine() != -1 && deadCard.getPosCol() != -1) {
            game.getBoard().removeCard(deadCard.getPosLine(), deadCard.getPosCol());
        }
        owner.getGraveyard().add(deadCard);

        // Gerar ossos
        owner.addBones(1);
        eventBus.publish(new Event(EventType.BONES_GAINED, owner, null, 1));

        // Ativar efeitos de morte quando implementarmos sigils
        if (deadCard instanceof CreatureCard creature) {
            System.out.println("🔮 Death Effects of " + creature.getName() + " will be activated.");
        }

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, owner));
    }

    private void handleDamageDealt(@NotNull Event event) {
        Object damageData = event.getData(); // Pode conter informações detalhadas sobre o dano, por exemplo pode ter um sigil de dano critico
        Player player = event.getPlayer();

        System.out.println("💥 Damage inflicted: " + damageData + " by " + player.getName());

        // Log adicional ou efeitos especiais relacionados ao dano
        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleCombatResolved(@NotNull Event event) {
        Player player = event.getPlayer();

        System.out.println("🏟️ Combat resolved " + player.getName());

        // TODO: Limpar estados temporários de combate
        // Verificar criaturas mortas
        cleanupDeadCreatures();

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === RESOURCES HANDLERS ===

    private void handleBonesGained(@NotNull Event event) {
        Player player = event.getPlayer();
        Object amount = event.getData();

        System.out.println("🦴 " + player.getName() + " gained " + amount + " bones!");
        System.out.println("🦴 Total of: " + player.getBones() + " bones!");

        // Publicar mudança de estado para atualizar UI
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleBonesSpent(@NotNull Event event) {
        Player player = event.getPlayer();
        Object amount = event.getData();

        System.out.println("💸 " + player.getName() + " used " + amount + " bones!");
        System.out.println("🦴 Bones remaining: " + player.getBones());

        // Publicar mudança de estado para atualizar UI
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleLifeLost(@NotNull Event event) {
        Player player = event.getPlayer();

        System.out.println("❤️ " + player.getName() + " lost a candle (life)!");
        System.out.println("💖 Candles remaining: " + player.getLives());

        // Verificar se o jogo acabou
        if (!player.isAlive()) {
            Player winner = (player == game.getPlayer1()) ? game.getPlayer2() : game.getPlayer1();
            eventBus.publish(new Event(EventType.GAME_ENDED, winner));
        } else {
            // Publicar mudança de estado
            eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
        }
    }

    // === SIGIL HANDLERS ===
    private void handleSigilActivated(@NotNull Event event) {
        // Implementar quando o sistema de sigils estiver pronto
        Player player = event.getPlayer();
        Card card = event.getCard();
        Object sigilData = event.getData();

        System.out.println("🔮 Sigil activated: " + sigilData + " from " + card.getName());

        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === GAME HANDLERS ===
    private void handlePlayerAction(@NotNull Event event) {
        Player player = event.getPlayer();
        Object actionData = event.getData();

        System.out.println("🎮 " + player.getName() + " made an action: " + actionData);

        // Log da ação para histórico ou replay
        // Publicar mudança de estado
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleGameStateChanged(@NotNull Event event) {
        Player player = event.getPlayer();

        // Este evento é usado principalmente para notificar a UI sobre mudanças
        System.out.println("🔄 Updated Game State of " + player.getName());

        // TODO: Aqui você pode adicionar lógica para atualizar displays, verificar condições especiais, etc.
    }

    private void handleGameEnded(@NotNull Event event) {
        Player winner = event.getPlayer();

        System.out.println("🏆 GAME OVER!");
        System.out.println("🎉 WINNER: " + winner.getName());

        // TODO: Lógica de fim de jogo
        displayGameResults(winner);

        // Limpar estados temporários
        cleanupGameState();
    }

    // === AUXILIARY METHODS ===


    private void activateEndOfTurnEffects(@NotNull Player player) {
        System.out.println("🌙 Activating end of turn effects " + player.getName());

        Board board = game.getBoard();
        int playerLine = (player.getOrder() == 1) ? 3 : 0;

        for (int col = 0; col < 4; col++) {
            Card card = board.getCard(playerLine, col);
            if (card instanceof CreatureCard creature) {
                System.out.println("🔮 Verifying end of turn effects " + creature.getName());
            }
        }
    }

    private void clearTurnTemporaryEffects(@NotNull Player player) {
        System.out.println("🧹 Cleansing temporary effects " + player.getName());

        // Limpar buffs temporários, estados especiais, etc.
        // TODO: Implementar quando tivermos efeitos temporários como sigils e items
    }

    private void cleanupDeadCreatures() {
        System.out.println("🧹 Cleaning up dead creatures...");

        // Esta lógica já está implementada na GameLogic.cleanupDeadCreatures()
        // Apenas chamamos ela aqui se necessário
    }

    private void displayGameResults(@NotNull Player winner) {
        System.out.println("📊 === END RESULTS ===");
        System.out.println("🏆 WINNER: " + winner.getName());

        Player loser = (winner == game.getPlayer1()) ? game.getPlayer2() : game.getPlayer1();
        System.out.println("💀 LOSER: " + loser.getName() + " (Lives: " + loser.getLives() + ")");

        System.out.println("🦴 Final bones - " + game.getPlayer1().getName() + ": " + game.getPlayer1().getBones() +
                ", " + game.getPlayer2().getName() + ": " + game.getPlayer2().getBones());
        System.out.println("📚 Cards remaining - " + game.getPlayer1().getName() + ": " + game.getPlayer1().getHand().size() +
                ", " + game.getPlayer2().getName() + ": " + game.getPlayer2().getHand().size());
    }

    private void cleanupGameState() {
        System.out.println("🧹 Cleaning up game state...");

        // TODO: Limpar estados temporários, resetar flags, etc.
        // Preparar para um novo jogo se necessário
    }

    // === GETTER PARA EVENTBUS ===
    public EventBus getEventBus() {
        return eventBus;
    }



    // === AUXILIARY METHODS ===

    private void activateOnPlayEffects(@NotNull CreatureCard creature, Player player) {
        // Processar sigils que ativam quando a carta é jogada
        for (Sigil sigil : creature.getSigils()) {
            System.out.println("🔮 Activating sigils: " + sigil.getClass().getSimpleName());
        }
    }

    private void activateStartOfTurnEffects(Player player) {
        // Verificar efeitos de início de turno no tabuleiro
        System.out.println("🌅 Activating start of turn effects ...");
    }


}