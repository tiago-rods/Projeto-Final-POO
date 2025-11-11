package cards;

import events.Event;
import events.EventBus;
import events.EventType;
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

    // ====== controle de turno e seleção
    private boolean isPlayer1Turn = true;            // true = vez do P1; false = vez do P2
    private Card selectedCardNode = null;


    // ======= configs da mao
    private static final double HAND_SPACING = 5;           // espaçamento “natural” entre cartas
    private HBox playerHandP1;

    // label de turno
    private Label turnLabel;


    // ===== TELA DE JOGO =====
    public void startGame(Stage stage) {
        // ---------------------------------------------------------------------
        // LAYOUT GERAL:
        // [ esquerda (20%) | TABULEIRO (80%) ]
        // - Esquerda: balança/placar + botão do sino
        // - Centro: tabuleiro dividido em cima (P2) e baixo (P1)
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

            Card newCard = new Card("P1-CARD-0", "Grizzly", "/img/regular/grizzly.png");
            newCard.changeLifeIcon(1);
            addCardToHandBox(playerHandP1, newCard);



        });

        // indicador de turno
        turnLabel = new Label("Turn: P1");
        turnLabel.setTextFill(Color.BEIGE);
        turnLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));


// --- MONTAGEM FINAL ---
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

// Grids agora são por jogador:
// P2 (topo) e P1 (baixo)
        GridPane topGrid = createPlayerGrid("P2");    // P2-0..P2-7 (topo)
        StackPane centerDivider = new StackPane();    // divisor horizontal
        centerDivider.setMinHeight(4);
        centerDivider.setMaxHeight(4);
        centerDivider.setStyle("-fx-background-color: #3a2d2d; -fx-background-radius: 4;");
        GridPane bottomGrid = createPlayerGrid("P1"); // P1-0..P1-7 (baixo)

        playerHandP1 = createPlayerHand("HAND-P1");
        playerHandP1.setAlignment(Pos.CENTER);



        // ====== ÁREA DE DECKS (CANTO DIREITO INFERIOR) ======
        HBox deckArea = new HBox(30); // espaçamento entre os montes
        deckArea.setAlignment(Pos.BOTTOM_RIGHT);
        deckArea.setPadding(new Insets(0, 30, 15, 0)); // afastar da borda

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

        // ====== JUNTA GRID + DECKS ======
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


        // Adicionando cartas iniciais ao começar o jogo:
        for (Card card : game.getPlayer1().getHand()) {
            addCardToHandBox(playerHandP1, card);
        }

    }
// ==========================================================
// === MÉTODOS AUXILIARES    ===
// ==========================================================


    /**
     * Cria um grid 2 linhas x 4 colunas (8 slots) para um jogador.
     * prefix: "P1" (baixo) ou "P2" (cima) — usado na ID dos slots.
     */

    private GridPane createPlayerGrid(String prefix) {
        GridPane grid = new GridPane();
        grid.setHgap(10);     // espaçamento horizontal (simétrico)
        grid.setVgap(10);     // espaçamento vertical (simétrico)
        grid.setAlignment(Pos.CENTER);



        int id = 0;
        for (int r = 0; r < 2; r++) {

            for (int c = 0; c < 4; c++) {
                String imagePath;
                //seta e pata nos devidos lugares
                if (prefix.equals("P2")) {
                    imagePath = (r == 0) ? "/img/arrow.png" : "/img/paw.png";
                } else {
                    imagePath = (r == 0) ? "/img/paw.png" : "/img/arrow.png";
                }

                StackPane slot = createPlaceholder(prefix + "-" + id, imagePath);


                slot.getProperties().put("row", r);
                slot.getProperties().put("col", c);

                // Centraliza verticalmente caso a célula fique mais alta que o slot
                GridPane.setValignment(slot, VPos.CENTER);

                grid.add(slot, c, r);
                id++;
            }
        }
        return grid;
    }


    /**
     * Cria um slot para carta (placeholder).
     * Você poderá substituir o conteúdo por uma ImageView no futuro.
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

        // Define as imagens de placeholder
        setImagePlaceholder(slot, imagePath);



        // Preferências iniciais menores (mantém 16:9-ish)
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

        //todos começam vazios
        // define o slot como vazio, ou estar ocupado começa false
        slot.getProperties().put("occupied", Boolean.FALSE);


        //boolean ocupado = Boolean.TRUE.equals(slot.getProperties().get("occupied"));
        //Se for true, o slot já tem uma carta; se for false, está livre
       // clique no slot tenta posicionar a carta selecionada
        slot.setOnMouseClicked(e -> dropCard(slot));



        return slot;
    }

// Define uma imagem em um StackPane, ajustando tamanho, opacidade e rotação conforme o tipo.
// Use para atualizar visualmente um slot da interface (ex: carta, seta, ícone).
// Exemplo de uso:
// setSlotImage(slotCarta, "/images/card_back.png");
// setSlotImage(slotSeta, "/images/arrow.png");
// setSlotImage(slotPata, "/images/paw.png");

    private void setImagePlaceholder(StackPane slot, String resourcePath) {
        // Carrega a imagem do classpath (src/main/resources)
        Image img = new Image(
                getClass().getResource(resourcePath).toExternalForm(),
                false
        );

        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);   // mantém proporção (pode sobrar borda)
        iv.setSmooth(true);
        iv.setCache(true);

        // Remove possíveis bindings anteriores
        iv.fitWidthProperty().unbind();
        iv.fitHeightProperty().unbind();

     // caso especial de arrow e paw
            if (resourcePath.contains("arrow")) {
                // seta reduz tamanho + opacidade
                int inset = 25;
                StackPane.setMargin(iv, new Insets(inset));
                iv.fitWidthProperty().bind(slot.widthProperty().subtract(inset * 2));
                iv.fitHeightProperty().bind(slot.heightProperty().subtract(inset * 2));
                iv.setOpacity(0.4); //Controla opacidade
                if (isP2(slot.getId())) { //Caso seja P2
                    iv.setRotate(180);   //Deixar de ponta cabeca
                }


            } else if (resourcePath.contains("paw")) {
                // pata apenas reduz opacidade
                iv.fitWidthProperty().bind(slot.widthProperty());
                iv.fitHeightProperty().bind(slot.heightProperty());
                iv.setOpacity(0.4); //Controla opacidade
                if (isP2(slot.getId())) { //Caso seja P2
                    iv.setRotate(180);   //Deixar de ponta cabeca
                }


            } else {
                // carta ocupa tudo
                iv.fitWidthProperty().bind(slot.widthProperty());
                iv.fitHeightProperty().bind(slot.heightProperty());

            }



        // Troca o conteúdo do slot pela imagem
        slot.getChildren().setAll(iv);


    }




    /**
     * Verifica a qual jogador o identificador de slot pertence.
     *
     * @param slotId Identificador do slot (por exemplo, "P1-3" ou "P2-7")
     * @return true se o slot pertencer ao jogador correspondente (P1 ou P2)
     */
    private boolean isP2(String slotId) {
        return slotId.startsWith("P2");
    }

    private boolean isP1(String slotId) {
        return slotId.startsWith("P1");
    }



    //============================================
    //Mao do jogador
    //============================================



    // --------------------------------------------------------------
// Adiciona uma carta existente à mão do jogador (HBox) com limite de 7 cartas.
// Esta função APENAS insere a carta; não cria nem adiciona handlers de UI.
// Os hovers e seleções são tratados pelo próprio HBox (delegação de eventos).
// Exemplo de uso:
// Card newCard = new Card("P1-CARD-0", "Grizzly", "/images/card_back.png");
// addCardToHandBox(handBoxP1, newCard);
//
// @param handBox o HBox que representa a mão do jogador onde a carta será adicionada
// @param card    o objeto Card que será adicionado à mão
// --------------------------------------------------------------
    private void addCardToHandBox(HBox handBox, Card card) {
        // Limite máximo de 7 cartas
        if (handBox.getChildren().size() >= 7) {
            System.out.println("Limite máximo de 7 cartas atingido.");
            return;
        }

        // --- INSERE A CARTA ---
        handBox.getChildren().add(card);
    }

    // Suponho(rafa) que de para usar handBox.getChildren() como índices das cartas


    // Cria o container da mão do jogador (HBox) com delegação de eventos.
// - Suporta até 7 cartas lado a lado.
// - Os efeitos de hover e seleção são tratados diretamente pelo HBox.
// Exemplo de uso:
// HBox handBoxP1 = createPlayerHand("P1");
// Card card = new Card("P1-CARD-0", "Grizzly", "/images/card_back.png");
// addCardToHandBox(handBoxP1, card);
//
// @param prefix prefixo/ID para identificar a mão (ex.: "P1", "P2")
// @return HBox configurado para receber cartas com efeitos visuais
    private HBox createPlayerHand(String prefix) {
        HBox hand = new HBox();

        // --- LAYOUT ---
        hand.setSpacing(HAND_SPACING); // espaço entre as cartas
        hand.setAlignment(Pos.CENTER_LEFT); // mantém as cartas alinhadas à esquerda
        hand.setId(prefix); // define o ID (ex.: “P1”)

        // --- TAMANHO FIXO ---
        // Calcula a largura total com base em 7 cartas e espaçamento entre elas
        double fixedWidth = (CARD_WIDTH * 7) + (HAND_SPACING * 6);
        hand.setMinWidth(fixedWidth);
        hand.setPrefWidth(fixedWidth);
        hand.setMaxWidth(fixedWidth);

        // Define a altura fixa igual à altura da carta
        hand.setMinHeight(CARD_HEIGHT);
        hand.setPrefHeight(CARD_HEIGHT);
        hand.setMaxHeight(CARD_HEIGHT);

        // --------------------------------------------------------------
        // DELEGAÇÃO DE EVENTOS (apenas no container)
        // --------------------------------------------------------------
        // A ideia é aplicar o comportamento de hover e seleção
        // para qualquer carta dentro deste HBox, sem precisar
        // adicionar listeners individualmente em cada Card.
        // --------------------------------------------------------------

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
                    selectCard(card); // aqui o highlight(true) será chamado
                }
                e.consume();
            }
        });


        return hand;
    }



    private void passTurn() {
        System.out.println("Pass turn.");

        // alterna a vez
        isPlayer1Turn = !isPlayer1Turn;
        turnLabel.setText(isPlayer1Turn ? "Turn: P1" : "Turn: P2");

        //limpar qualquer seleção pendente
        //clearSelection();

    }


    // === helpers de seleção ===
    private void selectCard(Card card) {
        clearSelection();              // garante seleção única
        selectedCardNode = card;       // já é Card

        card.highlight(true); //faz brilhar
        System.out.println("Carta selecionada");
    }

    private void clearSelection() {
        if (selectedCardNode != null) {
            selectedCardNode.highlight(false);
        }
        selectedCardNode = null;

    }






    // === posicionar carta no slot, validando turno e ocupação ===
    private void dropCard(StackPane slot) {
        // precisa ter carta selecionada se nao tiver sai da fnção
        if (selectedCardNode == null) {
            return;
        }

        // o slot precisa pertencer ao jogador da vez
        boolean slotDoP1 = isP1(slot.getId());

        //caso nao seja nem a vez nem o slot do p1
        if ((isPlayer1Turn && !slotDoP1) || (!isPlayer1Turn && slotDoP1)) {
            // feedback
            System.out.println("Não é o lado do jogador da vez.");
            return;
        }

        // slot precisa estar livre
        boolean ocupado = Boolean.TRUE.equals(slot.getProperties().get("occupied"));
        if (ocupado) {
            System.out.println("Slot já ocupado.");
            return;
        }

        // substitui o placeholder pela própria Card e marca como ocupado
        slot.getChildren().setAll(selectedCardNode);
        slot.getProperties().put("occupied", Boolean.TRUE);

        // usa os metadados para atualizar a Card
        Integer row = (Integer) slot.getProperties().get("row");
        Integer col = (Integer) slot.getProperties().get("col");
        if (row != null && col != null) {
            selectedCardNode.setPos(row, col);
            System.out.println("Card foi para (" + row + "," + col + ")");
        }

        // remove da mão do jogador
        if (selectedCardNode.getParent() instanceof HBox hand) {
            hand.getChildren().remove(selectedCardNode);
        }

        // limpa seleção
        clearSelection();
    }

    private Card pickCardFromEventTarget(Object target) {
        // Se não é Node, não temos como subir a árvore
        if (!(target instanceof Node n)) return null;

        // Sobe pelos pais até achar uma Card
        while (n != null && !(n instanceof Card)) {
            n = n.getParent();
        }
        return (n instanceof Card c) ? c : null;
    }


    // Criação da Área de armazenamento de cartas
    private StackPane createDeckPlaceholder(String id, String imagePath, String deckType) {
        StackPane deck = new StackPane();
        deck.setId(id);
        deck.setMaxHeight(CARD_HEIGHT*1.2);
        deck.setMaxWidth(CARD_WIDTH);
        deck.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        deck.setAlignment(Pos.CENTER);

        // ----- Cria as "camadas" do deck -----
        for (int i = 0; i < 3; i++) {
            Image img = new Image(getClass().getResource(imagePath).toExternalForm(), false);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setFitWidth(CARD_WIDTH);
            iv.setFitHeight(CARD_HEIGHT);
            iv.setTranslateY(-i * 3); // desloca levemente para criar efeito de pilha
            deck.getChildren().add(iv);
        }


        // ----- Efeito de hover -----
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

        // ----- Clique: comprar carta -----
        deck.setOnMouseClicked(e -> {
            // Tipo, nome e caminho da nova carta a ser adicionada ao clicar no deck
            // Vai sempre pegando as info do game, que é onde fica toda a lógica do jogo
            String type;
            String name;
            String imgPath;
            // Aqui cria o evento específico que será chamado ao dar publish, contendo as info necessárias
            Event cardDrawn;
            // se for do tipo esquilo
            if(deckType.equals("Esquilos")) {
                CreatureCard cardSquirrelDeck = game.getDeckP1().getSquirrelCardDeck().getFirst();
                type = "Creature";
                name = cardSquirrelDeck.getName();
                imgPath = cardSquirrelDeck.getImagePath();
                cardDrawn = new Event(EventType.CARD_DRAWN, game.getCurrentPlayer(), cardSquirrelDeck);
            } else { // se for do tipo criatura
                CreatureCard cardDeck = game.getDeckP1().getCardDeck().getFirst(); // pega a primeira carta do deck
                type = "Squirrel";
                name = cardDeck.getName();
                imgPath = cardDeck.getImagePath();
                cardDrawn = new Event(EventType.CARD_DRAWN, game.getCurrentPlayer(), cardDeck);
            }

            System.out.println(deckType + " clicado! Comprando " + name + "...");

            Card newCard = new Card(type + "-" + System.currentTimeMillis(), name, imgPath);
            addCardToHandBox(playerHandP1, newCard);

            eventBus.publish(cardDrawn);
        });

        return deck;
    }



    //Fim
}


