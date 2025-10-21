
// Item Hook permite que voce traga uma criatura do seu oponente para o seu lado do tabuleiro

public class HookItem implements Items {
    @Override
    public String name(){ return "Hook"; } //retornar o nome do item

    @Override
    public String description(){ return "Steal a creature from your opponent's side and bring it to your side."; }

    @Override
    public boolean canUse(GameContext context, Player user){ //verificador para ver se o item pode ser utilizado
        Board board = context.getBoard(); //verifica se há espaço vazio do lado do jogador e se o oponente possui alguma criatura
        return board.hasEmptySlot(user.getSide()) && board.hasAnyCreature(board.opponentSideOf(user.getSide()));
    }

    @Override
    public void use(GameContext context, Player user){
        if(!canUse(context, user)) throw new IllegalStateException("Não pode usar o Gancho agora."); //verifica se o item pode ser utilizado
        Board board = context.getBoard();

        Slot enemySlot = board.firstOccupiedSlot(board.opponentSideOf(user.getSide()));
        .orElseThrow();
        CreatureCard captured = enemySlot.removeCreature();
        Slot free = board.firstEmptySlot(user.getSide()).orElseThrow();
        free.placeCreature(captured);
        context.log().publish(new CreatureCapturedEvent(user, captured));
        user.getItems().remove(this);
    }
}
