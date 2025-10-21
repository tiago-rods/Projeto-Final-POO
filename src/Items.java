public class Items {
    String name(); //retornar o nome do item
    String description(); //retornar a descrição do item
    boolean canUse(GameContext context, Player user); //verificar se o item pode ser usado
    void use(GameContext context, Player user); //usar o item
}
