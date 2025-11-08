package events;
import cards.*;

public class EventLogics {

    private EventBus eventBus;
    private GameLogic game;

    public EventLogics (GameLogic game) {
        this.game = game;
        this.eventBus = new EventBus();

        setupSubscribers();
    }

    // esse event, é aquele chamado ao clicar no botão
    private void setupSubscribers () {
        eventBus.subscribe(EventType.CARD_DRAWN, event -> {
            drawCard();
            System.out.println("Carta comprada: " + event.getCard());
            // chama a função que
        });

    }


    private void drawCard () {
        if (game.getCurrentPlayer() == game.getPlayer1()) {
            game.drawCard(game.getPlayer1(), game.getDeckP1());
        } else if (game.getCurrentPlayer() == game.getPlayer2()) {
            game.drawCard(game.getPlayer2(), game.getDeckP2());
        } else {
            System.out.println("Erro ao comprar carta");
        }
    }


}
