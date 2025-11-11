package ui.screens;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PassTurnScreen {
    public Scene createScene(Stage stage) {

        // ===== TEXTO "PRESS ANY KEY TO START" =====
        Label pressKey = new Label("Press any key to start your turn");
        pressKey.setTextFill(Color.LIGHTGRAY);
        pressKey.setFont(Font.font("Consolas", 20));

        // ===== ANIMAÇÃO DE PISCAR =====
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), pressKey);
        fade.setFromValue(1.0);
        fade.setToValue(0.1);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // ===== LAYOUT DA TELA INICIAL =====
        StackPane root = new StackPane(pressKey);
        root.setStyle("-fx-background-color: #2B1A1A;"); // fundo escuro

        return new Scene(root, 1000, 700);
    }
}
