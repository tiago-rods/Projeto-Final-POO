//carta do porquinho da ao jogador 4 ossos
public class HoggyBankItem implements Items {
    @Override
    public String name() { return "Hoggy Bank"; }

    @Override
    public String description() { return "Gains 4 bones imediatelly (one use)."; }

    @Override
    public boolean canUse(GameContext context, Player user) { return true; }

    @Override
    public void use(GameContext context, Player user) {
        user.addBones(4);
        context.log().publish(new BonesGainedEvent(user, 4)); //ganha 4 ossos
        user.getItems().remove(this);
    }
}

