package items;

public class
Scissors implements Items {
    @Override
    public String name() {
        return "Scissors";
    }

    @Override
    public String description() {
        return "When used, the player chooses one card of the opponent to be cut";
    }

    @Override
    public boolean canUse(events.GameLogic game, cards.Player player) {
        // Check if opponent has at least one card on the board
        int opponentPlayerOrder = (player.getOrder() == 1) ? 2 : 1;

        // Check opponent's lines (0,1 for P2, 2,3 for P1)
        int[] opponentLines = (player.getOrder() == 1) ? new int[] { 0, 1 } : new int[] { 2, 3 };

        for (int line : opponentLines) {
            for (int col = 0; col < 4; col++) {
                cards.Card card = game.getBoard().getCard(line, col);
                if (card != null && card instanceof cards.CreatureCard) {
                    return true; // Found at least one opponent card
                }
            }
        }
        return false; // No opponent cards found
    }

    @Override
    public void use(events.GameLogic game, cards.Player player) {
        // The actual logic is handled by the UI (GameScreen)
        // This method is called to trigger the selection mode
    }

}
