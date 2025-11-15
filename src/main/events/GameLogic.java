package events;

import cards.Board;
import cards.Card;
import cards.Deck;
import cards.Player;
import cards.CreatureCard;

public class GameLogic {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Deck deckP1;
    private Deck deckP2;

    public GameLogic(Board board, Player player1, Player player2, Deck deckP1, Deck deckP2) {
        this.board = board;
        this.player1 = player1;
        this.player2 = player2;
        this.deckP1 = deckP1;
        this.deckP2 = deckP2;
        this.currentPlayer = player1; // Player 1 começa
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
        initializePlayerHand(player2, deckP2);
        System.out.println("Jogo inicializado!");
        printGameState();
    }

    // Compra uma carta do deck
    public boolean drawCard(Player player, Deck deck) {
        if (deck.getRemainingCards() > 0) {
            deck.draw(player.getHand());
            return true;
        }
        return false;
    }


    public boolean placeCardFromCurrentPlayerHand(int handIndex, int column) {
        return placeCardOnBoard(currentPlayer, handIndex, column);
    }


    // Coloca uma carta no tabuleiro (zona de posicionamento)
    public boolean placeCardOnBoard(Player player, int handIndex, int column) {
        if (handIndex < 0 || handIndex >= player.getHand().size()) {
            return false;
        }

        Card card = player.getHand().get(handIndex);

        // Verifica se é uma CreatureCard
        if (!(card instanceof CreatureCard)) {
            return false;
        }

        CreatureCard creature = (CreatureCard) card;

        // Verifica se pode pagar o custo
        if (!canPayCost(player, creature)) {
            return false;
        }

        // Tenta colocar no tabuleiro
        if (board.placeCard(card, column, player.getOrder())) {
            // Remove da mão e paga o custo
            player.removeCardFromHand(handIndex);
            payCost(player, creature);
            return true;
        }

        return false;
    }

    // Verifica se o jogador pode pagar o custo da carta
    private boolean canPayCost(Player player, CreatureCard card) {
        // Verifica custo de sangue (sacrifícios)
        if (card.getBloodCost() > 0) {
            int sacrificeableCards = countSacrificeableCards(player);
            if (sacrificeableCards < card.getBloodCost()) {
                return false;
            }
        }

        // Verifica custo de ossos
        if (card.getBonesCost() > 0) {
            if (player.getBones() < card.getBonesCost()) {
                return false;
            }
        }

        return true;
    }

    // Conta cartas que podem ser sacrificadas (na zona de posicionamento)
    private int countSacrificeableCards(Player player) {
        int count = 0;
        int line = (player.getOrder() == 1) ? 3 : 0;

        for (int col = 0; col < 4; col++) {
            if (!board.EmptySpace(line, col)) {
                count++;
            }
        }
        return count;
    }

    // Paga o custo da carta
    private void payCost(Player player, CreatureCard card) {
        // Paga custo de ossos
        if (card.getBonesCost() > 0) {
            player.spendBones(card.getBonesCost());
        }

        // Paga custo de sangue (sacrifica cartas)
        if (card.getBloodCost() > 0) {
            sacrificeCards(player, card.getBloodCost());
        }
    }

    // Sacrifica cartas para pagar o custo de sangue
    public boolean sacrificeCards(Player player, int amount) {
        int line = (player.getOrder() == 1) ? 3 : 0;
        int sacrificed = 0;



        for (int col = 0; col < 4 && sacrificed < amount; col++) {
            if (!board.EmptySpace(line, col)) {
                Card card = board.removeCard(line, col);
                player.getGraveyard().add(card);

                // Adiciona ossos se for uma CreatureCard
                if (card instanceof CreatureCard) {
                    player.addBones(1);
                }
                sacrificed++;
            }
        }

        return sacrificed == amount;
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

            if (attackerCard != null && attackerCard instanceof CreatureCard) {
                CreatureCard attackingCreature = (CreatureCard) attackerCard;
                performAttack(attackingCreature, defender, col);
            }
        }

        // Remove criaturas mortas após todos os ataques
        cleanupDeadCreatures();
    }

    // Realiza o ataque de uma criatura específica
    private void performAttack(CreatureCard attacker, Player defender, int column) {
        int attackerLine = attacker.getPosLine();
        int attackerCol = attacker.getPosCol();

        // Verifica se tem sigilo de voo (FlySigil)
        boolean hasFlying = attacker.getSigils().stream()
                .anyMatch(s -> s.getClass().getSimpleName().equals("FlySigil"));

        // Busca carta na linha de ataque oposta
        Card oppositeAttackCard = board.getOppositeAttackCard(attackerLine, attackerCol);

        if (oppositeAttackCard != null && !hasFlying) {
            // Ataque bloqueado - criaturas lutam entre si
            if (oppositeAttackCard instanceof CreatureCard) {
                CreatureCard blocker = (CreatureCard) oppositeAttackCard;
                resolveCombat(attacker, blocker);
            }
        } else {
            // Ataque direto ao oponente ou carta voa por cima do bloqueador
            Card oppositeDefenseCard = board.getOppositeDefenseCard(attackerLine, attackerCol);

            if (oppositeDefenseCard != null && !hasFlying) {
                // Ataca a carta de defesa diretamente
                if (oppositeDefenseCard instanceof CreatureCard) {
                    CreatureCard defenseCreature = (CreatureCard) oppositeDefenseCard;
                    defenseCreature.takeDamage(attacker.getAttack());
                    defenseCreature.changeLifeIcon(Math.max(0, defenseCreature.getHealth()));
                }
            } else {
                // Dano direto ao jogador
                int damage = attacker.getAttack();
                for (int i = 0; i < damage; i++) {
                    defender.loseLife();
                }
                System.out.println(defender.getName() + " levou " + damage + " de dano! Vidas restantes: " + defender.getLives());
            }
        }
    }

    // Resolve o combate entre duas criaturas
    private void resolveCombat(CreatureCard attacker, CreatureCard defender) {
        System.out.println("Combate: " + attacker.getName() + " (" + attacker.getAttack() + "/" + attacker.getHealth() +
                ") vs " + defender.getName() + " (" + defender.getAttack() + "/" + defender.getHealth() + ")");

        // Ambas as criaturas causam dano uma à outra
        attacker.takeDamage(defender.getAttack());
        defender.takeDamage(attacker.getAttack());

        // Atualiza ícones de vida
        attacker.changeLifeIcon(Math.max(0, attacker.getHealth()));
        defender.changeLifeIcon(Math.max(0, defender.getHealth()));

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

                        System.out.println(creature.getName() + " foi destruída!");
                    }
                }
            }
        }
    }

    // Verifica se o jogo acabou
    public boolean isGameOver() {
        return !player1.isAlive() || !player2.isAlive();
    }

    // Retorna o vencedor do jogo
    public Player getWinner() {
        if (!isGameOver()) {
            return null;
        }
        return player1.isAlive() ? player1 : player2;
    }

    // Troca o turno do jogador
    public void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        System.out.println("\n=== Turno de " + currentPlayer.getName() + " ===");

        // Compra uma carta no início do turno
        Deck currentDeck = (currentPlayer == player1) ? deckP1 : deckP2;

    }

    // Fase de preparação do turno
    public void startTurnPhase(Player player) {
        System.out.println("\n--- Início do turno de " + player.getName() + " ---");
        System.out.println("Vidas: " + player.getLives());
        System.out.println("Ossos: " + player.getBones());
        System.out.println("Cartas na mão: " + player.getHand().size());
    }

    // Executar turno completo (simplificado para testes)
    public void executeTurn() {
        startTurnPhase(currentPlayer);

        // Aqui você pode adicionar lógica de IA ou esperar input do jogador
        // Por enquanto, apenas executa a fase de ataque

        executeAttackPhase(currentPlayer);

        if (!isGameOver()) {
            switchTurn();
        }
    }

    // Compra do deck "normal" (criaturas, etc.)
    public boolean drawFromMainDeck(Player player, Deck deck) {
        int before = player.getHand().size();
        deck.draw(player.getHand());      // já existe esse método
        return player.getHand().size() > before;
    }

    // Compra do deck de Esquilos
    public boolean drawFromSquirrelDeck(Player player, Deck deck) {
        int before = player.getHand().size();
        deck.drawSquirrel(player.getHand());  // já existe esse método
        return player.getHand().size() > before;
    }


    // Compra do deck normal do jogador atual
    public boolean drawFromMainDeckCurrentPlayer() {
        Deck currentDeck = (currentPlayer == player1) ? deckP1 : deckP2;
        return drawFromMainDeck(currentPlayer, currentDeck);
    }

    // Compra do deck de esquilos do jogador atual
    public boolean drawFromSquirrelDeckCurrentPlayer() {
        Deck currentDeck = (currentPlayer == player1) ? deckP1 : deckP2;
        return drawFromSquirrelDeck(currentPlayer, currentDeck);
    }


    // Getters
    public Board getBoard() { return board; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Deck getDeckP1() { return deckP1; }
    public Deck getDeckP2() { return deckP2; }

    // Metodo auxiliar para debug
    public void printGameState() {
        System.out.println("\n========== ESTADO DO JOGO ==========");
        System.out.println("Turno atual: " + currentPlayer.getName());
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