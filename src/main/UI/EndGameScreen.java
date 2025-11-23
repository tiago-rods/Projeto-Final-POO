package UI;

import cards.Player;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EndGameScreen extends StackPane {

    public EndGameScreen(Player winner, Runnable onRestart, Runnable onClose) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("GAME OVER");
        titleLabel.setTextFill(Color.RED);
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 60));

        Label winnerLabel = new Label(winner.getName() + " Wins!");
        winnerLabel.setTextFill(Color.WHITE);
        winnerLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 30));

        Button restartButton = createButton("Restart");
        restartButton.setOnAction(e -> onRestart.run());

        Button closeButton = createButton("Close Game");
        closeButton.setOnAction(e -> onClose.run());

        content.getChildren().addAll(titleLabel, winnerLabel, restartButton, closeButton);
        this.getChildren().add(content);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #4b2e2e; " +
                        "-fx-text-fill: #f0e6d2; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-size: 20;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #4b2020; " +
                        "-fx-text-fill: #f0e6d2; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-size: 20;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #4b2e2e; " +
                        "-fx-text-fill: #f0e6d2; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-size: 20;"));
        return btn;
    }
}
