public class Board {
    // Constants for board dimensions
    private static final int width = 4;
    private static final int height = 4;

    // Matrix representing the board [row][column]
    private Card[][] board;

    /*              C0 C1 C2 C3
    *       Line 0  -  -  -  -
    *       Line 1  -  -  -  -
    *       Line 2  -  -  -  -
    *       Line 3  -  -  -  -
    * */

    // Enum -> Naming the types of spaces on the game board
    // Enum to identify the board zones
    public enum SpaceType {
        PLAYER_1_POSITIONING,  // Line 3 (lower)
        ATTACK_PLAYER1,          // Line 2
        ATTACK_PLAYER2,          // Line 1
        PLAYER_2_POSITIONING   // Line 0 (upper)
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
        switch (line) {
            case 3: return SpaceType.PLAYER_1_POSITIONING;
            case 2: return SpaceType.ATTACK_PLAYER1;
            case 1: return SpaceType.ATTACK_PLAYER2;
            case 0: return SpaceType.PLAYER_2_POSITIONING;
            default: throw new IllegalArgumentException("Invalid Line: " + line);
        }
    }

    // Checks if a position is empty
    public boolean EmptySpace(int line, int col) {
        validatePosition(line, col);
        return board[line][col] == null;
    }

    // Place a card in the placement space
    // No need to pass line as param because it changes depending on the player.
    public boolean placeCard(Card card, int colPos, int player) {
        int linePos = (player == 1) ? 3 : 0;

        if (EmptySpace(linePos, colPos)) {
            board[linePos][colPos] = card;
            card.setPos(linePos, colPos);
            return true;
        }
        return false;
    }

    // Move card from placement space to attack space.
    public boolean moveToAttack(int col, int player) {
        int linePos = (player == 1) ? 3 : 0;
        int lineAttack = (player == 1) ? 2 : 1;

        // Checks if there is a card in the position and if the attack slot is empty
        if (!EmptySpace(linePos, col) && EmptySpace(lineAttack, col)) {
            // aux variable, current position is now NULL, card goes to attack position
            Card card = board[linePos][col];
            board[linePos][col] = null;
            board[lineAttack][col] = card;
            card.setPos(lineAttack, col);
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

    // Gets opposite attack card
    // If doesnt have a card, returns NULL
    // Passes the current card of the player turn (your line, your col)
    public Card getOppositeAttackCard(int line, int col) {
        validatePosition(line, col);

        // if it is player1's turn
        if (line == 2) { // Attack of player 1
            // return a specific opposite card of a certain col
            return board[1][col]; // Verify line attack of player 2
        } else if (line == 3) { // Attack of player 2
            return board[2][col]; // Verify line attack of player 1
        }
        return null;
    }

    // Gets opposite defense card
    public Card getOppositeDefenseCard(int line, int col) {
        validatePosition(line, col);

        // if it is player1's turn
        if (line == 2) { // Attack of player 1
            // return a specific opposite card of a certain col
            return board[0][col]; // Verify line defense of player 2
        } else if (line == 3) { // Attack of player 2
            return board[3][col]; // Verify line defense of player 1
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