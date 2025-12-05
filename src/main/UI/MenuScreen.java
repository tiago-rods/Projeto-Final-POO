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
    private VBox instructionsBox;
    private VBox instructionsContentBox;
    private Label title;

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
        title = new Label("INSCRYPTION");
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
        Button btnInstructions = createMenuButton("Instructions");
        Button btnSettings = createMenuButton("Settings");
        Button btnExit = createMenuButton("Exit");

        btnNewGame.setOnAction(e -> showNewGameView());
        btnCreateGame.setOnAction(e -> {}); // Do nothing
        btnInstructions.setOnAction(e -> showInstructionsView());
        btnSettings.setOnAction(e -> showSettingsView());
        btnExit.setOnAction(e -> stage.close());

        menuBox.getChildren().addAll(btnNewGame, btnCreateGame, btnInstructions, btnSettings, btnExit);

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

        // Instructions View
        createInstructionsView();

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

        root.getChildren().addAll(title, menuBox, newGameBox, instructionsBox, settingsBox);
    }

    private void createInstructionsView() {
        instructionsBox = new VBox();
        instructionsBox.spacingProperty().bind(root.heightProperty().divide(36));
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setVisible(false);

        // Content Box for text/buttons
        instructionsContentBox = new VBox();
        instructionsContentBox.setAlignment(Pos.CENTER);
        instructionsContentBox.spacingProperty().bind(root.heightProperty().divide(50));

        // Initial Overview
        showInstructionsOverview();

        instructionsBox.getChildren().add(instructionsContentBox);
    }

    private void showInstructionsOverview() {
        instructionsContentBox.getChildren().clear();

        Label lblTitle = new Label("Overview");
        lblTitle.setTextFill(Color.BEIGE);
        lblTitle.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(48).asString(), "px;"));

        Label lblText = new Label(
            "The game adopts a PvP (player versus player) format, differentiating itself from the original single-player experience. " +
            "Our focus is on adapting Act 1, preserving the dark atmosphere and emphasizing strategic deck-building mechanics.\n\n" +
            "Although we maintain characteristic elements — such as cost, damage, sigils, sacrifice, and bones — we opted for a 4x4 board, " +
            "unlike the original 4x3. This change makes the game more balanced and dynamic for two players. " +
            "Our goal is to provide an enjoyable, strategic, and engaging experience."
        );
        lblText.setTextFill(Color.BEIGE);
        lblText.setWrapText(true);
        lblText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblText.maxWidthProperty().bind(root.widthProperty().multiply(0.6));
        lblText.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ", root.widthProperty().divide(100).asString(), "px;"));

        Button btnBoard = createMenuButton("Board Dynamics");
        Button btnItems = createMenuButton("Items");
        Button btnDecks = createMenuButton("Decks and Cards");
        Button btnSigils = createMenuButton("Items and Sigils");
        Button btnBalance = createMenuButton("Balance and Attack");
        Button btnBack = createMenuButton("Back");

        btnBoard.setOnAction(e -> showInstructionDetail("Board Dynamics", 
            "The board has 4 columns by 4 rows, allowing for greater strategic depth.\n\n" +
            "Both players can place cards behind others: " +
            "If the front card dies, the card placed behind it automatically advances to the attack tile on its next turn."
        ));

        btnItems.setOnAction(e -> showInstructionDetail("Items", 
            "Just like in the original game, items play a crucial role, capable of drastically changing the course of the game when used at the right moment.\n\n" +
            "How to obtain items:\n" +
            "There are three ways to acquire an item during the game:\n\n" +
            "Starting item: " +
            "Each player starts the game with a random item, usable at any time during their turn.\n\n" +
            "Item sigil: " +
            "Certain cards have a special sigil. " +
            "When played, they grant a random item to the player who placed them.\n\n" +
            "Critical situation: " +
            "When a player reaches 1 life point, they automatically receive an additional item, adding more tension and the possibility of a comeback."
        ));

        btnDecks.setOnAction(e -> showInstructionDetail("Decks and Cards", 
            "The game uses two distinct decks: " +
            "Squirrel Deck and " +
            "Creature Deck.\n\n" +
            "Types of Cards:\n" +
            "There are three main categories: " +
            "Cards with no cost (such as squirrels and rabbits), " +
            "Cards with a blood cost (require sacrifices to be played), and " +
            "Cards with a bone cost (require a specific amount of accumulated bones).\n\n" +
            "Sacrifices:\n" +
            "To play cards that require blood: " +
            "It is necessary to sacrifice already positioned cards. " +
            "Most cards are worth 1 sacrifice point, except those with special sigils that increase this value.\n\n" +
            "Bones:\n" +
            "When losing a card (by death or sacrifice), the player gains 1 bone. " +
            "Accumulated bones serve as a resource to summon specific cards."
        ));

        btnSigils.setOnAction(e -> showInstructionDetail("Items and Sigils", 
            "Items are unique tools with special effects. " +
            "Each item has a unique function capable of completely altering the course of the game.\n\n" +
            "Sigils are special abilities that certain cards possess. They can provide effects such as: " +
            "Multiple attack, " +
            "Special movement, " +
            "Flight, " +
            "Among others."
        ));

        btnBalance.setOnAction(e -> showInstructionDetail("Balance and Attack", 
            "The board is divided into two types of tiles: " +
            "Placement tile and " +
            "Attack tile.\n\n" +
            "After a positioned round, the card automatically advances to the attack tile. " +
            "Combat takes place in this space: " +
            "If there is an enemy card in front, it receives the damage. " +
            "Otherwise, the damage is applied directly to the opposing player.\n\n" +
            "Balance System:\n" +
            "Each player has a damage scale. " +
            "If the scale accumulates 5 weights against you, one of your lives is lost. " +
            "Each player has two lives. When a life is lost, the game is reset and the duel begins again. " +
            "When both are lost, the player is defeated."
        ));

        btnBack.setOnAction(e -> showMainMenuView());

        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(btnBoard, btnItems, btnDecks, btnSigils, btnBalance, btnBack);

        instructionsContentBox.getChildren().addAll(lblTitle, lblText, buttonsBox);
    }

    private void showInstructionDetail(String title, String content) {
        instructionsContentBox.getChildren().clear();

        Label lblTitle = new Label(title);
        lblTitle.setTextFill(Color.BEIGE);
        lblTitle.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(48).asString(), "px;"));

        Label lblText = new Label(content);
        lblText.setTextFill(Color.BEIGE);
        lblText.setWrapText(true);
        lblText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblText.maxWidthProperty().bind(root.widthProperty().multiply(0.7));
        lblText.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ", root.widthProperty().divide(100).asString(), "px;"));

        Button btnBack = createMenuButton("Back");
        btnBack.setOnAction(e -> showInstructionsOverview());

        instructionsContentBox.getChildren().addAll(lblTitle, lblText, btnBack);
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
        instructionsBox.setVisible(false);
    }

    private void showSettingsView() {
        menuBox.setVisible(false);
        newGameBox.setVisible(false);
        settingsBox.setVisible(true);
        instructionsBox.setVisible(false);
    }

    private void showInstructionsView() {
        menuBox.setVisible(false);
        newGameBox.setVisible(false);
        settingsBox.setVisible(false);
        instructionsBox.setVisible(true);
        showInstructionsOverview();
        
        // Move title to top left and reduce size
        StackPane.setAlignment(title, Pos.TOP_LEFT);
        title.translateYProperty().unbind();
        title.setTranslateY(20);
        title.setTranslateX(20);
        title.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(40).asString(), "px;"));
    }

    private void showMainMenuView() {
        menuBox.setVisible(true);
        newGameBox.setVisible(false);
        settingsBox.setVisible(false);
        instructionsBox.setVisible(false);
        
        resetTitlePosition();
    }

    private void resetTitlePosition() {
        StackPane.setAlignment(title, Pos.CENTER);
        title.setTranslateX(0);
        title.translateYProperty().bind(root.heightProperty().multiply(-0.185));
        title.styleProperty().bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ", root.widthProperty().divide(19.2).asString(), "px;"));
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
