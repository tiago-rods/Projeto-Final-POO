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
    }
}
