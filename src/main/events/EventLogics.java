package events;

import cards.*;
import sigils.*;
import items.*;

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
        eventBus.subscribe(EventType.ITEM_USED, this::handleItemUsed);
    }

    // === TURN HANDLERS ===

    private void handleTurnStarted(Event event) {
        Player player = event.getPlayer();
        System.out.println("🎯" + player.getName() + "'s Turn Started");

        activateStartOfTurnEffects(player);

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleTurnEnded(Event event) {
        Player player = event.getPlayer();
        System.out.println("🏁" + player.getName() + "'s turn ended!");

        if (game.isGameOver()) {
            Player winner = game.getWinner();
            eventBus.publish(new Event(EventType.GAME_ENDED, winner));
            return;
        }
    }

    private void handlePhaseChanged(Event event) {
        Object phaseData = event.getData();
        Player player = event.getPlayer();

        System.out.println("📅 Phase changed " + phaseData + " (Player: " + player.getName() + ")");
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === CARDS HANDLERS ===

    private void handleCardDrawn(Event event) {
        Player player = event.getPlayer();
        Card card = event.getCard();

        System.out.println("📋 " + player.getName() + " drew: " + card.getName());
        System.out.println("📝 " + player.getName() + " now has " + player.getHand().size() + " cards in hand.");

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleCardPlayed(Event event) {
        Player player = event.getPlayer();
        Card card = event.getCard();

        System.out.println("🃏 " + player.getName() + " played " + card.getName());

        if (card instanceof CreatureCard creature) {
            System.out.println("🦎 Creature " + creature.getName() + " was played!");
            creature.changeLifeIcon(creature.getHealth());
            activateOnPlayEffects(creature, player);
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleCardMoved(Event event) {
        Card card = event.getCard();
        Player player = event.getPlayer();

        System.out.println("🔄 " + player.getName() + "'s" + card.getName() + " moved forward ");

        if (card instanceof CreatureCard creature) {
            if (creature.getPosLine() == 1 || creature.getPosLine() == 2) {
                System.out.println("⚔️ " + creature.getName() + " is now in the attack tile!");
            }
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleCardDestroyed(Event event) {
        Card card = event.getCard();
        Player owner = event.getPlayer();

        System.out.println("💥 " + card.getName() + " was destroyed!");
        owner.getGraveyard().add(card);

        if (card instanceof CreatureCard) {
            // owner.addBones(1); // REMOVED: GameLogic already handles this
            eventBus.publish(new Event(EventType.BONES_GAINED, owner, null, 1));
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, owner));
    }

    private void handleCardSacrificed(Event event) {
        Card card = event.getCard();
        Player player = event.getPlayer();

        System.out.println("⚰️ " + card.getName() + " was sacrificed by " + player.getName());

        player.getGraveyard().add(card);
        eventBus.publish(new Event(EventType.BONES_GAINED, player, null, 1));

        if (card instanceof CreatureCard creature) {
            System.out.println("💀 Death effects from " + creature.getName() + " will be activated.");
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === COMBAT HANDLERS ===

    private void handleAttackDeclared(Event event) {
        Card attackerCard = event.getCard();
        Player attacker = event.getPlayer();

        System.out.println("⚔️ " + attackerCard.getName() + " declared an attack!");

        if (attackerCard instanceof CreatureCard creature) {
            System.out.println("🗡️ " + creature.getName() + " (ATK: " + creature.getAttack() + ") is attacking!");
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, attacker));
    }

    private void handleCreatureDamaged(Event event) {
        Card damagedCard = event.getCard();
        Player owner = event.getPlayer();
        Object damageAmount = event.getData();

        if (damagedCard instanceof CreatureCard creature) {
            System.out.println("💢 " + creature.getName() + " received " + damageAmount + " damage points!");
            System.out.println("❤️ Current life: " + creature.getHealth());

            creature.changeLifeIcon(Math.max(0, creature.getHealth()));

            if (creature.getHealth() <= 0) {
                eventBus.publish(new Event(EventType.CREATURE_DIED, owner, creature));
            }
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, owner));
    }

    private void handleCreatureDied(Event event) {
        Card deadCard = event.getCard();
        Player owner = event.getPlayer();

        System.out.println("💀 " + deadCard.getName() + " has died!");

        if (deadCard.getPosLine() != -1 && deadCard.getPosCol() != -1) {
            game.getBoard().removeCard(deadCard.getPosLine(), deadCard.getPosCol());
        }
        owner.getGraveyard().add(deadCard);

        eventBus.publish(new Event(EventType.BONES_GAINED, owner, null, 1));

        if (deadCard instanceof CreatureCard creature) {
            System.out.println("🔮 Death Effects of " + creature.getName() + " will be activated.");
            activateOnDeathEffects(creature, owner);
        }

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, owner));
    }

    private void handleDamageDealt(Event event) {
        Object damageData = event.getData();
        Player player = event.getPlayer();

        System.out.println("💥 Damage inflicted: " + damageData + " by " + player.getName());
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleCombatResolved(Event event) {
        Player player = event.getPlayer();

        System.out.println("🏟️ Combat resolved " + player.getName());
        cleanupDeadCreatures();

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === RESOURCES HANDLERS ===

    private void handleBonesGained(Event event) {
        Player player = event.getPlayer();
        Object amount = event.getData();

        System.out.println("🦴 " + player.getName() + " gained " + amount + " bones!");
        System.out.println("🦴 Total of: " + player.getBones() + " bones!");

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleBonesSpent(Event event) {
        Player player = event.getPlayer();
        Object amount = event.getData();

        System.out.println("💸 " + player.getName() + " used " + amount + " bones!");
        System.out.println("🦴 Bones remaining: " + player.getBones());

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleLifeLost(Event event) {
        Player player = event.getPlayer();

        System.out.println("❤️ " + player.getName() + " lost a candle (life)!");
        System.out.println("💖 Candles remaining: " + player.getLives());

        if (!player.isAlive()) {
            Player winner = (player == game.getPlayer1()) ? game.getPlayer2() : game.getPlayer1();
            eventBus.publish(new Event(EventType.GAME_ENDED, winner));
        } else {
            eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
        }
    }

    // === SIGIL HANDLERS ===

    private void handleSigilActivated(Event event) {
        Player player = event.getPlayer();
        Card card = event.getCard();
        Object sigilData = event.getData();

        System.out.println("🔮 Sigil activated: " + sigilData + " from " + card.getName());
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === GAME HANDLERS ===

    private void handlePlayerAction(Event event) {
        Player player = event.getPlayer();
        Object actionData = event.getData();

        System.out.println("🎮 " + player.getName() + " made an action: " + actionData);
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    private void handleGameStateChanged(Event event) {
        Player player = event.getPlayer();
        System.out.println("🔄 Updated Game State of " + player.getName());
    }

    private void handleGameEnded(Event event) {
        Player winner = event.getPlayer();

        System.out.println("🏆 GAME OVER!");
        System.out.println("🎉 WINNER: " + winner.getName());

        displayGameResults(winner);
        cleanupGameState();
    }

    private void handleItemUsed(Event event) {
        Player player = event.getPlayer();
        Object data = event.getData();

        if (!(data instanceof Items))
            return;

        Items item = (Items) data;
        Card target = event.getCard(); // Target card if any

        System.out.println("🎒 " + player.getName() + " used item: " + item.name());

        switch (item.name()) {
            case "Pliers":
                game.applyPliersDamage(player);
                break;
            case "Scissors":
                if (target != null) {
                    game.destroyCard(target);
                }
                break;
            case "Hook":
                if (target != null) {
                    game.stealCard(target, player);
                }
                break;
            case "HourGlass":
                game.skipOpponentTurn();
                break;
            case "BottledSquirrel":
                game.drawSquirrelFromItem(player);
                break;
            case "HoggyBank":
                game.grantBones(player, 4);
                break;
        }

        // Remove item from player inventory
        player.getItems().remove(item);

        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, player));
    }

    // === AUXILIARY METHODS ===

    private void activateEndOfTurnEffects(Player player) {
        System.out.println("🌙 Activating end of turn effects " + player.getName());
        Board board = game.getBoard();
        int playerLine = (player.getOrder() == 1) ? 3 : 0; // Linha de posicionamento (ou todas?)
        // Na verdade, efeitos podem ocorrer em qualquer carta do jogador

        // Iterar por todas as cartas do jogador no board
        for (int line = 0; line < 4; line++) {
            // Simplificação: verificar apenas linhas do jogador?
            // P1: 2 e 3. P2: 0 e 1.
            boolean isPlayerRow = (player.getOrder() == 1) ? (line >= 2) : (line <= 1);
            if (!isPlayerRow)
                continue;

            for (int col = 0; col < 4; col++) {
                Card card = board.getCard(line, col);
                if (card instanceof CreatureCard creature) {
                    // Aqui chamaria creature.onEndOfTurn() ou algo assim se existisse
                    // Por enquanto, vamos apenas logar e preparar para sigilos
                    for (Sigil sigil : creature.getSigils()) {
                        // Se tivéssemos uma interface para EndOfTurnSigil, checaríamos aqui
                        // if (sigil instanceof EndOfTurnSigil) ((EndOfTurnSigil)sigil).onEndOfTurn();
                    }
                }
            }
        }
    }

    private void clearTurnTemporaryEffects(Player player) {
        System.out.println("🧹 Cleansing temporary effects " + player.getName());
    }

    private void cleanupDeadCreatures() {
        System.out.println("🧹 Cleaning up dead creatures...");
        // GameLogic já faz isso, mas se tiver algo extra de evento, fazemos aqui
    }

    private void displayGameResults(Player winner) {
        System.out.println("📊 === END RESULTS ===");
        System.out.println("🏆 WINNER: " + winner.getName());

        Player loser = (winner == game.getPlayer1()) ? game.getPlayer2() : game.getPlayer1();
        System.out.println("💀 LOSER: " + loser.getName() + " (Lives: " + loser.getLives() + ")");

        System.out.println("🦴 Final bones - " + game.getPlayer1().getName() + ": " + game.getPlayer1().getBones() +
                ", " + game.getPlayer2().getName() + ": " + game.getPlayer2().getBones());
        System.out.println(
                "📚 Cards remaining - " + game.getPlayer1().getName() + ": " + game.getPlayer1().getHand().size() +
                        ", " + game.getPlayer2().getName() + ": " + game.getPlayer2().getHand().size());
    }

    private void cleanupGameState() {
        System.out.println("🧹 Cleaning up game state...");
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void activateOnPlayEffects(CreatureCard creature, Player player) {
        System.out.println("✨ Activating OnPlay effects for " + creature.getName());
        for (Sigil sigil : creature.getSigils()) {
            System.out.println("🔮 Activating sigil (OnPlay): " + sigil.getClass().getSimpleName());
            // if (sigil instanceof OnPlaySigil) ...
        }
    }

    private void activateOnDeathEffects(CreatureCard creature, Player player) {
        System.out.println("☠️ Activating OnDeath effects for " + creature.getName());
        for (Sigil sigil : creature.getSigils()) {
            System.out.println("🔮 Activating sigil (OnDeath): " + sigil.getClass().getSimpleName());
            // if (sigil instanceof OnDeathSigil) ...
        }
    }

    private void activateStartOfTurnEffects(Player player) {
        System.out.println("🌅 Activating start of turn effects for " + player.getName());
        Board board = game.getBoard();
        // Iterar por todas as cartas do jogador no board
        for (int line = 0; line < 4; line++) {
            boolean isPlayerRow = (player.getOrder() == 1) ? (line >= 2) : (line <= 1);
            if (!isPlayerRow)
                continue;

            for (int col = 0; col < 4; col++) {
                Card card = board.getCard(line, col);
                if (card instanceof CreatureCard creature) {
                    for (Sigil sigil : creature.getSigils()) {
                        // if (sigil instanceof StartOfTurnSigil) ...
                    }
                }
            }
        }
    }
}
