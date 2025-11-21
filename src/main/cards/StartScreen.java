package cards;


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
        //  TOCAR MUSICA DE FUNDO
        AudioController.startBGM("dark_bg_edited.wav");
        AudioController.setBGMVolume(0.6);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #2B1A1A;"); // fundo escuro

        // ===== TÍTULO DO JOGO =====
        Label title = new Label("INSCRYPTION");
        title.setTextFill(Color.BEIGE);
        // Bind font size to root width (approx 1/30 of width)
        title.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(30).asString(), "px;"));
        title.setEffect(new DropShadow(25, Color.rgb(255, 240, 200, 0.35)));

        // ===== TEXTO "PRESS ANY KEY TO START" =====
        Label pressKey = new Label("Press any key to start");
        pressKey.setTextFill(Color.LIGHTGRAY);
        // Bind font size to root width (approx 1/96 of width)
        pressKey.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ", root.widthProperty().divide(96).asString(), "px;"));
        
        // Bind translateY to root height (approx 1/10 of height)
        pressKey.translateYProperty().bind(root.heightProperty().divide(10));

        // ===== ANIMAÇÃO DE PISCAR =====
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), pressKey);
        fade.setFromValue(1.0);
        fade.setToValue(0.1);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        root.getChildren().addAll(title, pressKey);

        return new Scene(root, 1080, 720);
    }
}
