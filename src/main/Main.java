import cards.*;
import events.EventLogics;
import events.GameLogic;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {

        Board board = new Board();
        Deck deckP1 = new Deck();
        Deck deckP2 = new Deck();

        Player player1 = new Player("Rafa", 1);
        Player player2 = new Player("Yano", 2);

        GameLogic game = new GameLogic(board, player1, player2, deckP1, deckP2);
        EventLogics event = new EventLogics(game);

        game.initializeBothPlayers();
        // Agora vai chamando os eventos dentro de EventLogics ...


        //Cria e exibe a tela inicial StartScreen
        StartScreen startScreen = new StartScreen();
        Scene startScene = startScreen.createScene(stage);

        //O Stage é a janela
        //A Scene é o conteúdo dentro da janela

        startScene.setOnKeyPressed((KeyEvent e) -> {
                new GameScreen(game).startGame(stage); // Vai pra GameScreen
                startScene.setOnKeyPressed(null); // remove o listener após o primeiro disparo
        });

        stage.setMaximized(true);
        stage.setTitle("Inscryption");
        stage.setScene(startScene);
        stage.show();
    }

    // Na main só deve conter launch
    public static void main(String[] args) {
        launch(args);
        // Qualquer código no main depois de launch(args);
        // (como a criação do seu GameLogic) só executa depois que a janela do JavaFX é fechada.
    }

}
