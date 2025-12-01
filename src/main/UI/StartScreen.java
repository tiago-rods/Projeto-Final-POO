package UI;

import cards.*;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
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
        title.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                root.widthProperty().divide(30).asString(), "px;"
        ));
        title.setEffect(new DropShadow(25, Color.rgb(255, 240, 200, 0.35)));

        // ===== TEXTO "PRESS ANY KEY TO START" =====
        Label pressKey = new Label("Press any key to start");
        pressKey.setTextFill(Color.LIGHTGRAY);
        pressKey.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ",
                root.widthProperty().divide(96).asString(), "px;"
        ));

        pressKey.translateYProperty().bind(root.heightProperty().divide(10));

        // ===== CRÉDITOS  =====
        Label credits = new Label("Game made by: \nRafael M. Nogueira F.\nTiago A. Rodrigues\nArtur Yano C.");
        credits.setTextFill(Color.rgb(180, 180, 180)); // Cinza claro, mas sutil
        credits.setTextAlignment(TextAlignment.LEFT); // Alinha as linhas do texto à direita

        // Tamanho da fonte responsivo (um pouco menor que o "Press Start")
        credits.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ",
                root.widthProperty().divide(100).asString(), "px;"
        ));

        // Define a posição no StackPane e a margem
        StackPane.setAlignment(credits, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(credits, new Insets(0, 50, 60, 0)); // Top, Right, Bottom, Left

        // ===== ANIMAÇÃO DE PISCAR =====
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), pressKey);
        fade.setFromValue(1.0);
        fade.setToValue(0.1);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Adiciona todos os elementos ao root
        root.getChildren().addAll(title, pressKey, credits);

        return new Scene(root, 1080, 720);
    }
}