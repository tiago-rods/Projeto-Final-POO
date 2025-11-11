import cards.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ui.screens.GameScreen;
import ui.screens.StartScreen;

public class Main extends Application {
    @Override
    public void start(Stage stage) {

        //Cria e exibe a tela inicial StartScreen
        StartScreen startScreen = new StartScreen();
        Scene startScene = startScreen.createScene(stage);

        //O Stage é a janela
        //A Scene é o conteúdo dentro da janela

        startScene.setOnKeyPressed((KeyEvent e) -> {
                new GameScreen().startGame(stage); // Vai pra GameScreen
                startScene.setOnKeyPressed(null); // remove o listener após o primeiro disparo
        });



        stage.setMaximized(true);
        stage.setTitle("Inscryption");
        stage.setScene(startScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);

        Board board = new Board();
        Deck deckP1 = new Deck();
        Deck deckP2 = new Deck();

        Player player1 = new Player("Rafa", 1);
        Player player2 = new Player("Yano", 2);

        initializePlayerHand(player1, deckP1);
        initializePlayerHand(player2, deckP2);


    }

    // Tabuleiro:
    /*              C0 C1 C2 C3
     *       Line 0  -  -  -  -
     *       Line 1  -  -  -  -
     *       Line 2  -  -  -  -
     *       Line 3  -  -  -  -
     * */


    /* Player deve comprar UMA carta, esquilo ou criatura
    // public static void buyHand(List<Card> hand)
     *  se ele clicar em esquilo
     *       add esquilo a mão
     *       deck.drawSquirrel(player.order.hand);
     *
     *  se ele clicar em criatura
     *       add criatura a mão
     *       player = deck.draw(hand);
     *
     *  impossibilita dele clicar dnv em qualquer baralho
     *
     * */

    // seleciona uma carta na mão e joga
    void SelectAndPlace(Player player, Board board) {
        // while (true) // loop infinito, só para quando o jogador colocar as cartas
        // Deve-se criar um índice para cada carta na mão na parte visual
        // Ao clicar nesse índice, por exemplo, clicou na segunda, retorna o numero 1 pq no array é a segunda carta da mão
        // card = removeCardFromHand(índice);
        // Ai faria tipo:
        // Verificar a posição do tabuleiro na qual o player esta querendo posicionar a carta e retornar esse índice (line, col) de posição,
        // esse índice sendo a posição de uma matriz das cartas do tabuleiro, ou seja, Ex: [0,3]
        // na teoria seria impossível marcar uma posição que não existe
        // if (player.order == 1) {
        //      while() {
        //      }
        //      bloodCount = 0;
        //
        //      if (bloodCount == bloodCost)
        //      if(board.EmptySpace(line, col) && (line == 2 || line == 3)) { // se tiver vazio e for na sua área
        //
        //          if() {
        //
        //          }
        //          board.placeCard(card, col, 1))
        //
        //      }
        // } if ( player.order == 2) {
        //
        //      }
        //

    }


    public static void initializePlayerHand (Player player, Deck deck) {
        deck.draw(5, player.getHand());
        deck.drawSquirrel(player.getHand());
    }

}
