package UI;

import cards.Player;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EndScreen {
    private Stage stage;

    // Constantes de cores padronizadas
    private static final String BUTTON_BG_COLOR = "#3a2d2d";
    private static final String BUTTON_HOVER_BG_COLOR = "#4b3e3e";
    private static final String BUTTON_BORDER_COLOR = "#5a4d4d";
    private static final String BUTTON_HOVER_BORDER_COLOR = "#6a5d5d";
    private static final String BUTTON_TEXT_COLOR = "beige";
    private static final String BUTTON_HOVER_TEXT_COLOR = "white";

    public void start(Stage stage, Player winner) {
        this.stage = stage;

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #2B1A1A;");

        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);

        // Title
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setTextFill(Color.BEIGE);
        gameOverLabel.setStyle("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: 80px;");

        // Winner announcement
        Label winnerLabel = new Label(winner.getName() + " Wins!");
        winnerLabel.setTextFill(Color.BEIGE);
        winnerLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 40px;");

        // Buttons
        Button btnMenu = createButton("Return to Menu");
        Button btnExit = createButton("Exit Game");

        btnMenu.setOnAction(e -> {
            // Return to main menu
            AudioController.startBGM("dark_bg_edited.wav");
            new MenuScreen().start(stage);
        });

        btnExit.setOnAction(e -> {
            Platform.exit();
        });

        contentBox.getChildren().addAll(gameOverLabel, winnerLabel, btnMenu, btnExit);
        root.getChildren().add(contentBox);

        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);

        // Estilo base padronizado
        String baseStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 20px; " +
                        "-fx-padding: 10 20; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;",
                BUTTON_BG_COLOR, BUTTON_TEXT_COLOR, BUTTON_BORDER_COLOR);

        // Estilo hover padronizado
        String hoverStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 20px; " +
                        "-fx-padding: 10 20; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;",
                BUTTON_HOVER_BG_COLOR, BUTTON_HOVER_TEXT_COLOR, BUTTON_HOVER_BORDER_COLOR);

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            AudioController.playSFX("clique.wav");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
        });

        btn.setOnMousePressed(e -> AudioController.playSFX("selecionar.wav"));

        return btn;
    }
}