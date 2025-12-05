package items;

public interface Items {
    String name(); // retornar o nome do item

    String description(); // retornar a descrição do item

    boolean canUse(events.GameLogic game, cards.Player player); // verificar se o item pode ser usado

    void use(events.GameLogic game, cards.Player player); // usar o item
}
