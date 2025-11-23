package events;

import cards.Board;
import cards.Card;
import cards.Deck;
import cards.Player;
import cards.CreatureCard;
import cards.Scale; // NOVO IMPORT

public class GameLogic {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Deck deckP1;
    private Deck deckP2;
    private EventBus eventBus; // EventBus injected

    // ANTES: private int gameScale = 0;
    // AGORA: balança separada, classe em cards
    private Scale scale = new Scale();
    // 0 = Neutro
    // +5 = Player 1 vence
    // -5 = Player 2 vence

    // Controle de compra de carta por turno
    private boolean hasDrawnThisTurn = false;

    public GameLogic(Board board, Player player1, Player player2, Deck deckP1, Deck deckP2, EventBus eventBus) {
        this.board = board;
        this.player1 = player1;
        this.player2 = player2;
        this.deckP1 = deckP1;
        this.deckP2 = deckP2;
        this.currentPlayer = player1; // Player 1 começa
        this.eventBus = eventBus;

        // ANTES: this.gameScale = 0;
        // AGORA: já começa em 0 dentro de GameScale
        this.scale.reset();
    }

    // Inicializa a mão do jogador com cartas do deck
    public void initializePlayerHand(Player player, Deck deck) {
        deck.shuffle();
        // Cada jogador começa com 3 cartas + 1 squirrel
        deck.draw(3, player.getHand());
        deck.drawSquirrel(player.getHand());
    }

    // Inicializa ambos os jogadores
    public void initializeBothPlayers() {
        initializePlayerHand(player1, deckP1);
        // Player 2 starts with 1 extra card (4 cards + 1 squirrel) for balance
        deckP2.shuffle();
        deckP2.draw(4, player2.getHand());
        deckP2.drawSquirrel(player2.getHand());
        System.out.println("Jogo inicializado!");
        printGameState();
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, currentPlayer));
    }

    // Compra uma carta do deck (metodo antigo, genérico)
    public boolean drawCard(Player player, Deck deck) {
        if (deck.getRemainingCards() > 0) {
            deck.draw(player.getHand());
            return true;
        }
        return false;
    }

    // ===========PLACE CARD

    public enum PlaceCardResult {
        SUCCESS,
        INVALID_SPACE, // tentou colocar em zona errada (linha de ataque, lado inimigo etc)
        NOT_IN_CURRENT_HAND, // índice não está na mão do jogador atual
        NOT_A_CREATURE, // carta não é criatura
        NOT_ENOUGH_SACRIFICES,
        NOT_ENOUGH_BONES,
        SLOT_OCCUPIED, // já tem carta naquela coluna da sua linha de posicionamento
        REQUIRES_SACRIFICE_SELECTION // Novo: Indica à UI que o modo interativo é necessário
    }

    // helper de consulta para a UI saber se dá pra pagar custo de sangue
    public boolean canPayBloodCostWithCurrentBoard(CreatureCard card) {
        int bloodCost = card.getBloodCost();
        if (bloodCost <= 0) {
            return true;
        }
        int available = countSacrificeableCards(currentPlayer);
        return available >= bloodCost;
    }

    // Este metodo agora lida APENAS com cartas de custo 0 (sangue) ou custo de
    // ossos.
    // Se a carta tiver custo de sangue, ele retorna REQUIRES_SACRIFICE_SELECTION.
    public PlaceCardResult tryPlaceCardFromCurrentPlayerHand(int handIndex, int targetLine, int targetCol) {

        // 1 Verifica se o alvo é uma zona de posicionamento do jogador atual
        Board.SpaceType spaceType = board.getSpaceType(targetLine, targetCol);

        if (currentPlayer.getOrder() == 1) {
            if (spaceType != Board.SpaceType.PLAYER_1_POSITIONING) {
                return PlaceCardResult.INVALID_SPACE;
            }
        } else { // order == 2
            if (spaceType != Board.SpaceType.PLAYER_2_POSITIONING) {
                return PlaceCardResult.INVALID_SPACE;
            }
        }

        // 2 Verifica se a carta está na mão real do jogador atual
        if (handIndex < 0 || handIndex >= currentPlayer.getHand().size()) {
            return PlaceCardResult.NOT_IN_CURRENT_HAND;
        }

        Card baseCard = currentPlayer.getHand().get(handIndex);
        if (!(baseCard instanceof CreatureCard creature)) {
            return PlaceCardResult.NOT_A_CREATURE;
        }

        // 3 Verifica se o slot está vazio na linha de posicionamento do player
        int linePos = (currentPlayer.getOrder() == 1) ? 3 : 0;
        if (!board.EmptySpace(linePos, targetCol)) {
            return PlaceCardResult.SLOT_OCCUPIED;
        }

        // 4 Verifica custo com mais detalhe
        int bloodCost = creature.getBloodCost();
        int bonesCost = creature.getBonesCost();

        // 4a Custo de Ossos (pode pagar direto)
        if (bonesCost > 0 && currentPlayer.getBones() < bonesCost) {
            return PlaceCardResult.NOT_ENOUGH_BONES;
        }

        // 4b Custo de Sangue
        if (bloodCost > 0) {
            // Verifica se o jogador TEM cartas suficientes no total
            int sacrificeable = countSacrificeableCards(currentPlayer);
            if (sacrificeable < bloodCost) {
                return PlaceCardResult.NOT_ENOUGH_SACRIFICES;
            }
            // Se ele tem, mas não as selecionou, dizemos à UI para iniciar o processo
            return PlaceCardResult.REQUIRES_SACRIFICE_SELECTION;
        }

        // 5 Paga o custo (apenas ossos, já que bloodCost é 0)
        payCost(currentPlayer, creature, null); // Passa null para sacrifícios

        // 6 Coloca de fato no board (usa a API atual baseada em playerOrder)
        boolean placed = board.placeCard(baseCard, targetCol, currentPlayer.getOrder());
        if (!placed) {
            return PlaceCardResult.SLOT_OCCUPIED;
        } else {
            System.out.println(
                    "Carta adicionada em jogo: " + creature.getName() +
                            " | Posição: linha " + creature.getPosLine() +
                            ", coluna " + creature.getPosCol());
            board.printBoard();
            creature.setJustPlayed(true); // Mark as just played
            eventBus.publish(new Event(EventType.CARD_PLAYED, currentPlayer, creature));
        }

        // 7 Remove da mão
        currentPlayer.removeCardFromHand(handIndex);

        return PlaceCardResult.SUCCESS;
    }

    // Conta cartas que podem ser sacrificadas (na zona de posicionamento)
    public int countSacrificeableCards(Player player) {
        int count = 0;
        int line = (player.getOrder() == 1) ? 3 : 0;
        int attackLine = (player.getOrder() == 1) ? 2 : 1;

        // Check positioning row
        for (int col = 0; col < 4; col++) {
            if (!board.EmptySpace(line, col)) {
                count++;
            }
        }
        // Check attack row
        for (int col = 0; col < 4; col++) {
            if (!board.EmptySpace(attackLine, col)) {
                count++;
            }
        }
        System.out.println("🔍 Criaturas sacrificáveis para " + player.getName() + ": " + count);
        return count;
    }

    // MODIFICADO: Paga o custo da carta
    private void payCost(Player player, CreatureCard card, java.util.List<CreatureCard> sacrifices) {
        // Paga custo de ossos
        if (card.getBonesCost() > 0) {
            player.spendBones(card.getBonesCost());
            eventBus.publish(new Event(EventType.BONES_SPENT, player, null, card.getBonesCost()));
        }

        // Paga custo de sangue (sacrifica cartas da lista)
        if (card.getBloodCost() > 0 && sacrifices != null) {
            sacrificeCards(player, sacrifices);
        }
    }

    // Sacrifica uma lista específica de cartas
    public void sacrificeCards(Player player, java.util.List<CreatureCard> cardsToSacrifice) {
        for (Card card : cardsToSacrifice) {
            // Remove do tabuleiro
            board.removeCard(card.getPosLine(), card.getPosCol());

            // Adiciona ao cemitério
            player.getGraveyard().add(card);

            // Adiciona ossos se for uma CreatureCard
            if (card instanceof CreatureCard) {
                player.addBones(1);
                eventBus.publish(new Event(EventType.CARD_SACRIFICED, player, card));
            }
        }
        System.out.println("Sacrificadas " + cardsToSacrifice.size() + " criaturas.");
    }

    // Chamado pela UI após a seleção de sacrifícios
    public PlaceCardResult tryPlaceCardWithSacrifices(
            int handIndex,
            int targetLine,
            int targetCol,
            java.util.List<CreatureCard> sacrifices) {

        // 1 Verifica se o alvo é um slot de posicionamento válido
        Board.SpaceType spaceType = board.getSpaceType(targetLine, targetCol);
        if (currentPlayer.getOrder() == 1) {
            if (spaceType != Board.SpaceType.PLAYER_1_POSITIONING)
                return PlaceCardResult.INVALID_SPACE;
        } else {
            if (spaceType != Board.SpaceType.PLAYER_2_POSITIONING)
                return PlaceCardResult.INVALID_SPACE;
        }

        // 2 Verifica a carta na mão
        if (handIndex < 0 || handIndex >= currentPlayer.getHand().size()) {
            return PlaceCardResult.NOT_IN_CURRENT_HAND;
        }
        Card baseCard = currentPlayer.getHand().get(handIndex);
        if (!(baseCard instanceof CreatureCard creature)) {
            return PlaceCardResult.NOT_A_CREATURE;
        }

        // 3 Verifica Custo
        int bloodCost = creature.getBloodCost();
        int bonesCost = creature.getBonesCost();

        // 3a O número de sacrifícios bate com o custo?
        if (sacrifices == null || sacrifices.size() != bloodCost) {
            return PlaceCardResult.NOT_ENOUGH_SACRIFICES;
        }

        // 3b Custo de Ossos
        if (bonesCost > 0 && currentPlayer.getBones() < bonesCost) {
            return PlaceCardResult.NOT_ENOUGH_BONES;
        }

        // 4 VERIFICAÇÃO DE SLOT
        // O slot de destino é válido se:
        // a) Ele já está vazio
        // b) Ele contém uma das cartas que SERÁ sacrificada

        Card cardAtTarget = board.getCard(targetLine, targetCol);
        boolean targetIsSacrifice = false;
        if (cardAtTarget != null) {
            for (CreatureCard sac : sacrifices) {
                // Compara se é o mesmo objeto de carta
                if (sac == cardAtTarget) {
                    targetIsSacrifice = true;
                    break;
                }
            }
        }

        // Se o slot NÃO está vazio (cardAtTarget != null) E
        // a carta lá NÃO é um dos sacrifícios (!targetIsSacrifice),
        // então o slot está ocupado por outra carta.
        if (cardAtTarget != null && !targetIsSacrifice) {
            return PlaceCardResult.SLOT_OCCUPIED;
        }

        // 5 Paga o custo (agora temos a lista de sacrifícios)
        payCost(currentPlayer, creature, sacrifices);

        // 6 Coloca de fato no board
        // (A lógica de payCost já removeu as cartas, então o slot está livre)
        boolean placed = board.placeCard(baseCard, targetCol, currentPlayer.getOrder());
        if (!placed) {
            // Isso não deveria acontecer se a lógica estiver correta
            return PlaceCardResult.SLOT_OCCUPIED;
        } else {
            System.out.println(
                    "Carta (com sacrifício) adicionada em jogo: " + creature.getName() +
                            " | Posição: linha " + creature.getPosLine() +
                            ", coluna " + creature.getPosCol());
            board.printBoard();
            creature.setJustPlayed(true); // Mark as just played
            eventBus.publish(new Event(EventType.CARD_PLAYED, currentPlayer, creature));
        }

        // 7 Remove da mão
        currentPlayer.removeCardFromHand(handIndex);

        return PlaceCardResult.SUCCESS;
    }

    /**
     * Executa todas as ações de final de turno para o jogador atual:
     * 1. Cartas na linha de posicionamento tentam mover para a linha de ataque.
     * 2. Cartas na linha de ataque (incluindo as que acabaram de mover) atacam.
     * 3. Cartas mortas são removidas (isso é feito dentro de executeAttackPhase).
     */
    public void performEndOfTurnActions(Player attacker) {

        // === 1. FASE DE MOVIMENTO ===
        // Cartas da linha de posicionamento andam para a de ataque
        System.out.println("Movendo cartas para a linha de ataque...");

        int positioningLine = (attacker.getOrder() == 1) ? 3 : 0;
        int attackLine = (attacker.getOrder() == 1) ? 2 : 1;

        for (int col = 0; col < 4; col++) {
            Card cardToMove = board.getCard(positioningLine, col);

            // Só move se tiver uma carta na pos. e o espaço de atk estiver livre
            if (cardToMove != null && board.EmptySpace(attackLine, col)) {
                if (cardToMove instanceof CreatureCard creature && creature.isJustPlayed()) {
                    System.out.println(creature.getName() + " acabou de ser jogada e aguarda um turno.");
                    creature.setJustPlayed(false); // Will move next turn
                    continue;
                }

                // O metodo moveCardToAttack chama board.moveToAttack
                boolean moved = moveCardToAttack(attacker, col);
                if (moved && cardToMove instanceof CreatureCard) {
                    System.out.println(cardToMove.getName() + " moveu para a linha de ataque.");
                    eventBus.publish(new Event(EventType.CARD_MOVED, attacker, cardToMove));
                }
            } else if (cardToMove != null) {
                System.out.println(cardToMove.getName() + " está bloqueado e não pode avançar.");
            }
        }

        // Opcional: Imprimir o board após os movimentos para debug
        System.out.println("Tabuleiro após movimentos:");
        board.printBoard();

        // === 2. FASE DE ATAQUE E LIMPEZA ===
        System.out.println("Executando ataques...");
        executeAttackPhase(attacker);
    }

    // Move uma carta da zona de posicionamento para zona de ataque
    public boolean moveCardToAttack(Player player, int column) {
        return board.moveToAttack(column, player.getOrder());
    }

    // Executa a fase de ataque
    public void executeAttackPhase(Player attacker) {
        int attackLine = (attacker.getOrder() == 1) ? 2 : 1;
        Player defender = (attacker == player1) ? player2 : player1;

        for (int col = 0; col < 4; col++) {
            Card attackerCard = board.getCard(attackLine, col);

            if (attackerCard instanceof CreatureCard) {
                CreatureCard attackingCreature = (CreatureCard) attackerCard;

                // ANTES: performAttack(attackingCreature, defender, col);
                // AGORA: passamos também o Player atacante para atualizar a balança
                performAttack(attackingCreature, attacker, defender, col);
            }
        }

        // Remove criaturas mortas após todos os ataques
        cleanupDeadCreatures();
    }

    // Realiza o ataque de uma criatura específica
    // ANTES: private void performAttack(CreatureCard attacker, Player defender, int
    // column)
    // AGORA: também recebe o Player atacante
    private void performAttack(CreatureCard attacker, Player attackerPlayer, Player defender, int column) {
        int attackerLine = attacker.getPosLine();
        int attackerCol = attacker.getPosCol();

        eventBus.publish(new Event(EventType.ATTACK_DECLARED, attackerPlayer, attacker));

        // Verifica se tem sigilo de voo (FlySigil)
        boolean hasFlying = attacker.getSigils().stream()
                .anyMatch(s -> s.getClass().getSimpleName().equals("FlySigil"));

        // Vamos calcular as linhas opostas manualmente
        int oppositeAttackLine;
        int oppositeDefenseLine;

        if (attackerLine == 2) { // Atacante é P1
            oppositeAttackLine = 1;
            oppositeDefenseLine = 0;
        } else { // Atacante é P2 (linha 1)
            oppositeAttackLine = 2;
            oppositeDefenseLine = 3;
        }

        // 1. Busca carta na linha de ATAQUE oposta
        Card oppositeAttackCard = board.getCard(oppositeAttackLine, attackerCol);

        if (oppositeAttackCard != null && !hasFlying) {
            // Ataque bloqueado - criaturas lutam entre si
            System.out.println("⚔️ Ataque bloqueado na linha de frente (Linha " + oppositeAttackLine + ")");
            if (oppositeAttackCard instanceof CreatureCard) {
                CreatureCard blocker = (CreatureCard) oppositeAttackCard;
                resolveCombat(attacker, blocker);
            }
        } else {
            // Ataque direto ao oponente (ignora retaguarda)
            int damage = attacker.getAttack();
            System.out.println(defender.getName() + " levou " + damage + " de dano direto na balança!");
            eventBus.publish(new Event(EventType.DAMAGE_DEALT, attackerPlayer, null, damage));

            // Agora o dano mexe APENAS na balança
            updateGameScale(attackerPlayer, damage);
        }
    }

    // Resolve o combate entre duas criaturas
    private void resolveCombat(CreatureCard attacker, CreatureCard defender) {
        System.out.println("Combate: " + attacker.getName() + " (" + attacker.getAttack() + "/" + attacker.getHealth() +
                ") vs " + defender.getName() + " (" + defender.getAttack() + "/" + defender.getHealth() + ")");

        // Apenas o atacante causa dano ao defensor
        defender.takeDamage(attacker.getAttack());

        // Atualiza ícones de vida
        defender.changeLifeIcon(Math.max(0, defender.getHealth()));
        // Assumindo player2 como defensor aqui, mas idealmente passaria o dono
        eventBus.publish(new Event(EventType.CREATURE_DAMAGED, player2, defender, attacker.getAttack()));

        System.out.println("Após combate: " + attacker.getName() + " HP: " + attacker.getHealth() +
                ", " + defender.getName() + " HP: " + defender.getHealth());
    }

    // Remove criaturas mortas do tabuleiro
    private void cleanupDeadCreatures() {
        for (int line = 0; line < 4; line++) {
            for (int col = 0; col < 4; col++) {
                Card card = board.getCard(line, col);

                if (card instanceof CreatureCard) {
                    CreatureCard creature = (CreatureCard) card;
                    if (creature.getHealth() <= 0) {
                        // Remove do tabuleiro
                        board.removeCard(line, col);

                        // Adiciona ao cemitério do jogador apropriado
                        Player owner = (line >= 2) ? player1 : player2;
                        owner.getGraveyard().add(creature);
                        owner.addBones(1); // Adiciona 1 osso quando criatura morre
                        eventBus.publish(new Event(EventType.CREATURE_DIED, owner, creature));

                        System.out.println(creature.getName() + " foi destruída!");
                    }
                }
            }
        }
    }

    // Verifica se o jogo acabou
    public boolean isGameOver() {
        // considera as vidas dos jogadores
        return !player1.isAlive() || !player2.isAlive();
    }

    // Retorna o vencedor do jogo
    public Player getWinner() {
        if (!isGameOver()) {
            return null;
        }

        // Quem ainda estiver vivo é o vencedor
        if (player1.isAlive() && !player2.isAlive()) {
            return player1;
        } else if (player2.isAlive() && !player1.isAlive()) {
            return player2;
        }

        // Empate esquisito: os dois morreram ao mesmo tempo
        return null;
    }

    /**
     * MODIFICADO: Este metodo agora APENAS executa as ações de fim de turno
     * (movimento e ataque) do jogador atual.
     * Ele NÃO troca mais o jogador.
     */
    public void executeEndOfTurn() {
        System.out.println("\n--- Fase de Ações de " + currentPlayer.getName() + " ---");
        performEndOfTurnActions(currentPlayer);
    }

    /**
     * NOVO: Este método finaliza o turno trocando o jogador e resetando
     * os controles de compra de carta.
     */
    public void switchToNextPlayer() {
        eventBus.publish(new Event(EventType.TURN_ENDED, currentPlayer));

        // muda player
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        System.out.println("\n=== Turno de " + currentPlayer.getName() + " ===");

        hasDrawnThisTurn = false; // Reseta o controle de compra
        eventBus.publish(new Event(EventType.TURN_STARTED, currentPlayer));
    }

    /**
     * Atualiza a balança do jogo quando um jogador leva dano direto.
     * Se a balança chegar a +5 ou -5, o jogador oposto perde 1 vida
     * e a balança é resetada para 0.
     */
    private void updateGameScale(Player attacker, int damage) {
        // Atualiza a balança normalmente
        scale.applyDirectDamage(attacker, damage);
        eventBus.publish(new Event(EventType.GAME_STATE_CHANGED, attacker));

        // Se a balança está pendendo para o lado do Player 1 (+5),
        // quem perde vida é o Player 2.
        if (scale.reachedPlayer1Win()) {
            player2.loseLife();
            System.out.println(
                    "⚠ " + player2.getName() + " perdeu 1 vida pela balança! Vidas restantes: " + player2.getLives());
            eventBus.publish(new Event(EventType.LIFE_LOST, player2));
            scale.reset(); // volta para 0

            if (!player2.isAlive()) {
                System.out.println("FIM DE JOGO! " + player1.getName() + " venceu!");
                eventBus.publish(new Event(EventType.GAME_ENDED, player1));
            }
        }

        // Se a balança está pendendo para o lado do Player 2 (-5),
        // quem perde vida é o Player 1.
        if (scale.reachedPlayer2Win()) {
            player1.loseLife();
            System.out.println(
                    "⚠ " + player1.getName() + " perdeu 1 vida pela balança! Vidas restantes: " + player1.getLives());
            eventBus.publish(new Event(EventType.LIFE_LOST, player1));
            scale.reset(); // volta para 0

            if (!player1.isAlive()) {
                System.out.println("FIM DE JOGO! " + player2.getName() + " venceu!");
                eventBus.publish(new Event(EventType.GAME_ENDED, player2));
            }
        }
    }

    // Fase de preparação do turno
    public void startTurnPhase(Player player) {
        System.out.println("\n--- Início do turno de " + player.getName() + " ---");
        System.out.println("Vidas: " + player.getLives());
        System.out.println("Ossos: " + player.getBones());
        System.out.println("Cartas na mão: " + player.getHand().size());
    }

    // enums de draw
    public enum DrawResult {
        SUCCESS,
        ALREADY_DREW_THIS_TURN,
        DECK_EMPTY
    }

    public GameLogic.DrawResult drawFromMainDeckCurrentPlayer() {
        Deck currentDeck = (currentPlayer == player1) ? deckP1 : deckP2;

        if (hasDrawnThisTurn)
            return DrawResult.ALREADY_DREW_THIS_TURN; // já comprou

        if (currentDeck.getRemainingCards() <= 0)
            return DrawResult.DECK_EMPTY; // deck zerado

        int before = currentPlayer.getHand().size();
        Card drawn = currentDeck.draw(currentPlayer.getHand()); // compra
        hasDrawnThisTurn = true;

        if (currentPlayer.getHand().size() > before) {
            eventBus.publish(new Event(EventType.CARD_DRAWN, currentPlayer, drawn));
            return DrawResult.SUCCESS;
        } else {
            return DrawResult.DECK_EMPTY;
        }
    }

    public GameLogic.DrawResult drawFromSquirrelDeckCurrentPlayer() {
        Deck currentDeck = (currentPlayer == player1) ? deckP1 : deckP2;

        if (hasDrawnThisTurn)
            return DrawResult.ALREADY_DREW_THIS_TURN;

        int before = currentPlayer.getHand().size();
        Card drawn = currentDeck.drawSquirrel(currentPlayer.getHand()); // compra esquilo
        hasDrawnThisTurn = true;

        if (currentPlayer.getHand().size() > before) {
            eventBus.publish(new Event(EventType.CARD_DRAWN, currentPlayer, drawn));
            return DrawResult.SUCCESS;
        } else {
            return DrawResult.DECK_EMPTY;
        }
    }

    // Getters
    public Board getBoard() {
        return board;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Deck getDeckP1() {
        return deckP1;
    }

    public Deck getDeckP2() {
        return deckP2;
    }

    public boolean hasDrawnThisTurn() {
        return hasDrawnThisTurn;
    }

    // ANTES: retornava o int direto
    // public int getGameScale(){ return gameScale; }

    // AGORA: expõe o valor da balança (UI continua enxergando um int)
    public int getGameScale() {
        return scale.getValue();
    }

    // Metodo auxiliar para debug
    public void printGameState() {
        System.out.println("\n========== ESTADO DO JOGO ==========");
        System.out.println("Turno atual: " + currentPlayer.getName());
        System.out.println("Balança: " + scale.getValue());
        System.out.println("\nPlayer 1 (" + player1.getName() + "):");
        System.out.println("  Vidas: " + player1.getLives());
        System.out.println("  Ossos: " + player1.getBones());
        System.out.println("  Mão: " + player1.getHand().size() + " cartas");
        System.out.println("  Cemitério: " + player1.getGraveyard().size() + " cartas");

        System.out.println("\nPlayer 2 (" + player2.getName() + "):");
        System.out.println("  Vidas: " + player2.getLives());
        System.out.println("  Ossos: " + player2.getBones());
        System.out.println("  Mão: " + player2.getHand().size() + " cartas");
        System.out.println("  Cemitério: " + player2.getGraveyard().size() + " cartas");

        board.printBoard();
        System.out.println("====================================\n");
    }
}
