package UI;

import cards.Board;
import cards.Deck;
import cards.Player;
import events.EventBus;
import events.EventLogics;
import events.GameLogic;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MenuScreen {

    private Stage stage;
    private StackPane root;
    private VBox menuBox;
    private VBox newGameBox;
    private VBox settingsBox;
    private ColorAdjust brightnessAdjust;

    public void start(Stage stage) {
        this.stage = stage;
        createMenuContent();

        // Reuse existing scene to maintain window state (maximized/size)
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1080, 720);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }

    private void createMenuContent() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #2B1A1A;");
        
        // Brightness effect
        brightnessAdjust = new ColorAdjust();
        root.setEffect(brightnessAdjust);

        // Title
        Label title = new Label("INSCRYPTION");
        title.setTextFill(Color.BEIGE);
        // Bind font size to root width (approx 1/19 of width)
        title.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(19.2).asString(), "px;"));
        // Bind translateY to root height (approx -1/5 of height)
        title.translateYProperty().bind(root.heightProperty().multiply(-0.185));

        // Main Menu Options
        menuBox = new VBox(); 
        // Bind spacing to root height
        menuBox.spacingProperty().bind(root.heightProperty().divide(36));
        menuBox.setAlignment(Pos.CENTER);
        // Push menu down a bit to separate from title
        menuBox.translateYProperty().bind(root.heightProperty().multiply(0.046));

        Button btnNewGame = createMenuButton("Start a new game");
        Button btnCreateGame = createMenuButton("Create a new game");
        Button btnSettings = createMenuButton("Settings");
        Button btnExit = createMenuButton("Exit");

        btnNewGame.setOnAction(e -> showNewGameView());
        btnCreateGame.setOnAction(e -> {}); // Do nothing
        btnSettings.setOnAction(e -> showSettingsView());
        btnExit.setOnAction(e -> stage.close());

        menuBox.getChildren().addAll(btnNewGame, btnCreateGame, btnSettings, btnExit);

        // New Game View
        newGameBox = new VBox();
        newGameBox.spacingProperty().bind(root.heightProperty().divide(43.2));
        newGameBox.setAlignment(Pos.CENTER);
        newGameBox.setVisible(false);

        Label lblEnterNames = new Label("Enter Player Names");
        lblEnterNames.setTextFill(Color.BEIGE);
        lblEnterNames.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(64).asString(), "px;"));

        TextField p1Name = new TextField("Player 1");
        p1Name.maxWidthProperty().bind(root.widthProperty().multiply(0.156));
        p1Name.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-size: ", root.widthProperty().divide(120).asString(), "px;"));
        
        TextField p2Name = new TextField("Player 2");
        p2Name.maxWidthProperty().bind(root.widthProperty().multiply(0.156));
        p2Name.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-size: ", root.widthProperty().divide(120).asString(), "px;"));
        
        Button btnPlay = createMenuButton("Play");
        Button btnBackNewGame = createMenuButton("Back");

        btnPlay.setOnAction(e -> startGame(p1Name.getText(), p2Name.getText()));
        btnBackNewGame.setOnAction(e -> showMainMenuView());

        newGameBox.getChildren().addAll(lblEnterNames, p1Name, p2Name, btnPlay, btnBackNewGame);

        // Settings View
        settingsBox = new VBox();
        settingsBox.spacingProperty().bind(root.heightProperty().divide(36));
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setVisible(false);

        Label lblSettingsTitle = new Label("Settings");
        lblSettingsTitle.setTextFill(Color.BEIGE);
        lblSettingsTitle.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(48).asString(), "px;"));

        // Graphics - Brightness
        VBox graphicsBox = new VBox(10);
        graphicsBox.setAlignment(Pos.CENTER);
        Label lblGraphics = new Label("Screen Brightness");
        lblGraphics.setTextFill(Color.BEIGE);
        lblGraphics.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ", root.widthProperty().divide(96).asString(), "px;"));
        
        Slider brightnessSlider = new Slider(-0.8, 0.2, 0.0); // Range from dark to slightly bright
        brightnessSlider.maxWidthProperty().bind(root.widthProperty().multiply(0.156));
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightnessAdjust.setBrightness(newVal.doubleValue());
        });
        
        graphicsBox.getChildren().addAll(lblGraphics, brightnessSlider);

        // Audio - Volume
        VBox audioBox = new VBox(10);
        audioBox.setAlignment(Pos.CENTER);
        Label lblAudio = new Label("Overall Sound");
        lblAudio.setTextFill(Color.BEIGE);
        lblAudio.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ", root.widthProperty().divide(96).asString(), "px;"));
        
        Slider volumeSlider = new Slider(0, 100, 60); // Default 60%
        volumeSlider.maxWidthProperty().bind(root.widthProperty().multiply(0.156));
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            AudioController.setBGMVolume(newVal.doubleValue() / 100.0);
        });
        
        audioBox.getChildren().addAll(lblAudio, volumeSlider);

        Button btnBackSettings = createMenuButton("Back");
        btnBackSettings.setOnAction(e -> showMainMenuView());

        settingsBox.getChildren().addAll(lblSettingsTitle, graphicsBox, audioBox, btnBackSettings);

        root.getChildren().addAll(title, menuBox, newGameBox, settingsBox);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.prefWidthProperty().bind(root.widthProperty().multiply(0.156)); // 300 / 1920
        btn.prefHeightProperty().bind(root.heightProperty().multiply(0.046)); // 50 / 1080
        
        // Bind font size
        javafx.beans.binding.StringExpression fontSizeBinding = javafx.beans.binding.Bindings.concat("-fx-font-size: ", root.widthProperty().divide(96).asString(), "px;");
        
        String baseStyle = "-fx-background-color: #3a2d2d; -fx-text-fill: beige; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: #5a4d4d; -fx-border-radius: 5;";
        String hoverStyle = "-fx-background-color: #4b3e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: #6a5d5d; -fx-border-radius: 5;";
        
        // Initial style
        btn.styleProperty().bind(javafx.beans.binding.Bindings.concat(baseStyle, fontSizeBinding));
        
        btn.setOnMouseEntered(e -> {
            btn.styleProperty().unbind();
            btn.styleProperty().bind(javafx.beans.binding.Bindings.concat(hoverStyle, fontSizeBinding));
            AudioController.playSFX("clique.wav");
        });
        btn.setOnMouseExited(e -> {
            btn.styleProperty().unbind();
            btn.styleProperty().bind(javafx.beans.binding.Bindings.concat(baseStyle, fontSizeBinding));
        });
        btn.setOnMousePressed(e -> AudioController.playSFX("selecionar.wav"));
        
        return btn;
    }

    private void showNewGameView() {
        menuBox.setVisible(false);
        newGameBox.setVisible(true);
        settingsBox.setVisible(false);
    }

    private void showSettingsView() {
        menuBox.setVisible(false);
        newGameBox.setVisible(false);
        settingsBox.setVisible(true);
    }

    private void showMainMenuView() {
        menuBox.setVisible(true);
        newGameBox.setVisible(false);
        settingsBox.setVisible(false);
    }

    private void startGame(String p1, String p2) {
        // Initialize Game
        EventBus eventBus = new EventBus();
        Board board = new Board();
        Deck deckP1 = new Deck();
        Deck deckP2 = new Deck();
        Player player1 = new Player(p1.isEmpty() ? "Player 1" : p1, 1);
        Player player2 = new Player(p2.isEmpty() ? "Player 2" : p2, 2);

        GameLogic game = new GameLogic(board, player1, player2, deckP1, deckP2, eventBus);
        EventLogics event = new EventLogics(game, eventBus);

        game.initializeBothPlayers();

        new GameScreen(game, eventBus).startGame(stage);
    }
}
