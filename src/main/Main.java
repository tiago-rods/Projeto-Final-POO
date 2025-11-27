import UI.MenuScreen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.Objects;
import UI.StartScreen;

public class Main extends Application {
    @Override
    public void start(Stage stage) {

        // A inicialização do jogo é feita na MenuScreen ao clicar em "Play"
        //Cria e exibe a tela inicial StartScreen
        StartScreen startScreen = new StartScreen();
        Scene startScene = startScreen.createScene(stage);
        Image Icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png")));

        //O Stage é a janela
        //A Scene é o conteúdo dentro da janela

        startScene.setOnKeyPressed((KeyEvent e) -> {
                new MenuScreen().start(stage); // Vai pra MenuScreen
                startScene.setOnKeyPressed(null); // remove o listener após o primeiro disparo
        });

        stage.setMaximized(true);
        stage.setTitle("Inscryption PvP");
        stage.getIcons().add(Icon);
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
