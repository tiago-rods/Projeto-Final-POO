//package cards;
//
//import javafx.animation.FadeTransition;
//import javafx.animation.RotateTransition;
//import javafx.animation.ScaleTransition;
//import javafx.geometry.Pos;
//import javafx.scene.Parent;
//import javafx.scene.control.Label;
//import javafx.scene.effect.DropShadow;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.StackPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Line;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.util.Duration;
//
//public class WaitingScreen {
//
//    public Parent createRoot(String nextPlayerName) {
//
//        // ===== TÍTULO =====
//        Label title = new Label("WAITING FOR " + nextPlayerName.toUpperCase());
//        title.setTextFill(Color.BEIGE);
//        title.setFont(Font.font("Serif", FontWeight.BOLD, 44));
//        title.setEffect(new DropShadow(25, Color.rgb(255, 240, 200, 0.35)));
//
//        // Animação de pulso MUITO mais suave
//        ScaleTransition pulse = new ScaleTransition(Duration.seconds(3.5), title);
//        pulse.setFromX(1.0);
//        pulse.setFromY(1.0);
//        pulse.setToX(1.05);  // bem sutil
//        pulse.setToY(1.05);
//        pulse.setCycleCount(ScaleTransition.INDEFINITE);
//        pulse.setAutoReverse(true);
//        pulse.setInterpolator(javafx.animation.Interpolator.EASE_BOTH); // suavidade
//        pulse.play();
//
//        // ===== SUBTÍTULO =====
//        Label turnText = new Label("TO START THE TURN");
//        turnText.setTextFill(Color.BEIGE);
//        turnText.setFont(Font.font("Serif", FontWeight.NORMAL, 26));
//
//        // Linha decorativa no estilo do jogo
//        Line divider = new Line(0, 0, 280, 0);
//        divider.setStroke(Color.web("#6a4c4c")); // mesma cor de borda do tabuleiro
//        divider.setStrokeWidth(2);
//
//        // ===== TEXTO "PRESS ANY KEY" =====
//        Label pressKey = new Label("Press any key to continue");
//        pressKey.setTextFill(Color.LIGHTGRAY);
//        pressKey.setFont(Font.font("Consolas", 20));
//        pressKey.setTranslateY(8);
//
//        // Fade pulsante igual à StartScreen, mas levemente mais dramático
//        FadeTransition fade = new FadeTransition(Duration.seconds(1.1), pressKey);
//        fade.setFromValue(1.0);
//        fade.setToValue(0.15);
//        fade.setCycleCount(FadeTransition.INDEFINITE);
//        fade.setAutoReverse(true);
//        fade.play();
//
//        // ===== CONTEÚDO CENTRAL =====
//        VBox content = new VBox(16, title, turnText, divider, pressKey);
//        content.setAlignment(Pos.CENTER);
//
//        // ===== CAIXA "CINZA" CENTRAL (overlay) =====
//        StackPane card = new StackPane(content);
//        card.setAlignment(Pos.CENTER);
//        card.setMaxWidth(600);
//        card.setStyle(
//                "-fx-background-color: rgba(40, 34, 34, 0.92);" + // cinza/ marrom escuro
//                        "-fx-border-color: #6a4c4c;" +                   // mesma borda do board
//                        "-fx-border-width: 3;" +
//                        "-fx-border-radius: 18;" +
//                        "-fx-background-radius: 18;" +
//                        "-fx-padding: 40;"
//        );
//        card.setEffect(new DropShadow(35, Color.rgb(0, 0, 0, 0.8)));
//
//        // ===== ROOT (FUNDO) =====
//        StackPane root = new StackPane(card);
//        root.setAlignment(Pos.CENTER);
//        root.setStyle(
//                // fundo no mesmo tom da StartScreen / jogo
//                "-fx-background-color: linear-gradient(to bottom, #2B1A1A, #1f1b1b);" +
//                        "-fx-padding: 40;"
//        );
//
//        return root;
//    }
//}

package cards;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class WaitingScreen {

    public Parent createRoot(String nextPlayerName) {

        // ===== TÍTULO =====
        Label title = new Label("WAITING FOR " + nextPlayerName.toUpperCase());
        title.setTextFill(Color.BEIGE);
        title.setFont(Font.font("Serif", FontWeight.BOLD, 44));
        title.setEffect(new DropShadow(25, Color.rgb(255, 240, 200, 0.35)));


        // =========================
        //  SUBTÍTULO
        // =========================
        Label turnText = new Label("TO START THE TURN");
        turnText.setTextFill(Color.BEIGE);
        turnText.setFont(Font.font("Serif", FontWeight.NORMAL, 24));

        // =========================
        //  LINHA DECORATIVA
        // =========================
        Line divider = new Line(0, 0, 260, 0);
        divider.setStroke(Color.web("#6a4c4c"));
        divider.setStrokeWidth(2);

        // =========================
        //  TEXTO "PRESS ANY KEY"
        // =========================
        Label pressKey = new Label("Press any key to continue");
        pressKey.setTextFill(Color.LIGHTGRAY);
        pressKey.setFont(Font.font("Consolas", 20));
        pressKey.setTranslateY(6);

        FadeTransition pressFade = new FadeTransition(Duration.seconds(0.9), pressKey);
        pressFade.setFromValue(1.0);
        pressFade.setToValue(0.2);
        pressFade.setCycleCount(FadeTransition.INDEFINITE);
        pressFade.setAutoReverse(true);
        pressFade.play();


        // =========================
        //  CONTEÚDO CENTRAL
        // =========================
        VBox content = new VBox(14, title, turnText, divider, pressKey);
        content.setAlignment(Pos.CENTER);

        StackPane card = new StackPane(content);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(620);
        card.setStyle(
                "-fx-background-color: rgba(40, 34, 34, 0.94);" +
                        "-fx-border-color: #6a4c4c;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 38;"
        );
        card.setEffect(new DropShadow(40, Color.rgb(0, 0, 0, 0.85)));

        // ===== ROOT (FUNDO) =====
        StackPane root = new StackPane(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                // fundo no mesmo tom da StartScreen / jogo
                "-fx-background-color: linear-gradient(to bottom, #2B1A1A, #1f1b1b);" +
                        "-fx-padding: 40;"
        );

        // =========================
        //  INTERAÇÃO COM O MOUSE
        // =========================

        // Quando mexer o mouse, faz um parallax suave no "card" + sigils
        root.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            double w = root.getWidth();
            double h = root.getHeight();
            if (w <= 0 || h <= 0) return;

            double dx = (e.getX() - w / 2.0) / (w / 2.0);  // -1 .. 1
            double dy = (e.getY() - h / 2.0) / (h / 2.0);  // -1 .. 1

            double maxRotate = 5.0;
            double maxTranslateX = 18.0;
            double maxTranslateY = 10.0;

            card.setRotate(-dx * maxRotate);
            card.setTranslateX(dx * maxTranslateX);
            card.setTranslateY(dy * maxTranslateY);


        });

        return root;
    }
}



