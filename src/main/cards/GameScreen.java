package cards;

import events.EventBus;
import events.GameLogic;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.VPos;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class GameScreen {

    // Passando o game para ca, a gnt tem controle de todos as informações da partida
    private final GameLogic game;
    private final EventBus eventBus;

    // ==== CONSTRUTOR ====
    public GameScreen(GameLogic game, EventBus eventBus) {
        this.game  = game;
        this.eventBus = eventBus;
    }

    //====== medidas padrao
    private static final double CARD_WIDTH = 100;   // largura padrão da carta
    private static final double CARD_HEIGHT = 150;   // altura padrão da carta

    // ====== controle de seleção
    private Card selectedCardNode = null;

    // ======= configs da mao
    private static final double HAND_SPACING = 5;           // espaçamento “natural” entre cartas
    private HBox playerHandP1;

    // label de turno
    private Label turnLabel;

    // grids do topo e do fundo (antes eram locais)
    private GridPane topGrid;
    private GridPane bottomGrid;

    // orientação da câmera: false = normal (P1 embaixo), true = invertida (P2 embaixo)
    private boolean flippedView = false;

    // ===== TELA DE JOGO =====
    public void startGame(Stage stage) {
        // ---------------------------------------------------------------------
        // LAYOUT GERAL:
        // [ esquerda (20%) | TABULEIRO (80%) ]
        // - Esquerda: balança/placar + botão do sino
        // - Centro: tabuleiro dividido em cima (TOP) e baixo (BOTTOM)
        // ---------------------------------------------------------------------
        HBox root = new HBox();
        root.setStyle("-fx-background-color: #1f1b1b;");

        // ====== PAINEL ESQUERDO (20%) ======
        VBox leftPanel = new VBox();
        leftPanel.setPadding(new Insets(16));
        leftPanel.setSpacing(12);
        leftPanel.setStyle("-fx-background-color: #241d1d; -fx-border-color: #3a2d2d; -fx-border-width: 0 2 0 0;");

        // ---- BALANÇA/PLACAR (TOPO) ----
        Label scoreTitle = new Label("SCALE");
        scoreTitle.setTextFill(Color.BEIGE);
        scoreTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 20));

        // --- ESCALA RESPONSIVA ---
        GridPane scaleValues = new GridPane();
        scaleValues.setHgap(0);
        scaleValues.setVgap(0);
        scaleValues.setAlignment(Pos.CENTER);

        // 11 colunas com largura percentual igual (5 4 3 2 1 0 1 2 3 4 5)
        for (int c = 0; c < 11; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 11.0);
            scaleValues.getColumnConstraints().add(cc);
        }

        // cria labels e adiciona
        for (int i = -5; i <= 5; i++) {
            Label lbl = new Label(String.valueOf(Math.abs(i)));
            lbl.setTextFill(Color.LIGHTGRAY);
            lbl.setFont(Font.font("Consolas", 14));
            lbl.setAlignment(Pos.CENTER);
            lbl.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(lbl, Priority.ALWAYS);
            scaleValues.add(lbl, i + 5, 0); //O que sera colocado, linha, coluna
        }

        // --- LINHAS E MARCADOR ---
        StackPane scaleBar = new StackPane();
        scaleBar.setMinHeight(40);
        scaleBar.setPrefHeight(40);
        scaleBar.setStyle("-fx-background-color: transparent;");

        Line line = new Line();
        line.setStartX(0);
        line.setStartY(0);
        line.setEndX(300);
        line.setEndY(0);
        line.setStroke(Color.DARKGOLDENROD);
        line.setStrokeWidth(3);

        Line marker = new Line();
        marker.setStartX(0);
        marker.setStartY(-12);
        marker.setEndX(0);
        marker.setEndY(12);
        marker.setStroke(Color.BEIGE);
        marker.setStrokeWidth(3);

        scaleBar.widthProperty().addListener((obs, oldW, newW) -> {
            line.setEndX(newW.doubleValue() - 8); // margem direita
        });

        scaleBar.getChildren().addAll(line, marker);
        StackPane.setAlignment(line, Pos.CENTER);
        StackPane.setAlignment(marker, Pos.CENTER);

        // --- ESPAÇADOR + BOTÃO ---
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button bellButton = new Button("Pass turn");
        bellButton.setMaxWidth(Double.MAX_VALUE);
        bellButton.setStyle(
                "-fx-background-color: #4b2e2e; -fx-text-fill: #f0e6d2; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 20;"
        );
        // Hover
        bellButton.setOnMouseEntered(e -> bellButton.setStyle(
                "-fx-background-color: #4b2020; -fx-text-fill: #f0e6d2; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 20;"
        ));
        bellButton.setOnMouseExited(e -> bellButton.setStyle(
                "-fx-background-color: #4b2e2e; -fx-text-fill: #f0e6d2; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 20;"
        ));
        bellButton.setOnMouseClicked(e -> {
            System.out.println("Sino clicado.");
            passTurn();
        });

        // indicador de turno
        turnLabel = new Label();
        turnLabel.setTextFill(Color.BEIGE);
        turnLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));

        // --- MONTAGEM FINAL DO PAINEL ESQUERDO ---
        leftPanel.getChildren().addAll(
                scoreTitle,
                scaleValues,
                scaleBar,
                turnLabel,
                spacer,
                bellButton
        );

        // ====== TABULEIRO (80%)  ======
        VBox boardArea = new VBox();
        boardArea.setPadding(new Insets(10));
        boardArea.setSpacing(10);
        boardArea.setStyle(
                "-fx-background-color: #2b2222;" +
                        "-fx-border-color: #6a4c4c;" +
                        "-fx-border-style: dashed;" +
                        "-fx-border-width: 3;"
        );
        boardArea.setAlignment(Pos.TOP_CENTER);

        // Grids agora são "TOP" (cima) e "BOTTOM" (baixo)
        topGrid = createPlayerGrid("TOP");        // topo
        StackPane centerDivider = new StackPane();
        centerDivider.setMinHeight(4);
        centerDivider.setMaxHeight(4);
        centerDivider.setStyle("-fx-background-color: #3a2d2d; -fx-background-radius: 4;");
        bottomGrid = createPlayerGrid("BOTTOM");  // baixo

        playerHandP1 = createPlayerHand("HAND-P1");
        playerHandP1.setAlignment(Pos.CENTER);

        // ====== ÁREA DE DECKS (CANTO DIREITO INFERIOR) ======
        HBox deckArea = new HBox(30); // espaçamento entre os montes
        deckArea.setAlignment(Pos.BOTTOM_RIGHT);
        deckArea.setPadding(new Insets(0, 30, 15, 0)); // afastar da borda
        deckArea.setPickOnBounds(false);

        // Monte de Criaturas
        StackPane deckCreatures = createDeckPlaceholder(
                "DeckCriaturas",
                "/img/regular/backs/common.png",
                "Criaturas"
        );

        // Monte de Esquilos
        StackPane deckSquirrels = createDeckPlaceholder(
                "DeckEsquilos",
                "/img/regular/backs/squirrel.png",
                "Esquilos"
        );

        deckArea.getChildren().addAll(deckCreatures, deckSquirrels);

        // ====== JUNTA GRID + DECKS PARA DEIXAR NO CANTO INFERIOR DIREITO ======
        StackPane boardWithDecks = new StackPane();
        boardWithDecks.getChildren().addAll(bottomGrid, deckArea);
        StackPane.setAlignment(deckArea, Pos.BOTTOM_RIGHT);

        // Monta estrutura geral
        boardArea.getChildren().addAll(topGrid, centerDivider, boardWithDecks, playerHandP1);

        // ====== COMPOSIÇÃO E PROPORÇÕES ======
        root.getChildren().addAll(leftPanel, boardArea);

        // ~20% (esquerda - painel) / ~80% (tabuleiro)
        root.widthProperty().addListener((obs, oldW, newW) -> {
            double total = newW.doubleValue();
            leftPanel.setPrefWidth(total * 0.20);
            boardArea.setPrefWidth(total * 0.80);
        });

        // Altura acompanha o Stage
        root.prefWidthProperty().bind(stage.widthProperty());
        root.prefHeightProperty().bind(stage.heightProperty());

        // ===== TROCA DE CONTEÚDO DA CENA =====
        stage.getScene().setRoot(root);

        updateTurnLabelFromGame();

        // Adicionando cartas iniciais à mão do Player 1 ao começar o jogo:
        for (Card card : game.getPlayer1().getHand()) {
            addCardToHandBox(playerHandP1, card);
        }

        // desenha o board INTEIRO com base no Board (vazio no início)
        refreshBoardFromGame();
    }

    // ==========================================================
    // === MÉTODOS AUXILIARES    ===
    // ==========================================================

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

    /**
     * Cria um slot para carta (placeholder).
     */
    private StackPane createPlaceholder(String id, String imagePath) {
        StackPane slot = new StackPane();
        slot.setId(id);
        slot.setAlignment(Pos.CENTER);
        slot.setStyle(
                "-fx-background-color: rgba(80,60,60,0.35);" +
                        "-fx-border-color: #8a6a6a;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 2;"
        );

        // Define a imagem de placeholder
        setImagePlaceholder(slot, imagePath);

        slot.setMinWidth(CARD_WIDTH);
        slot.setMaxWidth(CARD_WIDTH);
        slot.setMinHeight(CARD_HEIGHT);
        slot.setMaxHeight(CARD_HEIGHT);

        // Hover
        slot.setOnMouseEntered(e -> slot.setStyle(
                "-fx-background-color: rgba(120,90,90,0.45);" +
                        "-fx-border-color: #b08a8a;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 2"
        ));
        slot.setOnMouseExited(e -> slot.setStyle(
                "-fx-background-color: rgba(80,60,60,0.35);" +
                        "-fx-border-color: #8a6a6a;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 2;"
        ));

        // clique no slot tenta posicionar a carta selecionada
        slot.setOnMouseClicked(e -> dropCard(slot));

        return slot;
    }

    // Define uma imagem em um StackPane (placeholder de seta/pata ou carta vazia)
    private void setImagePlaceholder(StackPane slot, String resourcePath) {
        Image img = new Image(
                getClass().getResource(resourcePath).toExternalForm(),
                false
        );

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
            if (isTopSlot(id)) { // topo fica "de cabeça para baixo"
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

    // === helpers para saber se é TOP ou BOTTOM ===
    private boolean isTopSlot(String slotId) {
        return slotId.startsWith("TOP");
    }

    private boolean isBottomSlot(String slotId) {
        return slotId.startsWith("BOTTOM");
    }

    private boolean isPositioningSlot(StackPane slot) {
        Integer row = (Integer) slot.getProperties().get("row");
        if (row == null) return false;

        String id = slot.getId();

        if (isBottomSlot(id)) {
            // BOTTOM: r=1 (seta) é posicionamento
            return row == 1;
        } else {
            // TOP: r=0 (seta) é posicionamento
            return row == 0;
        }
    }

    private boolean isAttackSlot(StackPane slot) {
        Integer row = (Integer) slot.getProperties().get("row");
        if (row == null) return false;

        String id = slot.getId();

        if (isBottomSlot(id)) {
            // BOTTOM: r=0 (pata) é ataque
            return row == 0;
        } else {
            // TOP: r=1 (pata) é ataque
            return row == 1;
        }
    }

    //============================================
    //Mao do jogador
    //============================================

    private void addCardToHandBox(HBox handBox, Card card) {
        if (handBox.getChildren().size() >= 7) {
            System.out.println("Limite máximo de 7 cartas atingido.");
            return;
        }
        handBox.getChildren().add(card);
    }

    private HBox createPlayerHand(String prefix) {
        HBox hand = new HBox();

        hand.setSpacing(HAND_SPACING);
        hand.setAlignment(Pos.CENTER_LEFT);
        hand.setId(prefix);

        double fixedWidth = (CARD_WIDTH * 7) + (HAND_SPACING * 6);
        hand.setMinWidth(fixedWidth);
        hand.setPrefWidth(fixedWidth);
        hand.setMaxWidth(fixedWidth);

        hand.setMinHeight(CARD_HEIGHT);
        hand.setPrefHeight(CARD_HEIGHT);
        hand.setMaxHeight(CARD_HEIGHT);

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

    //==========================HELPERS

    private void updateTurnLabelFromGame() {
        Player current = game.getCurrentPlayer();
        turnLabel.setText("Turn: " + current.getName());
    }

    // === helpers de seleção ===
    private void selectCard(Card card) {
        clearSelection();
        selectedCardNode = card;
        card.highlight(true);
        System.out.println("Carta selecionada");
    }

    private void clearSelection() {
        if (selectedCardNode != null) {
            selectedCardNode.highlight(false);
        }
        selectedCardNode = null;
    }

    // === posicionar carta no slot ===
    private void dropCard(StackPane slot) {
        // 1) Precisa ter carta selecionada
        if (selectedCardNode == null) {
            return;
        }

        // 2) Descobre posição lógica no Board
        int[] coords = getBoardPositionFromSlot(slot);
        if (coords == null) {
            System.out.println("Slot sem coordenadas lógicas.");
            return;
        }
        int line = coords[0];
        int col  = coords[1];

        Player current = game.getCurrentPlayer();

        // 3) Índice da carta na mão REAL
        int handIndex = current.getHand().indexOf(selectedCardNode);
        if (handIndex == -1) {
            System.out.println("Carta selecionada não está na mão do jogador atual.");
            return;
        }

        // 4) Delega 100% para a lógica
        GameLogic.PlaceCardResult result =
                game.tryPlaceCardFromCurrentPlayerHand(handIndex, line, col);

        if (result != GameLogic.PlaceCardResult.SUCCESS) {
            System.out.println("Não foi possível colocar a carta: " + result);
            // aqui depois você pode colocar animações/feedbacks diferentes pra cada motivo
            return;
        }

        // 5) UI só reflete o estado REAL
        clearSelection();
        refreshHandsFromGame();
        refreshBoardFromGame();
    }

    private Card pickCardFromEventTarget(Object target) {
        if (!(target instanceof Node n)) return null;

        while (n != null && !(n instanceof Card)) {
            n = n.getParent();
        }
        return (n instanceof Card c) ? c : null;
    }

    // Criação da Área de armazenamento de cartas (decks)
    private StackPane createDeckPlaceholder(String id, String imagePath, String deckType) {
        StackPane deck = new StackPane();
        deck.setId(id);
        deck.setMaxHeight(CARD_HEIGHT*1.2);
        deck.setMaxWidth(CARD_WIDTH);
        deck.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        deck.setAlignment(Pos.CENTER);

        // camadas visuais do deck
        for (int i = 0; i < 3; i++) {
            Image img = new Image(getClass().getResource(imagePath).toExternalForm(), false);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setFitWidth(CARD_WIDTH);
            iv.setFitHeight(CARD_HEIGHT);
            iv.setTranslateY(-i * 3);
            deck.getChildren().add(iv);
        }

        // hover
        deck.setOnMouseEntered(e -> {
            deck.setScaleX(1.08);
            deck.setScaleY(1.08);
            deck.setCursor(Cursor.HAND);
            deck.setEffect(new DropShadow(20, Color.rgb(220,180,180,0.5)));
        });

        deck.setOnMouseExited(e -> {
            deck.setScaleX(1);
            deck.setScaleY(1);
            deck.setEffect(null);
        });

        deck.setOnMouseClicked(e -> {
            boolean success;

            if (deckType.equals("Esquilos")) {
                success = game.drawFromSquirrelDeckCurrentPlayer();
            } else {
                success = game.drawFromMainDeckCurrentPlayer();
            }

            if (!success) {
                System.out.println("Deck " + deckType + " está vazio!");
                return;
            }

            System.out.println(deckType + " clicado! "
                    + game.getCurrentPlayer().getName() + " comprou uma carta.");

            // Redesenha mão com base no estado REAL
            refreshHandsFromGame();
        });

        return deck;
    }

    //=============================================================================



    private void passTurn() {
        System.out.println("Pass turn.");

        // 1) LÓGICA DO JOGO
        game.switchTurn();

        // 2) Alterna orientação visual
        flippedView = !flippedView;

        // 3) Atualiza UI com base no estado REAL
        updateTurnLabelFromGame();
        refreshHandsFromGame();
        refreshBoardFromGame();
        clearSelection();
    }

    // ===========================
    // === REFRESH DO TABULEIRO ==
    // ===========================

    // Encontra slot por (prefix, row, col)
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
        if (row == null) return;

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


    /**
     * Converte uma posição lógica do Board (line, col) para o slot VISUAL correto.
     *
     * O Board lógico é sempre fixo:
     * 0 = posicionamento do oponente
     * 1 = ataque do oponente
     * 2 = ataque do jogador da vez
     * 3 = posicionamento do jogador da vez
     *
     * Mas a UI pode estar em modo normal ou invertido (flippedView),
     * então este metodo traduz a posição REAL do Board para o local onde
     * a carta deve ser desenhada visualmente (TOP/BOTTOM e row 0/1).
     */

    private StackPane getVisualSlotForBoardPosition(int line, int col) {
        boolean isTop;
        int visualRow;

        if (!flippedView) {
            // visão normal: P2 em cima, P1 embaixo
            if (line == 0) { isTop = true;  visualRow = 0; } // TOP posic
            else if (line == 1) { isTop = true;  visualRow = 1; } // TOP atk
            else if (line == 2) { isTop = false; visualRow = 0; } // BOTTOM atk
            else { isTop = false; visualRow = 1; }               // BOTTOM posic
        } else {
            // visão invertida: jogador da vez embaixo
            // linhas do Board trocam de faixa visual
            if (line == 0) { isTop = false; visualRow = 1; } // vai para BOTTOM posic
            else if (line == 1) { isTop = false; visualRow = 0; } // BOTTOM atk
            else if (line == 2) { isTop = true;  visualRow = 1; } // TOP atk
            else { isTop = true;  visualRow = 0; }                // TOP posic
        }

        return findSlot(isTop ? "TOP" : "BOTTOM", visualRow, col);
    }

    /**
     * Converte um slot VISUAL clicado (TOP/BOTTOM, row 0/1) para a posição REAL do Board (line, col).
     *
     * O tabuleiro lógico é sempre fixo (linhas 0–3), mas a interface pode estar em modo normal
     * ou invertido (flippedView). Esse metodo desfaz essa projeção visual e retorna a posição
     * verdadeira do Board onde a ação deve ocorrer.
     *
     * Essencial para que a UI não contenha regras de jogo: ela apenas traduz o clique visual
     * para coordenadas lógicas e delega toda a validação/ação para a GameLogic.
     */


    private int[] getBoardPositionFromSlot(StackPane slot) {
        Integer visualRow = (Integer) slot.getProperties().get("row");
        Integer col       = (Integer) slot.getProperties().get("col");
        if (visualRow == null || col == null) {
            return null;
        }

        boolean top = isTopSlot(slot.getId());
        int line;

        if (!flippedView) {
            // visão normal: (mesma lógica invertida de getVisualSlotForBoardPosition)
            if (top) {
                line = (visualRow == 0) ? 0 : 1;   // TOP row 0 -> line 0, TOP row 1 -> line 1
            } else {
                line = (visualRow == 0) ? 2 : 3;   // BOTTOM row 0 -> line 2, BOTTOM row 1 -> line 3
            }
        } else {
            // visão invertida
            if (!top) {
                // BOTTOM
                line = (visualRow == 0) ? 1 : 0;   // row 0 -> line 1, row 1 -> line 0
            } else {
                // TOP
                line = (visualRow == 0) ? 3 : 2;   // row 0 -> line 3, row 1 -> line 2
            }
        }

        return new int[]{ line, col };
    }

    //===============REFRESHS

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
                if (card == null) continue;

                StackPane slot = getVisualSlotForBoardPosition(line, col);
                if (slot == null) continue;

                slot.getChildren().setAll(card);
                slot.getProperties().put("occupied", Boolean.TRUE);
            }
        }
    }

    //Fim
}
