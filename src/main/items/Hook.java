package items;

public class Hook implements Items {
    @Override
    public String name() {
        return "Hook";
    }

    @Override
    public String description() {
        return "Brings one of your opponents card to your side";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        // Check if opponent has at least one card that can be hooked
        // (i.e., at least one card where there's space in the corresponding column)
        int opponentPlayerOrder = (player.getOrder() == 1) ? 2 : 1;

        // Check opponent's lines (0,1 for P2, 2,3 for P1)
        int[] opponentLines = (player.getOrder() == 1) ? new int[] { 0, 1 } : new int[] { 2, 3 };
        int playerPositioningLine = (player.getOrder() == 1) ? 3 : 0;
        int playerAttackLine = (player.getOrder() == 1) ? 2 : 1;

        for (int line : opponentLines) {
            for (int col = 0; col < 4; col++) {
                cards.Card card = game.getBoard().getCard(line, col);
                if (card != null && card instanceof cards.CreatureCard) {
                    // Check if there's space in this column
                    boolean positioningEmpty = game.getBoard().EmptySpace(playerPositioningLine, col);
                    boolean attackEmpty = game.getBoard().EmptySpace(playerAttackLine, col);

                    if (positioningEmpty || attackEmpty) {
                        return true; // Found at least one hookable card
                    }
                }
            }
        }
        return false; // No hookable cards found
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        // The actual logic is handled by the UI (GameScreen)
        // This method is called to trigger the selection mode
    }

}
