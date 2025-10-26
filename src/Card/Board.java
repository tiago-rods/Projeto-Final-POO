public class Board {
    // Constants for board dimensions
    private static final int width = 4;
    private static final int height = 4;

    // Matrix representing the board [row][column]
    private Card[][] board;

    // Enum -> Naming the types of spaces on the game board
    // Enum to identify the board zones
    public enum SpaceType {
        PLAYER_1_POSITIONING,  // Col 0
        ATTACK_PLAYER1,          // Col 1
        ATTACK_PLAYER2,          // Col 2
        PLAYER_2_POSITIONING   // Col 3
    }

    public Board() {
        this.board = new Card[height][width];
        initializeBoard();
    }

    private void initializeBoard() {
        // Initializes all spaces to empty (null)
        for (int line = 0; line < height; line++) {
            for (int col = 0; col < width; col++) {
                board[line][col] = null;
            }
        }
    }

    // Methods for identifying the type of space
    public SpaceType getSpaceType(int line, int col) {
        switch (col) {
            case 0: return SpaceType.PLAYER_1_POSITIONING;
            case 1: return SpaceType.ATTACK_PLAYER1;
            case 2: return SpaceType.ATTACK_PLAYER2;
            case 3: return SpaceType.PLAYER_2_POSITIONING;
            default: throw new IllegalArgumentException("Invalid Column: " + col);
        }
    }

    // Checks if a position is empty
    public boolean EmptySpace(int line, int col) {
        validatePosition(line, col);
        return board[line][col] == null;
    }

    // Place a card in the placement space
    public boolean placeCard(Card card, int linePos, int player) {
        int colPos = (player == 1) ? 0 : 3;

        if (EmptySpace(linePos, colPos)) {
            board[linePos][colPos] = card;
            card.setPos(linePos, colPos);
            return true;
        }
        return false;
    }

    // Move card do espaço de posicionamento para espaço de ataque
    public boolean moveToAttack(int line, int player) {
        int colPos = (player == 1) ? 0 : 3;
        int colAttack = (player == 1) ? 1 : 2;

        // Checks if there is a card in the position and if the attack slot is empty
        if (!EmptySpace(line, colPos) && EmptySpace(line, colAttack)) {
            // aux variable, current position is now NULL, card goes to attack position
            Card card = board[line][colPos];
            board[line][colPos] = null;
            board[line][colAttack] = card;
            card.setPos(line, colAttack);
            return true;
        }
        return false;
    }

    // Remove a card of board and return it
    public Card removeCard(int line, int col) {
        validatePosition(line, col);
        Card card = board[line][col];
        board[line][col] = null;
        return card;
    }

    // Gets card in a position
    public Card getCard(int line, int col) {
        validatePosition(line, col);
        return board[line][col];
    }

    // Gets opposite card
    public Card getOppositeCard(int line, int col) {
        validatePosition(line, col);

        if (col == 1) { // Attack of player 1
            return board[line][2]; // Verify line attack of player 2
        } else if (col == 2) { // Attack of player 2
            return board[line][1]; // Verify line attack of player 1
        }
        return null;
    }

    // Validate Position
    private void validatePosition(int line, int col) {
        if (line < 0 || line >= height || col < 0 || col >= width) {
            throw new IllegalArgumentException(
                    String.format("Invalid Position: [%d][%d]", line, col)
            );
        }
    }

    // Debugging method - viewing the board
    public void printBoard() {
        System.out.println("=== Board ===");
        for (int line = 0; line < height; line++) {
            System.out.print("Linha " + line + ": ");
            for (int col = 0; col < width; col++) {
                if (board[line][col] != null) {
                    System.out.print("[" + board[line][col].getName() + "] ");
                } else {
                    System.out.print("[EMPTY] ");
                }
            }
            System.out.println(" (" + getSpaceType(line, 0) + ")");
        }
        System.out.println("================");
    }
}