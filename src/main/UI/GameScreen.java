package UI;

import cards.Board;
import cards.Card;
import cards.CreatureCard;
import cards.Player;
import events.EventBus;
import events.EventType;
import events.GameLogic;
import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.geometry.VPos;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Tooltip;

public class GameScreen {

    private Stage gameWindow;
    private Parent gameRoot;

    // Passando o game para ca, a gnt tem controle de todas as informações da
    // partida
    private final GameLogic game;
    private final EventBus eventBus;

    // ==== CONSTRUTOR ====
    public GameScreen(GameLogic game, EventBus eventBus) {
        this.game = game;
        this.eventBus = eventBus;

        // Subscribe to game state changes to update UI automatically
        this.eventBus.subscribe(events.EventType.GAME_STATE_CHANGED, e -> {
            javafx.application.Platform.runLater(() -> {
                refreshBoardFromGame();
                refreshHandsFromGame();
                refreshBonesHUD();
                refreshLivesHUD();
                refreshScaleFromGame();
                updateTurnLabelFromGame();
            });
        });

        this.eventBus.subscribe(EventType.LIFE_LOST, e -> {
            javafx.application.Platform.runLater(() -> {
                Player attackedPlayer;
                // Se o jogador 1 estiver atacando, significa que o jogador2 perdeu a vida
                if(game.getCurrentPlayer() == game.getPlayer1()) {
                    attackedPlayer = game.getPlayer2();
                } else {
                    attackedPlayer = game.getPlayer1();
                }

                // Apenas ARMAZENA a informação para usar depois no passTurn
                this.lifeLostTriggered = true;
                this.playerWhoLostLife = attackedPlayer;
            });
        });

        // Listen for Game Over
        this.eventBus.subscribe(events.EventType.GAME_ENDED, e -> {
            javafx.application.Platform.runLater(() -> {
                AudioController.pauseBGM(); // Stop music on end
                new EndScreen().start(this.gameWindow, e.getPlayer());
            });
        });
        AudioController.startBGM("bg_fireplace.wav");
        // Valores entre 0.0001 e 1 (usa log, por isso n pode ser 0)
        AudioController.setBGMVolume(0.4);
    }

    // ====== medidas padrao (agora dinâmicas)
    private final DoubleProperty cardWidth = new SimpleDoubleProperty();
    private final DoubleProperty cardHeight = new SimpleDoubleProperty();

    // ====== controle de seleção
    private Card selectedCardNode = null;

    // ======= configs da mao
    private final DoubleProperty handSpacing = new SimpleDoubleProperty();
    private HBox playerHandP1;

    // label de turno
    private Label turnLabel;

    // grids do topo e do fundo (antes eram locais)
    private GridPane topGrid;
    private GridPane bottomGrid;

    // mensagem temporária para o jogador
    private Label messageLabel;

    // timer para esconder mensagem depois de alguns segundos
    private javafx.animation.PauseTransition messageHideTimer;

    // HUD de velas (vidas)
    private HBox livesHUD;
    private Label livesValueLabel;

    // HUD de ossos
    private HBox bonesHUD;
    private Label bonesValueLabel;

    // HUD de ITENS (vertical storage with images)
    private VBox itemStorageBox;

    // HUD da balança vertical
    private StackPane scaleContainer;
    private Line scaleLine;
    private Circle scaleMarker;

    // botão de passar turno
    private Button bellButton;
    // trava para evitar múltiplos cliques de turno
    private boolean isPassingTurn = false;

    // orientação da câmera: false = normal (P1 embaixo), true = invertida (P2
    // embaixo)
    private boolean flippedView = false;

    // ====== VARIÁVEIS DE ESTADO DE SACRIFÍCIO ======
    private enum SacrificeState {
        NORMAL, // Estado padrão
        AWAITING_SACRIFICE, // Selecionou carta da mão, esperando sacrifícios
        AWAITING_PLACEMENT // Sacrifícios feitos, esperando local para colocar
    }

    private SacrificeState currentSacrificeState = SacrificeState.NORMAL;
    private Card cardToPlayAfterSacrifice = null; // A carta da mão que queremos jogar

    // ====== ITEM SELECTION STATE ======
    private enum ItemSelectionMode {
        NORMAL, // No item selection active
        SELECTING_HOOK_TARGET, // Selecting opponent card to hook
        SELECTING_SCISSORS_TARGET // Selecting opponent card to cut
    }

    private ItemSelectionMode currentItemSelectionMode = ItemSelectionMode.NORMAL;
    private items.Items selectedItem = null; // The item currently being used

    // Lista de Slots da UI (para feedback visual)
    private java.util.List<StackPane> sacrificeSlots = new java.util.ArrayList<>();
    // Lista de Cartas da Lógica (para enviar ao GameLogic)
    private java.util.List<CreatureCard> sacrificeCards = new java.util.ArrayList<>();

    // ====== MENUS E OVERLAYS ======
    private StackPane rootPane;
    private VBox menuOverlay;
    private VBox settingsBox;
    private VBox instructionsBox;
    private VBox instructionsContentBox;
    private VBox surrenderBox;
    private ColorAdjust brightnessAdjust;
    private boolean isMenuOpen = false;
    private Label menuTitle; // Título do menu (Pause, Settings, etc)

    // =======================================================
    // ===== TELA DE JOGO =====
    public void startGame(Stage stage) {
        this.gameWindow = stage; // guarda a janela do jogo

        // ---------------------------------------------------------------------
        // LAYOUT GERAL:
        // Root é um StackPane para permitir overlays (menus)
        // ---------------------------------------------------------------------
        rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: #1f1b1b;");

        // Brightness effect global
        brightnessAdjust = new ColorAdjust();
        // rootPane.setEffect(brightnessAdjust); // REMOVIDO

        // O layout do jogo em si (HBox)
        HBox gameLayout = new HBox();
        gameLayout.setStyle("-fx-background-color: transparent;");
        gameLayout.setEffect(brightnessAdjust); // ADICIONADO

        // Mantém a altura proporcional à altura da janela
        cardHeight.bind(rootPane.heightProperty().multiply(0.138));

        // Garante proporção 2:3 (largura:altura)
        cardWidth.bind(cardHeight.multiply(2.0 / 3.0));

        // Espaçamento da mão continua proporcional à largura
        handSpacing.bind(rootPane.widthProperty().multiply(0.0026));


        // ====== PAINEL ESQUERDO (20%) ======
        VBox leftPanel = new VBox();
        leftPanel.setPadding(new Insets(16));
        leftPanel.setSpacing(12);
        leftPanel.setStyle(
                "-fx-background-color: #241d1d; " +
                        "-fx-border-color: #3a2d2d; " +
                        "-fx-border-width: 0 2 0 0;");

        // indicador de turno
        turnLabel = new Label();
        turnLabel.setTextFill(Color.BEIGE);
        turnLabel.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(76.8).asString(), "px;"));

        // === HUD de ossos ===
        Image bonesImg = new Image(
                getClass().getResource("/img/icons/bone.png").toExternalForm());
        ImageView bonesIcon = new ImageView(bonesImg);
        bonesIcon.fitWidthProperty().bind(rootPane.widthProperty().multiply(0.018));
        bonesIcon.fitHeightProperty().bind(rootPane.widthProperty().multiply(0.018));
        bonesIcon.setPreserveRatio(true);

        bonesValueLabel = new Label("0");
        bonesValueLabel.setTextFill(Color.BEIGE);
        bonesValueLabel.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(64).asString(), "px;"));

        bonesHUD = new HBox(6); // espaço entre imagem e número
        bonesHUD.setAlignment(Pos.CENTER_LEFT);
        bonesHUD.getChildren().addAll(bonesIcon, bonesValueLabel);

        // === HUD de velas (vidas) ===
        Image candlesImg = new Image(
                getClass().getResource("/img/icons/candle.png").toExternalForm());
        ImageView candlesIcon = new ImageView(candlesImg);
        candlesIcon.fitWidthProperty().bind(rootPane.widthProperty().multiply(0.018));
        candlesIcon.fitHeightProperty().bind(rootPane.widthProperty().multiply(0.018));
        candlesIcon.setPreserveRatio(true);

        livesValueLabel = new Label("0");
        livesValueLabel.setTextFill(Color.BEIGE);
        livesValueLabel.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(64).asString(), "px;"));

        livesHUD = new HBox(6); // espaço entre imagem e número
        livesHUD.setAlignment(Pos.CENTER_LEFT);
        livesHUD.getChildren().addAll(candlesIcon, livesValueLabel);

        // === ÁREA DE ITENS (VERTICAL) ===
        itemStorageBox = new VBox(5);
        itemStorageBox.setAlignment(Pos.CENTER);
        itemStorageBox.setStyle(
                "-fx-background-color: #3a2d2d; " +
                        "-fx-border-color: #5a4d4d; " +
                        "-fx-border-width: 2; " +
                        "-fx-padding: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5;");

        // === BALANÇA VERTICAL NO MEIO DO PAINEL ESQUERDO ===

        scaleContainer = new StackPane();
        scaleContainer.minWidthProperty().bind(rootPane.widthProperty().multiply(0.041));
        scaleContainer.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.041));
        scaleContainer.maxWidthProperty().bind(rootPane.widthProperty().multiply(0.041));
        scaleContainer.minHeightProperty().bind(rootPane.heightProperty().multiply(0.23));
        scaleContainer.prefHeightProperty().bind(rootPane.heightProperty().multiply(0.23));
        scaleContainer.maxHeightProperty().bind(rootPane.heightProperty().multiply(0.23));
        scaleContainer.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: #3a2d2d; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 3;" +
                        "-fx-padding: 4");

        scaleLine = new Line();
        scaleLine.setStroke(Color.DARKGOLDENROD);
        scaleLine.setStrokeWidth(4);

        scaleMarker = new Circle(10, Color.BEIGE);
        scaleMarker.setStroke(Color.web("#2b2222"));
        scaleMarker.setStrokeWidth(2);

        // === DIVISÕES VISUAIS DA BALANÇA (DE +5 A -5) ===
        VBox ticksBox = new VBox();
        ticksBox.setFillWidth(true);
        ticksBox.setSpacing(0);
        ticksBox.setAlignment(Pos.CENTER_LEFT);

        int minScale = -5;
        int maxScale = 5;

        // Cria 11 linhas: 5,4,3,2,1,0,1,2,3,4,5
        for (int v = maxScale; v >= minScale; v--) {
            HBox row = new HBox(4);
            row.setAlignment(Pos.CENTER_LEFT);

            // número (mostra valor absoluto como no Inscryption)
            Label lbl = new Label(String.valueOf(Math.abs(v)));
            lbl.setTextFill(Color.LIGHTGRAY);
            lbl.setFont(Font.font("Consolas", 11));

            // risquinho
            Line tick = new Line(0, 0, 12, 0);
            tick.setStroke(Color.GRAY);
            tick.setStrokeWidth(v == 0 ? 2.5 : 1.2); // linha central mais forte

            row.getChildren().addAll(lbl, tick);
            VBox.setVgrow(row, Priority.ALWAYS);
            ticksBox.getChildren().add(row);
        }

        // Ajusta o tamanho da barra vertical conforme o container
        scaleContainer.heightProperty().addListener((obs, oldH, newH) -> {
            double h = newH.doubleValue() - 40; // padding top/bottom
            if (h < 0)
                h = 0;
            scaleLine.setStartX(0);
            scaleLine.setStartY(-h / 2);
            scaleLine.setEndX(0);
            scaleLine.setEndY(h / 2);
            refreshScaleFromGame(); // reposiciona o marcador quando a altura muda
        });

        scaleContainer.getChildren().addAll(ticksBox, scaleLine, scaleMarker);

        StackPane.setAlignment(ticksBox, Pos.CENTER_LEFT); // ticks encostados à esquerda
        StackPane.setAlignment(scaleLine, Pos.CENTER);
        StackPane.setAlignment(scaleMarker, Pos.CENTER);

        VBox scaleBox = new VBox(6);
        scaleBox.setAlignment(Pos.CENTER_RIGHT);
        scaleBox.getChildren().addAll(scaleContainer);

        // Container horizontal: Itens + Balança
        HBox leftCenterBox = new HBox(10);
        leftCenterBox.setAlignment(Pos.CENTER);
        leftCenterBox.getChildren().addAll(itemStorageBox, scaleBox);

        // label de mensagem de ajuda
        messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#ffdd88"));

        messageLabel.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ",
                        rootPane.widthProperty().divide(137).asString(), "px;"));

        messageLabel.setWrapText(true);
        messageLabel.setVisible(false); // começa escondido

        // --- ESPAÇADORES ---
        Region spacerTop = new Region();
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        // botão de passar turno
        bellButton = new Button("Pass turn");
        bellButton.setMaxWidth(Double.MAX_VALUE);

        javafx.beans.binding.StringExpression bellFontSize = javafx.beans.binding.Bindings.concat("-fx-font-size: ",
                rootPane.widthProperty().divide(96).asString(), ";");

        String bellBaseStyle = "-fx-background-color: #4b2e2e; -fx-text-fill: #f0e6d2; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 14;";
        String bellHoverStyle = "-fx-background-color: #4b2020; -fx-text-fill: #f0e6d2; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 14;";

        bellButton.styleProperty().bind(javafx.beans.binding.Bindings.concat(bellBaseStyle, bellFontSize));

        bellButton.setOnMouseEntered(e -> {
            bellButton.styleProperty().unbind();
            bellButton.styleProperty().bind(javafx.beans.binding.Bindings.concat(bellHoverStyle, bellFontSize));
        });
        bellButton.setOnMouseExited(e -> {
            bellButton.styleProperty().unbind();
            bellButton.styleProperty().bind(javafx.beans.binding.Bindings.concat(bellBaseStyle, bellFontSize));
        });
        bellButton.setOnMouseClicked(e -> {
            AudioController.playSFX("bell.wav");
            System.out.println("Sino clicado.");
            passTurn();
        });

        // --- MONTAGEM DO PAINEL ESQUERDO ---
        leftPanel.getChildren().addAll(
                turnLabel,
                livesHUD,
                bonesHUD,
                spacerTop,
                leftCenterBox,
                spacerBottom,
                messageLabel,
                bellButton);

        // ====== TABULEIRO (80%) ======
        VBox boardArea = new VBox();
        boardArea.setPadding(new Insets(10));
        boardArea.setSpacing(10);
        boardArea.setStyle(
                "-fx-background-color: #2b2222;" +
                        "-fx-border-color: #6a4c4c;" +
                        "-fx-border-style: dashed;" +
                        "-fx-border-width: 3;");
        boardArea.setAlignment(Pos.TOP_CENTER);

        // Grids agora são "TOP" (cima) e "BOTTOM" (baixo)
        topGrid = createPlayerGrid("TOP"); // topo

        StackPane centerDivider = new StackPane();
        centerDivider.setMinHeight(4);
        centerDivider.setMaxHeight(4);
        centerDivider.setStyle("-fx-background-color: #3a2d2d; -fx-background-radius: 4;");

        bottomGrid = createPlayerGrid("BOTTOM"); // baixo

        playerHandP1 = createPlayerHand("HAND-P1");
        playerHandP1.setAlignment(Pos.CENTER);

        // ====== ÁREA DE DECKS (CANTO DIREITO INFERIOR) ======
        HBox deckArea = new HBox(30); // espaçamento entre os montes
        deckArea.setAlignment(Pos.BOTTOM_RIGHT);
        deckArea.setPadding(new Insets(0, 30, 15, 0));
        deckArea.setPickOnBounds(false);

        // Monte de Criaturas
        StackPane deckCreatures = createDeckPlaceholder(
                "DeckCriaturas",
                "/img/regular/backs/common.png",
                "Criaturas");

        // Monte de Esquilos
        StackPane deckSquirrels = createDeckPlaceholder(
                "DeckEsquilos",
                "/img/regular/backs/squirrel.png",
                "Esquilos");

        deckArea.getChildren().addAll(deckCreatures, deckSquirrels);

        // ====== JUNTA GRID + DECKS PARA DEIXAR NO CANTO INFERIOR DIREITO ======
        StackPane boardWithDecks = new StackPane();
        boardWithDecks.getChildren().addAll(bottomGrid, deckArea);
        StackPane.setAlignment(deckArea, Pos.BOTTOM_RIGHT);

        // Monta estrutura geral
        boardArea.getChildren().addAll(topGrid, centerDivider, boardWithDecks, playerHandP1);

        // ====== COMPOSIÇÃO E PROPORÇÕES ======
        gameLayout.getChildren().addAll(leftPanel, boardArea);

        rootPane.widthProperty().addListener((obs, oldW, newW) -> {
            double total = newW.doubleValue();
            leftPanel.setPrefWidth(total * 0.20);
            boardArea.setPrefWidth(total * 0.80);
        });

        // ===== MENUS =====
        createMenuOverlay();
        createSettingsBox();
        createInstructionsBox();
        createSurrenderBox();
        createLifeLostOverlay();

        rootPane.getChildren().addAll(gameLayout, menuOverlay, settingsBox, instructionsBox, surrenderBox, lifeLostOverlay);

        // ===== TROCA DE CONTEÚDO DA CENA =====
        Scene scene = stage.getScene();
        scene.setRoot(rootPane);

        // Key Listener para ESC
        scene.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                toggleMenu();
            }
        });

        // guarda o layout da tela de jogo para voltar depois da WaitingScreen
        this.gameRoot = rootPane;

        updateTurnLabelFromGame();

        // Adicionando cartas iniciais à mão do Player 1 ao começar o jogo:
        for (Card card : game.getPlayer1().getHand()) {
            addCardToHandBox(playerHandP1, card);
        }

        refreshBonesHUD();
        refreshLivesHUD();
        refreshItemsHUD();
        refreshBoardFromGame();
        refreshScaleFromGame(); // balança já começa coerente com o estado do jogo
    }

    // ==========================================================
    // === MÉTODOS AUXILIARES ===
    // ==========================================================


    // Atualiza a posição visual da balança vertical com base no gameScale e flippedView.
    private void refreshScaleFromGame() {
        if (scaleContainer == null || scaleMarker == null)
            return;

        int scaleValue = game.getGameScale(); // -5 .. +5
        int min = -5;
        int max = 5;
        int range = max - min; // 10

        double totalHeight = scaleContainer.getHeight() - 40; // padding top/bottom
        if (totalHeight <= 0)
            return;

        // Inverte visualmente quando a câmera está flipada
        int visualValue = flippedView ? -scaleValue : scaleValue;

        double step = totalHeight / range; // altura entre cada "ponto"
        double offset = visualValue * step;

        // Sistema de coordenadas JavaFX: Y positivo é para baixo.
        // visualValue > 0 -> desce; visualValue < 0 -> sobe.
        scaleMarker.setTranslateY(-offset);
    }

    /**
     * Cria um grid 2 linhas x 4 colunas (8 slots) para uma faixa do tabuleiro.
     * prefix: "TOP" (cima) ou "BOTTOM" (baixo) — usado na ID dos slots.
     */
    private GridPane createPlayerGrid(String prefix) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        int id = 0;
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 4; c++) {
                String imagePath;
                // seta e pata nos devidos lugares
                if (prefix.equals("TOP")) {
                    imagePath = (r == 0) ? "/img/arrow.png" : "/img/paw.png";
                } else { // BOTTOM
                    imagePath = (r == 0) ? "/img/paw.png" : "/img/arrow.png";
                }
                StackPane slot = createPlaceholder(prefix + "-" + id, imagePath);
                slot.getProperties().put("row", r);
                slot.getProperties().put("col", c);
                GridPane.setValignment(slot, VPos.CENTER);
                grid.add(slot, c, r);
                id++;
            }
        }

        return grid;
    }

    /** Cria um slot para carta (placeholder). */
    private StackPane createPlaceholder(String id, String imagePath) {
        StackPane slot = new StackPane();
        slot.setId(id);
        slot.setAlignment(Pos.CENTER);
        slot.setStyle(
                "-fx-background-color: rgba(80,60,60,0.35);" +
                        "-fx-border-color: #8a6a6a;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 2;");

        // Define a imagem de placeholder
        setImagePlaceholder(slot, imagePath);

        slot.minWidthProperty().bind(cardWidth);
        slot.prefWidthProperty().bind(cardWidth);
        slot.maxWidthProperty().bind(cardWidth);

        slot.minHeightProperty().bind(cardHeight);
        slot.prefHeightProperty().bind(cardHeight);
        slot.maxHeightProperty().bind(cardHeight);

        // Hover
        slot.setOnMouseEntered(e -> slot.setStyle(
                "-fx-background-color: rgba(120,90,90,0.45);" +
                        "-fx-border-color: #b08a8a;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 2"));
        slot.setOnMouseExited(e -> slot.setStyle(
                "-fx-background-color: rgba(80,60,60,0.35);" +
                        "-fx-border-color: #8a6a6a;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 2;"));

        // clique no slot tenta posicionar a carta selecionada
        // clique no slot tenta posicionar a carta selecionada
        slot.setOnMouseClicked(e -> onSlotClicked(slot));

        return slot;
    }

    // Define uma imagem em um StackPane (placeholder de seta/pata ou carta vazia)
    private void setImagePlaceholder(StackPane slot, String resourcePath) {
        Image img = new Image(
                getClass().getResource(resourcePath).toExternalForm(),
                false);
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.fitWidthProperty().unbind();
        iv.fitHeightProperty().unbind();

        String id = slot.getId();
        if (resourcePath.contains("arrow")) {
            int inset = 25;
            StackPane.setMargin(iv, new Insets(inset));
            iv.fitWidthProperty().bind(slot.widthProperty().subtract(inset * 2));
            iv.fitHeightProperty().bind(slot.heightProperty().subtract(inset * 2));
            iv.setOpacity(0.4);
            if (isTopSlot(id)) {
                // topo fica "de cabeça para baixo"
                iv.setRotate(180);
            }
        } else if (resourcePath.contains("paw")) {
            iv.fitWidthProperty().bind(slot.widthProperty());
            iv.fitHeightProperty().bind(slot.heightProperty());
            iv.setOpacity(0.4);
            if (isTopSlot(id)) {
                iv.setRotate(180);
            }
        } else {
            iv.fitWidthProperty().bind(slot.widthProperty());
            iv.fitHeightProperty().bind(slot.heightProperty());
        }

        slot.getChildren().setAll(iv);
    }

    // === mensagens de ajuda
    private void showMessage(String text) {
        if (text == null || text.isEmpty())
            return;

        messageLabel.setText(text);
        messageLabel.setOpacity(1);
        messageLabel.setVisible(true);

        // para Timer anterior se existir
        if (messageHideTimer != null)
            messageHideTimer.stop();

        // espera 2s antes do fade
        messageHideTimer = new PauseTransition(Duration.seconds(2));
        messageHideTimer.setOnFinished(e -> {
            // anima fade-out
            FadeTransition fade = new FadeTransition(Duration.seconds(0.7), messageLabel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> {
                messageLabel.setVisible(false);
                messageLabel.setText("");
            });
            fade.play();
        });
        messageHideTimer.play();
    }

    // hud velas=================
    private void refreshLivesHUD() {
        Player current = game.getCurrentPlayer();
        livesValueLabel.setText(String.valueOf(current.getLives()));
    }

    // hud ossos=================
    private void refreshBonesHUD() {
        Player current = game.getCurrentPlayer();
        bonesValueLabel.setText(String.valueOf(current.getBones()));
    }

    // Configurações da toolTip
    private void hackTooltipStartTiming(Tooltip tooltip) {
        tooltip.setShowDelay(Duration.millis(100)); // Aparece em 100ms
        tooltip.setHideDelay(Duration.millis(50));  // Desaparece rápido
        tooltip.setShowDuration(Duration.seconds(5)); // Fica visível por 5s
    }

    // hud itens=================
    private void refreshItemsHUD() {
        itemStorageBox.getChildren().clear();
        Player current = game.getCurrentPlayer();

        // Cria 3 slots fixos para itens
        for (int i = 0; i < 3; i++) {
            StackPane slot = new StackPane();
            // Proporção 2:3 (50x75)
            slot.setPrefSize(50, 75);
            slot.setMinSize(50, 75);
            slot.setMaxSize(50, 75);

            slot.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-border-color: #554444; -fx-border-radius: 3;");

            if (i < current.getItems().size()) {
                items.Items item = current.getItems().get(i);
                ImageView iv = new ImageView(getItemImage(item.name()));
                iv.setFitWidth(40);
                iv.setFitHeight(60);
                iv.setPreserveRatio(true);

                // Adiciona evento de clique
                slot.setOnMouseClicked(e -> onItemClicked(item));
                slot.setCursor(Cursor.HAND);

                // Cria o tooltip com o nome do item
                Tooltip tooltip = new Tooltip(item.name());

                tooltip.setStyle(
                        "-fx-background-color: #333333;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-border-color: #776666;" +
                                "-fx-padding: 5px;"
                );

                // Opcional: Adiciona a descrição também se desejar
                // Tooltip tooltip = new Tooltip(item.name() + "\n" + item.description());

                // Melhora o timing de aparição
                hackTooltipStartTiming(tooltip);
                // Instala o tooltip no slot (StackPane), vincula
                Tooltip.install(slot, tooltip);
                // -----------------------------

                // Hover effect
                slot.setOnMouseEntered(e -> slot.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.1); -fx-border-color: #776666; -fx-border-radius: 3;"));
                slot.setOnMouseExited(e -> slot.setStyle(
                        "-fx-background-color: rgba(0,0,0,0.3); -fx-border-color: #554444; -fx-border-radius: 3;"));

                slot.getChildren().add(iv);
            }

            itemStorageBox.getChildren().add(slot);
        }
    }

    private Image getItemImage(String itemName) {
        try {
            // Tenta carregar a imagem do item baseada no nome
            // O caminho deve ser /items/NomeDoItem.jpg
            String path = "/items/" + itemName + ".jpg";
            return new Image(getClass().getResource(path).toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem do item: " + itemName);
            // Retorna uma imagem de fallback
            return new Image(getClass().getResource("/img/paw.png").toExternalForm());
        }
    }

    private void onItemClicked(items.Items item) {
        if (isPassingTurn) {
            showMessage("Cannot use items while turn is passing.");
            return;
        }
        // Items that require opponent card selection
        if (item instanceof items.Scissors) {
            if (item.canUse(game, game.getCurrentPlayer())) {
                currentItemSelectionMode = ItemSelectionMode.SELECTING_SCISSORS_TARGET;
                selectedItem = item;
                highlightOpponentCards(true);
                showMessage("Select an opponent's card to cut.");
            } else {
                showMessage("Cannot use " + item.name() + " right now.");
            }
        } else if (item instanceof items.Hook) {
            if (item.canUse(game, game.getCurrentPlayer())) {
                currentItemSelectionMode = ItemSelectionMode.SELECTING_HOOK_TARGET;
                selectedItem = item;
                highlightOpponentCards(true);
                showMessage("Select an opponent's card to hook.");
            } else {
                showMessage("Cannot use " + item.name() + " right now.");
            }
        } else {
            // Items with immediate effect (Hourglass, Pliers, etc.)
            if (item.canUse(game, game.getCurrentPlayer())) {
                item.use(game, game.getCurrentPlayer());
                // Removal is now handled by EventLogics via ITEM_USED event
                // game.getCurrentPlayer().getItems().remove(item);
                refreshItemsHUD();
                refreshBoardFromGame();
                refreshBonesHUD();
                refreshLivesHUD();
                refreshScaleFromGame();
            } else {
                showMessage("Cannot use " + item.name() + " right now.");
            }
        }
    }

    private void onSlotClicked(StackPane slot) {
        // Route based on current selection mode
        switch (currentItemSelectionMode) {
            case SELECTING_SCISSORS_TARGET -> handleScissorsSelection(slot);
            case SELECTING_HOOK_TARGET -> handleHookSelection(slot);
            case NORMAL -> dropCard(slot);
        }
    }

    private void handleScissorsSelection(StackPane slot) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        Card card = game.getBoard().getCard(coords[0], coords[1]);
        if (card != null && card instanceof CreatureCard) {
            // Check if it's an opponent's card
            if (isOpponentCard(coords[0])) {
                // Cut the card
                // Trigger item effect via GameLogic
                game.triggerItemEffect(selectedItem, game.getCurrentPlayer(), card);

                // Manual removal is no longer needed here as EventLogics handles it
                // if (selectedItem != null) {
                // game.getCurrentPlayer().getItems().remove(selectedItem);
                // }

                cancelItemSelection();
                refreshItemsHUD();
                refreshBoardFromGame();
                AudioController.playSFX("cut_card.wav");
                showMessage("Card cut!");
            } else {
                showMessage("Select an OPPONENT'S card.");
            }
        } else {
            showMessage("Select a valid creature card.");
        }
    }

    private void handleHookSelection(StackPane slot) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        Card card = game.getBoard().getCard(coords[0], coords[1]);
        if (card == null || !(card instanceof CreatureCard)) {
            showMessage("Select a valid creature card.");
            return;
        }

        // Verify it's an opponent's card
        if (!isOpponentCard(coords[0])) {
            showMessage("Select an OPPONENT'S card.");
            return;
        }

        // Determine target column (same column as hooked card)
        int targetCol = coords[1];

        // Determine target lines based on current player
        int playerPositioningLine = (game.getCurrentPlayer().getOrder() == 1) ? 3 : 0;
        int playerAttackLine = (game.getCurrentPlayer().getOrder() == 1) ? 2 : 1;

        // Check if there's space (at least one empty slot in the column)
        boolean positioningEmpty = game.getBoard().EmptySpace(playerPositioningLine, targetCol);
        boolean attackEmpty = game.getBoard().EmptySpace(playerAttackLine, targetCol);

        if (!positioningEmpty && !attackEmpty) {
            showMessage("No space available in that column!");
            return;
        }

        // Trigger item effect via GameLogic
        game.triggerItemEffect(selectedItem, game.getCurrentPlayer(), card);

        cancelItemSelection();
        refreshItemsHUD();
        refreshBoardFromGame();
        AudioController.playSFX("hook_pull.wav"); // Assuming sound exists or similar
        showMessage("Card hooked!");
    }

    // Helper method to check if a card at the given line belongs to the opponent
    private boolean isOpponentCard(int line) {
        if (game.getCurrentPlayer().getOrder() == 1) {
            // Current player is P1, opponent is P2 (lines 0 and 1)
            return (line == 0 || line == 1);
        } else {
            // Current player is P2, opponent is P1 (lines 2 and 3)
            return (line == 2 || line == 3);
        }
    }

    // Highlights all opponent cards with grey border
    private void highlightOpponentCards(boolean highlight) {
        for (Node n : topGrid.getChildren()) {
            if (n instanceof StackPane slot) {
                toggleOpponentCardHighlight(slot, highlight);
            }
        }
        for (Node n : bottomGrid.getChildren()) {
            if (n instanceof StackPane slot) {
                toggleOpponentCardHighlight(slot, highlight);
            }
        }
    }

    private void toggleOpponentCardHighlight(StackPane slot, boolean highlight) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        Card card = game.getBoard().getCard(coords[0], coords[1]);
        if (card == null || !(card instanceof CreatureCard))
            return;

        // Only highlight opponent's cards
        if (!isOpponentCard(coords[0]))
            return;

        // For Hook, also check if there's space in the target column
        if (currentItemSelectionMode == ItemSelectionMode.SELECTING_HOOK_TARGET) {
            int targetCol = coords[1];
            int playerPositioningLine = (game.getCurrentPlayer().getOrder() == 1) ? 3 : 0;
            int playerAttackLine = (game.getCurrentPlayer().getOrder() == 1) ? 2 : 1;

            boolean positioningEmpty = game.getBoard().EmptySpace(playerPositioningLine, targetCol);
            boolean attackEmpty = game.getBoard().EmptySpace(playerAttackLine, targetCol);

            // Can't hook if both slots are occupied
            if (!positioningEmpty && !attackEmpty) {
                return; // Don't highlight this card
            }
        }

        // Apply bright yellow border highlight
        if (highlight) {
            slot.setStyle(
                    "-fx-background-color: rgba(80,60,60,0.35);" +
                            "-fx-border-color: #FFD700;" + // Bright gold/yellow border
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-width: 4;" + // Even thicker border
                            "-fx-effect: dropshadow(gaussian, #FFD700, 8, 0.6, 0, 0);"); // Glow effect
        } else {
            // Reset to normal style
            slot.setStyle(
                    "-fx-background-color: rgba(80,60,60,0.35);" +
                            "-fx-border-color: #8a6a6a;" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-width: 2;");
        }
    }

    // Cancel item selection and reset state
    private void cancelItemSelection() {
        highlightOpponentCards(false);
        currentItemSelectionMode = ItemSelectionMode.NORMAL;
        selectedItem = null;
    }

    // === helpers para saber se é TOP ou BOTTOM ===
    private boolean isTopSlot(String slotId) {
        return slotId.startsWith("TOP");
    }

    private boolean isBottomSlot(String slotId) {
        return slotId.startsWith("BOTTOM");
    }

    // ============================================
    // Mao do jogador
    // ============================================
    private void addCardToHandBox(HBox handBox, Card card) {
        if (handBox.getChildren().size() >= 7) {
            System.out.println("Limite máximo de 7 cartas atingido.");
            return;
        }

        // 🔗 Garante que a carta na mão use cardWidth/cardHeight
        card.minWidthProperty().unbind();
        card.prefWidthProperty().unbind();
        card.maxWidthProperty().unbind();
        card.minHeightProperty().unbind();
        card.prefHeightProperty().unbind();
        card.maxHeightProperty().unbind();

        card.minWidthProperty().bind(cardWidth);
        card.prefWidthProperty().bind(cardWidth);
        card.maxWidthProperty().bind(cardWidth);

        card.minHeightProperty().bind(cardHeight);
        card.prefHeightProperty().bind(cardHeight);
        card.maxHeightProperty().bind(cardHeight);

        handBox.getChildren().add(card);
    }

    private HBox createPlayerHand(String prefix) {
        HBox hand = new HBox();
        hand.spacingProperty().bind(handSpacing);
        hand.setAlignment(Pos.CENTER_LEFT);
        hand.setId(prefix);

        hand.minWidthProperty().bind(cardWidth.multiply(7).add(handSpacing.multiply(6)));
        hand.prefWidthProperty().bind(cardWidth.multiply(7).add(handSpacing.multiply(6)));
        hand.maxWidthProperty().bind(cardWidth.multiply(7).add(handSpacing.multiply(6)));
        hand.minHeightProperty().bind(cardHeight);
        hand.prefHeightProperty().bind(cardHeight);
        hand.maxHeightProperty().bind(cardHeight);

        // Delegação de eventos

        // HOVER IN
        hand.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, e -> {
            Card card = pickCardFromEventTarget(e.getTarget());
            if (card != null) {
                for (Node sib : hand.getChildren()) {
                    if (sib != card) {
                        sib.setScaleX(1.0);
                        sib.setScaleY(1.0);
                        sib.setViewOrder(0);
                    }
                }
                AudioController.playSFX("hover-cards.wav");
                card.setViewOrder(-1);
                ScaleTransition st = new ScaleTransition(Duration.millis(140), card);
                st.setToX(1.25);
                st.setToY(1.25);
                st.setInterpolator(Interpolator.EASE_BOTH);
                st.play();
            }
        });

        // HOVER OUT
        hand.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            Card card = pickCardFromEventTarget(e.getTarget());
            if (card != null) {
                ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
                st.setToX(1.0);
                st.setToY(1.0);
                st.setInterpolator(Interpolator.EASE_BOTH);
                st.play();
                card.setViewOrder(0);
            }
        });

        // CLICK (seleção)
        hand.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            Card card = pickCardFromEventTarget(e.getTarget());
            if (card != null) {
                AudioController.playSFX("hover-cards.wav");
                if (selectedCardNode == card) {
                    clearSelection();
                } else {
                    selectCard(card);
                }
                e.consume();
            }
        });

        return hand;
    }

    // ==========================HELPERS
    private void updateTurnLabelFromGame() {
        Player current = game.getCurrentPlayer();
        turnLabel.setText(current.getName().toUpperCase());
    }

    // === helpers de seleção ===
    private void selectCard(Card card) {
        // Se já está em um processo de sacrifício, cancelar tudo
        if (currentSacrificeState != SacrificeState.NORMAL) {
            cancelSacrificeProcess();
        }

        clearSelection(); // Limpa a seleção visual antiga

        // É uma criatura com custo de sangue?
        if (card instanceof CreatureCard creature && creature.getBloodCost() > 0) {
            // pergunta pra GameLogic se tem sacrifício suficiente
            if (!game.canPayBloodCostWithCurrentBoard(creature)) {
                System.out.println("Não há criaturas suficientes no tabuleiro para sacrificar!");
                return;
            }

            // 1. Inicia o estado de sacrifício
            System.out.println("Iniciando modo de sacrifício para: " + creature.getName());
            currentSacrificeState = SacrificeState.AWAITING_SACRIFICE;
            cardToPlayAfterSacrifice = card; // Armazena a carta da mão
            sacrificeSlots.clear();
            sacrificeCards.clear();

            // 2. Destaca a carta da mão
            selectedCardNode = card;
            card.highlight(true);

            // 3. Destacar visualmente as cartas que PODEM ser sacrificadas
            highlightSacrificeableSlots(true);
        } else {
            // custo 0 ou não-criatura: seleção normal
            selectedCardNode = card;
            card.highlight(true);
        }
    }

    private void clearSelection() {
        if (selectedCardNode != null) {
            selectedCardNode.highlight(false);
        }
        selectedCardNode = null;
    }

    // === posicionar carta no slot ===
    private void dropCard(StackPane slot) {
        switch (currentSacrificeState) {
            case NORMAL -> // Tentar colocar carta de custo 0
                    placeCardNormal(slot);
            case AWAITING_SACRIFICE -> // Selecionar slot para sacrificar
                    selectSlotForSacrifice(slot);
            case AWAITING_PLACEMENT -> // Colocar a carta no slot após sacrifícios
                    placeCardOnSacrificeSlot(slot);
        }
    }

    // Lógica para o estado NORMAL
    private void placeCardNormal(StackPane slot) {
        if (selectedCardNode == null)
            return; // Nada selecionado

        // Pega coords e handIndex
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null) {
            System.out.println("Slot sem coordenadas lógicas.");
            return;
        }

        int handIndex = game.getCurrentPlayer().getHand().indexOf(selectedCardNode);
        if (handIndex == -1) {
            System.out.println("Carta selecionada não está na mão do jogador atual.");
            return;
        }

        // Usa o metodo da GameLogic
        GameLogic.PlaceCardResult result = game.tryPlaceCardFromCurrentPlayerHand(
                handIndex, coords[0], coords[1]);

        if (result == GameLogic.PlaceCardResult.SUCCESS) {
            AudioController.playSFX("place_card.wav");
            clearSelection();
            refreshHandsFromGame();
            refreshBoardFromGame();
            refreshBonesHUD();
        } else if (result == GameLogic.PlaceCardResult.REQUIRES_SACRIFICE_SELECTION) {
            // UI tentou colocar direto, mas requer sacrifício
            System.out.println("Iniciando modo de sacrifício via dropCard.");
            selectCard(selectedCardNode); // Inicia o processo de sacrifício
        } else {
            System.out.println("Não foi possível colocar a carta: " + result);
        }
    }

    // Lógica para o estado AWAITING_SACRIFICE
    private void selectSlotForSacrifice(StackPane slot) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        Card cardOnBoard = game.getBoard().getCard(coords[0], coords[1]);
        if (cardOnBoard == null || !(cardOnBoard instanceof CreatureCard creature)) {
            System.out.println("Slot inválido para sacrifício (vazio ou não criatura).");
            return;
        }
        if (sacrificeCards.contains(creature)) {
            System.out.println("Criatura já selecionada para sacrifício.");
            return;
        }

        // 2. Adiciona à lista de sacrifício
        sacrificeSlots.add(slot);
        sacrificeCards.add(creature);

        // 3. Feedback visual (seta no chão)

        slot.getProperties().put("original_card", cardOnBoard);

        // Determine correct placeholder image (Arrow or Paw)
        String id = slot.getId();
        Integer row = (Integer) slot.getProperties().get("row");
        String imagePath = "/img/arrow.png"; // Default

        if (row != null) {
            if (isTopSlot(id)) {
                imagePath = (row == 0) ? "/img/arrow.png" : "/img/paw.png";
            } else { // BOTTOM
                imagePath = (row == 0) ? "/img/paw.png" : "/img/arrow.png";
            }
        }

        setImagePlaceholder(slot, imagePath);

        System.out.println("Sacrifício selecionado: " + creature.getName() +
                ". Total: " + sacrificeCards.size());

        // Efeito sonoro ao sacrificar
        AudioController.playSFX("delete_card.wav");

        CreatureCard cardToPlay = (CreatureCard) cardToPlayAfterSacrifice;
        if (sacrificeCards.size() == cardToPlay.getBloodCost()) {
            // custo atingido -> modo de posicionamento
            currentSacrificeState = SacrificeState.AWAITING_PLACEMENT;
            System.out.println("Custo atingido. Selecione onde colocar a carta.");

            highlightSacrificeableSlots(false);
            highlightPlacementSlots(true);
        }
    }

    //  Lógica para o estado AWAITING_PLACEMENT
    private void placeCardOnSacrificeSlot(StackPane slot) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        int handIndex = game.getCurrentPlayer().getHand().indexOf(cardToPlayAfterSacrifice);
        if (handIndex == -1) {
            System.out.println("Erro: Carta da mão sumiu?");
            cancelSacrificeProcess();
            return;
        }

        GameLogic.PlaceCardResult result = game.tryPlaceCardWithSacrifices(
                handIndex,
                coords[0],
                coords[1],
                sacrificeCards);

        if (result != GameLogic.PlaceCardResult.SUCCESS) {
            System.out.println("Erro ao tentar colocar com sacrifício: " + result);
            // jogador ainda pode clicar em outro slot
            return;
        }

        AudioController.playSFX("place_card.wav");

        // 4. Limpa tudo e redesenha, apenas se tiver sucesso
        highlightPlacementSlots(false);
        cancelSacrificeProcess();
        refreshHandsFromGame();
        refreshBoardFromGame();
        refreshBonesHUD();
    }

    // Metodo para cancelar t odo o processo
    private void cancelSacrificeProcess() {
        System.out.println("Processo de sacrifício cancelado.");

        if (cardToPlayAfterSacrifice != null) {
            cardToPlayAfterSacrifice.highlight(false);
        }

        highlightSacrificeableSlots(false);
        highlightPlacementSlots(false);

        // Restaura a aparência dos slots de sacrifício
        for (StackPane slot : sacrificeSlots) {
            Card card = (Card) slot.getProperties().get("original_card");
            if (card != null) {
                slot.getChildren().setAll(card);
                slot.getProperties().remove("original_card");
            } else {
                resetSlotToPlaceholder(slot);
            }
        }

        currentSacrificeState = SacrificeState.NORMAL;
        cardToPlayAfterSacrifice = null;
        sacrificeSlots.clear();
        sacrificeCards.clear();

        if (selectedCardNode != null) {
            selectedCardNode.highlight(false);
            selectedCardNode = null;
        }
    }

    // --- HELPERS DE DESTAQUE VISUAL ---
    private void highlightSacrificeableSlots(boolean highlight) {
        for (Node n : topGrid.getChildren()) {
            if (n instanceof StackPane s)
                toggleSlotHighlight(s, highlight);
        }
        for (Node n : bottomGrid.getChildren()) {
            if (n instanceof StackPane s)
                toggleSlotHighlight(s, highlight);
        }
    }

    private void toggleSlotHighlight(StackPane slot, boolean highlight) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        Board.SpaceType spaceType = game.getBoard().getSpaceType(coords[0], coords[1]);
        boolean isCurrentPlayerPos = false;

        if (game.getCurrentPlayer().getOrder() == 1) {
            if (spaceType == Board.SpaceType.PLAYER_1_POSITIONING || spaceType == Board.SpaceType.ATTACK_PLAYER1) {
                isCurrentPlayerPos = true;
            }
        } else if (game.getCurrentPlayer().getOrder() == 2) {
            if (spaceType == Board.SpaceType.PLAYER_2_POSITIONING || spaceType == Board.SpaceType.ATTACK_PLAYER2) {
                isCurrentPlayerPos = true;
            }
        }

        if (isCurrentPlayerPos) {
            Card cardOnBoard = game.getBoard().getCard(coords[0], coords[1]);
            if (cardOnBoard != null) {
                if (highlight) {
                    slot.setEffect(new javafx.scene.effect.InnerShadow(20,
                            Color.rgb(247, 78, 17)));
                } else {
                    slot.setEffect(null);
                }
            }
        }
    }

    private void highlightPlacementSlots(boolean highlight) {
        for (Node n : topGrid.getChildren()) {
            if (n instanceof StackPane s)
                togglePlacementHighlight(s, highlight);
        }
        for (Node n : bottomGrid.getChildren()) {
            if (n instanceof StackPane s)
                togglePlacementHighlight(s, highlight);
        }
    }

    private void togglePlacementHighlight(StackPane slot, boolean highlight) {
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null)
            return;

        Board.SpaceType spaceType = game.getBoard().getSpaceType(coords[0], coords[1]);
        boolean isCurrentPlayerPos = false;

        if (game.getCurrentPlayer().getOrder() == 1 &&
                spaceType == Board.SpaceType.PLAYER_1_POSITIONING) {
            isCurrentPlayerPos = true;
        } else if (game.getCurrentPlayer().getOrder() == 2 &&
                spaceType == Board.SpaceType.PLAYER_2_POSITIONING) {
            isCurrentPlayerPos = true;
        }

        if (!isCurrentPlayerPos)
            return;

        Card cardOnBoard = game.getBoard().getCard(coords[0], coords[1]);
        boolean isSacrificeSlot = sacrificeSlots.contains(slot);

        if (cardOnBoard == null || isSacrificeSlot) {
            if (highlight) {
                slot.setEffect(new javafx.scene.effect.InnerShadow(20,
                        Color.rgb(255, 197, 176)));
            } else {
                slot.setEffect(null);
            }
        } else {
            if (highlight) {
                slot.setEffect(new javafx.scene.effect.InnerShadow(10, Color.DARKRED));
            } else {
                slot.setEffect(null);
            }
        }
    }

    private Card pickCardFromEventTarget(Object target) {
        if (!(target instanceof Node n))
            return null;
        while (n != null && !(n instanceof Card)) {
            n = n.getParent();
        }
        return (n instanceof Card c) ? c : null;
    }

    // Criação da Área de armazenamento de cartas (decks)
    private StackPane createDeckPlaceholder(String id, String imagePath, String deckType) {
        StackPane deck = new StackPane();
        deck.setId(id);
        deck.setAlignment(Pos.CENTER);

        //  Largura do deck = largura da carta
        deck.minWidthProperty().bind(cardWidth);
        deck.prefWidthProperty().bind(cardWidth);
        deck.maxWidthProperty().bind(cardWidth);

        //  Altura do deck = 1.2x altura da carta (pra parecer um monte)
        deck.minHeightProperty().bind(cardHeight.multiply(1.2));
        deck.prefHeightProperty().bind(cardHeight.multiply(1.2));
        deck.maxHeightProperty().bind(cardHeight.multiply(1.2));

        // camadas visuais do deck
        for (int i = 0; i < 3; i++) {
            Image img = new Image(getClass().getResource(imagePath).toExternalForm(), false);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.fitWidthProperty().bind(cardWidth);
            iv.fitHeightProperty().bind(cardHeight);
            iv.setTranslateY(-i * 3);
            deck.getChildren().add(iv);
        }

        // hover
        deck.setOnMouseEntered(e -> {
            deck.setScaleX(1.08);
            deck.setScaleY(1.08);
            deck.setCursor(Cursor.HAND);
            deck.setEffect(new DropShadow(20, Color.rgb(220, 180, 180, 0.5)));
        });
        deck.setOnMouseExited(e -> {
            deck.setScaleX(1);
            deck.setScaleY(1);
            deck.setEffect(null);
        });

        // Lógica de Compra de Cartas
        deck.setOnMouseClicked(e -> {
            AudioController.playSFX("draw.wav");
            GameLogic.DrawResult result;
            if ("Esquilos".equals(deckType)) {
                result = game.drawFromSquirrelDeckCurrentPlayer();
            } else {
                result = game.drawFromMainDeckCurrentPlayer();
            }

            switch (result) {
                case SUCCESS -> refreshHandsFromGame();
                case ALREADY_DREW_THIS_TURN -> showMessage("Você só pode comprar uma carta por turno.");
                case DECK_EMPTY -> showMessage("Este deck está vazio.");
            }
        });

        return deck;
    }

    // Variáveis para a tela de Vida Perdida
    private VBox lifeLostOverlay;
    private Label lifeLostTitle;
    private Label lifeLostSubtitle;

    // Variáveis para controle de fluxo de vida perdida
    private boolean lifeLostTriggered = false; // Flag para saber se o evento ocorreu
    private Player playerWhoLostLife = null;   // Quem perdeu
    private Runnable onLifeLostClosed = null;  // Ação ao fechar a tela

    private void createLifeLostOverlay() {
        lifeLostOverlay = new VBox();
        lifeLostOverlay.setAlignment(Pos.CENTER);
        // Fundo escuro levemente avermelhado para indicar dano
        lifeLostOverlay.setStyle("-fx-background-color: rgba(20, 0, 0, 0.95);");
        lifeLostOverlay.setVisible(false);
        lifeLostOverlay.setSpacing(20);

        // Título dramático
        lifeLostTitle = new Label("A CANDLE IS EXTINGUISHED");
        lifeLostTitle.setTextFill(Color.web("#d44e4e")); // Vermelho desbotado
        lifeLostTitle.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                rootPane.widthProperty().divide(25).asString(), "px;"
        ));

        // Subtítulo
        lifeLostSubtitle = new Label("One life remains...");
        lifeLostSubtitle.setTextFill(Color.BEIGE);
        lifeLostSubtitle.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ",
                rootPane.widthProperty().divide(60).asString(), "px;"
        ));


        Label continueText = new Label("[ CLICK TO CONTINUE ]");
        continueText.setTextFill(Color.GRAY);
        continueText.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ",
                rootPane.widthProperty().divide(80).asString(), "px;"
        ));

        // Animação de piscar para o texto de continuar
        FadeTransition fade = new FadeTransition(Duration.seconds(1.0), continueText);
        fade.setFromValue(1.0);
        fade.setToValue(0.4);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Adiciona componentes (Adicione skullIcon aqui se criar)
        lifeLostOverlay.getChildren().addAll(lifeLostTitle, lifeLostSubtitle, continueText);

        // Evento para fechar a tela ao clicar
        lifeLostOverlay.setOnMouseClicked(e -> closeLifeLostScreen());

        // Também permite fechar apertando Espaço ou Enter se a tela tiver foco
        lifeLostOverlay.setFocusTraversable(true);
        lifeLostOverlay.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                closeLifeLostScreen();
            }
        });
    }

    private void showLifeLostScreen(Player attackedPlayer, Runnable action) {
        this.onLifeLostClosed = action; // Guarda a ação (ir para WaitingScreen)

        // Atualiza textos
        lifeLostTitle.setText(attackedPlayer.getName().toUpperCase() + " LOST A LIFE");

        if (attackedPlayer.getLives() == 1) {
            lifeLostSubtitle.setText("The flame flickers... Only 1 life remains.");
        } else {
            lifeLostSubtitle.setText(attackedPlayer.getLives() + " lives remain.");
        }

        lifeLostOverlay.setVisible(true);
        lifeLostOverlay.toFront();
        lifeLostOverlay.requestFocus();

         AudioController.playSFX("candle_blow.wav");
    }

    private void closeLifeLostScreen() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), lifeLostOverlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            lifeLostOverlay.setVisible(false);
            lifeLostOverlay.setOpacity(1.0);

            // troca o turno
            if (onLifeLostClosed != null) {
                onLifeLostClosed.run();
                onLifeLostClosed = null; // Limpa para não repetir
            }
        });
        fadeOut.play();
    }


    // =============================================================================
    private void passTurn() {
        System.out.println("Pass turn.");

        // *** TRAVA contra cliques múltiplos ***
        if (isPassingTurn) {
            return; // ignora clique se já estiver processando o turno
        }
        isPassingTurn = true;
        if (bellButton != null) {
            bellButton.setDisable(true);
        }

        if (currentSacrificeState != SacrificeState.NORMAL) {
            cancelSacrificeProcess();
        }

        clearSelection();

        // Reseta as flags antes de calcular o turno
        this.lifeLostTriggered = false;
        this.playerWhoLostLife = null;

        // === ETAPA 1: EXECUTAR A LÓGICA DE ATAQUE ===
        game.executeEndOfTurn();

        // === ETAPA 2: ATUALIZAR O TABULEIRO VISUALMENTE ===
        refreshBoardFromGame();
        refreshScaleFromGame(); // balança reflete o dano antes do flip

        // === ETAPA 3: CRIAR A PAUSA ===
        PauseTransition delay = new PauseTransition(Duration.seconds(2.0));

        // === ETAPA 4: DEFINIR O QUE ACONTECE DEPOIS DA PAUSA ===
        delay.setOnFinished(event -> {
            // 5. Verifica se o evento de vida perdida foi disparado durante o executeEndOfTurn
            if (lifeLostTriggered && playerWhoLostLife != null) {
                // Se SIM: Mostra a tela de perda de vida.
                // Quando fechar, executa finalizeTurnSwitch.
                showLifeLostScreen(playerWhoLostLife, this::finalizeTurnSwitch);
            } else {
                // Se NÃO: Vai direto para a troca de turno padrão.
                finalizeTurnSwitch();
            }
        });

        delay.play();
    }

    //  Fazer a troca efetiva de tela/jogador
    private void finalizeTurnSwitch() {
        // 1. Troca o jogador na lógica do jogo
        game.switchToNextPlayer();

        // 2. Alterna orientação visual
        flippedView = !flippedView;

        // 3. Atualiza UI com base no novo jogador
        updateTurnLabelFromGame();
        refreshHandsFromGame();
        refreshBoardFromGame();
        refreshBonesHUD();
        refreshItemsHUD();
        refreshScaleFromGame();
        refreshLivesHUD();
        clearSelection();

        // 4. Configura a WaitingScreen (Tela de "Passe o computador")
        String nextPlayer = game.getCurrentPlayer().getName();
        WaitingScreen waiting = new WaitingScreen();
        Parent waitingRoot = waiting.createRoot(nextPlayer);

        Scene scene = gameWindow.getScene();
        scene.setRoot(waitingRoot);

        // 5. Configura retorno ao pressionar tecla
        scene.setOnKeyPressed(e2 -> returnToGame());
    }

    private void returnToGame() {
        Scene scene = gameWindow.getScene();

        // Restaurar listener do ESC
        scene.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                toggleMenu();
            }
        });

        scene.setRoot(gameRoot);

        // libera clique de turno novamente
        isPassingTurn = false;
        if (bellButton != null) {
            bellButton.setDisable(false);
        }
        // Volta a musica de fundo
        AudioController.resumeBGM();
    }

    // ===========================
    // === REFRESH DO TABULEIRO ==
    // ===========================
    private StackPane findSlot(String prefix, int row, int col) {
        GridPane grid = prefix.equals("TOP") ? topGrid : bottomGrid;
        for (Node n : grid.getChildren()) {
            if (n instanceof StackPane slot) {
                Integer r = (Integer) slot.getProperties().get("row");
                Integer c = (Integer) slot.getProperties().get("col");
                if (r != null && c != null && r == row && c == col) {
                    return slot;
                }
            }
        }
        return null;
    }

    // Reseta um slot para seta/pata original
    private void resetSlotToPlaceholder(StackPane slot) {
        Integer row = (Integer) slot.getProperties().get("row");
        if (row == null)
            return;
        String id = slot.getId();
        String imagePath;
        if (isTopSlot(id)) {
            imagePath = (row == 0) ? "/img/arrow.png" : "/img/paw.png";
        } else { // BOTTOM
            imagePath = (row == 0) ? "/img/paw.png" : "/img/arrow.png";
        }
        setImagePlaceholder(slot, imagePath);
        slot.getProperties().remove("occupied");
    }

    private void clearAllBoardSlots() {
        for (Node n : topGrid.getChildren()) {
            if (n instanceof StackPane sp) {
                resetSlotToPlaceholder(sp);
            }
        }
        for (Node n : bottomGrid.getChildren()) {
            if (n instanceof StackPane sp) {
                resetSlotToPlaceholder(sp);
            }
        }
    }

    // Converte uma posição lógica do Board (line, col) para o slot VISUAL correto.
    private StackPane getVisualSlotForBoardPosition(int line, int col) {
        boolean isTop;
        int visualRow;

        if (!flippedView) {
            if (line == 0) {
                isTop = true;
                visualRow = 0;
            } else if (line == 1) {
                isTop = true;
                visualRow = 1;
            } else if (line == 2) {
                isTop = false;
                visualRow = 0;
            } else {
                isTop = false;
                visualRow = 1;
            }
        } else {
            if (line == 0) {
                isTop = false;
                visualRow = 1;
            } else if (line == 1) {
                isTop = false;
                visualRow = 0;
            } else if (line == 2) {
                isTop = true;
                visualRow = 1;
            } else {
                isTop = true;
                visualRow = 0;
            }
        }

        return findSlot(isTop ? "TOP" : "BOTTOM", visualRow, col);
    }

    // Converte um slot VISUAL clicado para a posição REAL do Board (line, col).
    private int[] getBoardPositionFromSlot(StackPane slot) {
        Integer visualRow = (Integer) slot.getProperties().get("row");
        Integer col = (Integer) slot.getProperties().get("col");
        if (visualRow == null || col == null) {
            return null;
        }

        boolean top = isTopSlot(slot.getId());
        int line;

        if (!flippedView) {
            if (top) {
                line = (visualRow == 0) ? 0 : 1;
            } else {
                line = (visualRow == 0) ? 2 : 3;
            }
        } else {
            if (!top) {
                line = (visualRow == 0) ? 1 : 0;
            } else {
                line = (visualRow == 0) ? 3 : 2;
            }
        }

        return new int[] { line, col };
    }

    // =============== REFRESHS
    private void refreshHandsFromGame() {
        Player current = game.getCurrentPlayer();
        playerHandP1.getChildren().clear();
        for (Card card : current.getHand()) {
            addCardToHandBox(playerHandP1, card);
        }
    }

    private void refreshBoardFromGame() {
        clearAllBoardSlots();

        for (int line = 0; line < 4; line++) {
            for (int col = 0; col < 4; col++) {
                Card card = game.getBoard().getCard(line, col);
                if (card == null)
                    continue;

                StackPane slot = getVisualSlotForBoardPosition(line, col);
                if (slot == null)
                    continue;

                // Carta no tabuleiro segue o tamanho do slot
                card.minWidthProperty().unbind();
                card.prefWidthProperty().unbind();
                card.maxWidthProperty().unbind();
                card.minHeightProperty().unbind();
                card.prefHeightProperty().unbind();
                card.maxHeightProperty().unbind();

                card.minWidthProperty().bind(slot.widthProperty());
                card.prefWidthProperty().bind(slot.widthProperty());
                card.maxWidthProperty().bind(slot.widthProperty());

                card.minHeightProperty().bind(slot.heightProperty());
                card.prefHeightProperty().bind(slot.heightProperty());
                card.maxHeightProperty().bind(slot.heightProperty());

                slot.getChildren().setAll(card);
                slot.getProperties().put("occupied", Boolean.TRUE);
            }
        }
    }
    // Fim

    // ==========================================================
    // === MENUS E OVERLAYS ===
    // ==========================================================

    private void toggleMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    private void openMenu() {
        isMenuOpen = true;
        menuOverlay.setVisible(true);
        settingsBox.setVisible(false);
        instructionsBox.setVisible(false);
        surrenderBox.setVisible(false);
    }

    private void closeMenu() {
        isMenuOpen = false;
        menuOverlay.setVisible(false);
        settingsBox.setVisible(false);
        instructionsBox.setVisible(false);
        surrenderBox.setVisible(false);
    }

    private void createMenuOverlay() {
        menuOverlay = new VBox();
        menuOverlay.setAlignment(Pos.CENTER);
        menuOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        menuOverlay.setVisible(false);
        menuOverlay.spacingProperty().bind(rootPane.heightProperty().divide(36));

        Label title = new Label("PAUSE");
        title.setTextFill(Color.BEIGE);
        title.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(19.2).asString(), "px;"));

        Button btnResume = createMenuButton("Resume");
        Button btnInstructions = createMenuButton("Instructions");
        Button btnSettings = createMenuButton("Settings");
        Button btnSurrender = createMenuButton("Surrender");

        btnResume.setOnAction(e -> closeMenu());
        btnInstructions.setOnAction(e -> showInstructionsView());
        btnSettings.setOnAction(e -> showSettingsView());
        btnSurrender.setOnAction(e -> showSurrenderView());

        menuOverlay.getChildren().addAll(title, btnResume, btnInstructions, btnSettings, btnSurrender);
    }

    private void createSettingsBox() {
        settingsBox = new VBox();
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        settingsBox.setVisible(false);
        settingsBox.spacingProperty().bind(rootPane.heightProperty().divide(36));

        Label lblTitle = new Label("Settings");
        lblTitle.setTextFill(Color.BEIGE);
        lblTitle.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(48).asString(), "px;"));

        // Graphics - Brightness
        VBox graphicsBox = new VBox(10);
        graphicsBox.setAlignment(Pos.CENTER);
        Label lblGraphics = new Label("Screen Brightness");
        lblGraphics.setTextFill(Color.BEIGE);
        lblGraphics.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ", rootPane.widthProperty().divide(96).asString(), "px;"));

        Slider brightnessSlider = new Slider(-0.8, 0.2, 0.0);
        brightnessSlider.maxWidthProperty().bind(rootPane.widthProperty().multiply(0.156));
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightnessAdjust.setBrightness(newVal.doubleValue());
        });

        graphicsBox.getChildren().addAll(lblGraphics, brightnessSlider);

        // Audio
        VBox audioBox = new VBox(10);
        audioBox.setAlignment(Pos.CENTER);
        Label lblAudio = new Label("Overall Sound");
        lblAudio.setTextFill(Color.BEIGE);
        lblAudio.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ", rootPane.widthProperty().divide(96).asString(), "px;"));

        Slider volumeSlider = new Slider(0, 100, 60);
        volumeSlider.maxWidthProperty().bind(rootPane.widthProperty().multiply(0.156));
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            AudioController.setBGMVolume(newVal.doubleValue() / 100.0);
        });

        audioBox.getChildren().addAll(lblAudio, volumeSlider);

        Button btnBack = createMenuButton("Back");
        btnBack.setOnAction(e -> openMenu()); // Back to main pause menu

        settingsBox.getChildren().addAll(lblTitle, graphicsBox, audioBox, btnBack);
    }

    private void createSurrenderBox() {
        surrenderBox = new VBox();
        surrenderBox.setAlignment(Pos.CENTER);
        surrenderBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        surrenderBox.setVisible(false);
        surrenderBox.spacingProperty().bind(rootPane.heightProperty().divide(36));

        Label lblTitle = new Label("Surrender?");
        lblTitle.setTextFill(Color.BEIGE);
        lblTitle.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(48).asString(), "px;"));

        Label lblText = new Label("Are you sure you want to surrender and return to the main menu?");
        lblText.setTextFill(Color.BEIGE);
        lblText.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-font-family: 'Consolas'; -fx-font-size: ", rootPane.widthProperty().divide(96).asString(), "px;"));

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button btnYes = createMenuButton("Yes");
        Button btnNo = createMenuButton("No");

        btnYes.setOnAction(e -> {
            // Return to Main Menu
            // gameWindow.close(); // Not needed if we just switch scene
            AudioController.startBGM("dark_bg_edited.wav");
            AudioController.setBGMVolume(0.6);
            new MenuScreen().start(gameWindow);
        });

        btnNo.setOnAction(e -> openMenu());

        buttons.getChildren().addAll(btnYes, btnNo);
        surrenderBox.getChildren().addAll(lblTitle, lblText, buttons);
    }

    private void createInstructionsBox() {
        instructionsBox = new VBox();
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        instructionsBox.setVisible(false);
        instructionsBox.spacingProperty().bind(rootPane.heightProperty().divide(36));

        instructionsContentBox = new VBox();
        instructionsContentBox.setAlignment(Pos.CENTER);
        instructionsContentBox.spacingProperty().bind(rootPane.heightProperty().divide(50));

        instructionsBox.getChildren().add(instructionsContentBox);
    }

    private void showInstructionsView() {
        menuOverlay.setVisible(false);
        settingsBox.setVisible(false);
        surrenderBox.setVisible(false);
        instructionsBox.setVisible(true);
        showInstructionsOverview();
    }

    private void showSettingsView() {
        menuOverlay.setVisible(false);
        settingsBox.setVisible(true);
        surrenderBox.setVisible(false);
        instructionsBox.setVisible(false);
    }

    private void showSurrenderView() {
        menuOverlay.setVisible(false);
        settingsBox.setVisible(false);
        surrenderBox.setVisible(true);
        instructionsBox.setVisible(false);
    }

    // --- Instructions Logic (Adapted from MenuScreen) ---
    private void showInstructionsOverview() {
        instructionsContentBox.getChildren().clear();

        Label lblTitle = new Label("Overview");
        lblTitle.setTextFill(Color.BEIGE);
        lblTitle.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(48).asString(), "px;"));

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
        lblText.maxWidthProperty().bind(rootPane.widthProperty().multiply(0.6));
        lblText.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ",
                        rootPane.widthProperty().divide(100).asString(), "px;"));

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

        btnBack.setOnAction(e -> openMenu());

        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(btnBoard, btnItems, btnDecks, btnSigils, btnBalance, btnBack);

        instructionsContentBox.getChildren().addAll(lblTitle, lblText, buttonsBox);
    }

    private void showInstructionDetail(String title, String content) {
        instructionsContentBox.getChildren().clear();

        Label lblTitle = new Label(title);
        lblTitle.setTextFill(Color.BEIGE);
        lblTitle.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat(
                        "-fx-font-family: 'Serif'; -fx-font-weight: bold; -fx-font-size: ",
                        rootPane.widthProperty().divide(48).asString(), "px;"));

        Label lblText = new Label(content);
        lblText.setTextFill(Color.BEIGE);
        lblText.setWrapText(true);
        lblText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblText.maxWidthProperty().bind(rootPane.widthProperty().multiply(0.7));
        lblText.styleProperty()
                .bind(javafx.beans.binding.Bindings.concat("-fx-font-family: 'Consolas'; -fx-font-size: ",
                        rootPane.widthProperty().divide(100).asString(), "px;"));

        Button btnBack = createMenuButton("Back");
        btnBack.setOnAction(e -> showInstructionsOverview());

        instructionsContentBox.getChildren().addAll(lblTitle, lblText, btnBack);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.156));
        btn.prefHeightProperty().bind(rootPane.heightProperty().multiply(0.046));

        javafx.beans.binding.StringExpression fontSizeBinding = javafx.beans.binding.Bindings.concat("-fx-font-size: ", rootPane.widthProperty().divide(96).asString(), "px;");

        String baseStyle = "-fx-background-color: #3a2d2d; -fx-text-fill: beige; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: #5a4d4d; -fx-border-radius: 5;";
        String hoverStyle = "-fx-background-color: #4b3e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: #6a5d5d; -fx-border-radius: 5;";

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
}
