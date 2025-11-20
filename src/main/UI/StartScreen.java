package UI;


import cards.*;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StartScreen {
    public Scene createScene(Stage stage) {
        // ===== TÍTULO DO JOGO =====
        Label title = new Label("INSCRYPTION");
        title.setTextFill(Color.BEIGE);
        title.setFont(Font.font("Serif", FontWeight.BOLD, 64));
        title.setEffect(new DropShadow(25, Color.rgb(255, 240, 200, 0.35)));

        // ===== TEXTO "PRESS ANY KEY TO START" =====
        Label pressKey = new Label("Press any key to start");
        pressKey.setTextFill(Color.LIGHTGRAY);
        pressKey.setFont(Font.font("Consolas", 20));
        pressKey.setTranslateY(100); // move o texto para baixo do título

        // ===== ANIMAÇÃO DE PISCAR =====
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), pressKey);
        fade.setFromValue(1.0);
        fade.setToValue(0.1);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // ===== LAYOUT DA TELA INICIAL =====
        StackPane root = new StackPane(title, pressKey);
        root.setStyle("-fx-background-color: #2B1A1A;"); // fundo escuro

        return new Scene(root, 1000, 700);
    }
}
