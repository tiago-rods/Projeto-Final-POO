import cards.*;
import items.*;
import sigils.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

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

        GameLogic game = new GameLogic(board, player1, player2, deckP1, deckP2);

        game.initializeBothPlayers();
    }

}
