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
import javafx.scene.Parent;
import javafx.geometry.VPos;



public class GameScreen {

    private static final double CARD_WIDTH = 100;   // largura padrão da carta
    private static final double CARD_HEIGHT = 150;   // altura padrão da carta


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



        });


// --- MONTAGEM FINAL ---
        leftPanel.getChildren().addAll(
                scoreTitle,
                scaleValues,
                scaleBar,
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
                        "-fx-border-style: segments(10, 10) line-cap round;" +
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

// Empilha: topo (P2) / divisor / baixo (P1)
        boardArea.getChildren().addAll(topGrid, centerDivider, bottomGrid);

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

                StackPane slot = createCardSlot(prefix + "-" + id, imagePath);

                // Centraliza verticalmente caso a célula fique mais alta que o slot
                GridPane.setValignment(slot, VPos.CENTER);

                grid.add(slot, c, r);
                id++;
            }
        }
        return grid;
    }


    /**
     * Cria um slot "deitado" para carta (placeholder).
     * Você poderá substituir o conteúdo por uma ImageView no futuro.
     */
    private StackPane createCardSlot(String id, String imagePath) {
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

        // Define imagem diretamente neste slot
        setSlotImage(slot, imagePath);


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

        return slot;
    }



    private void setSlotImage(StackPane slot, String resourcePath) {
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

            } else if (resourcePath.contains("paw")) {
                // pata apenas reduz opacidade
                iv.fitWidthProperty().bind(slot.widthProperty());
                iv.fitHeightProperty().bind(slot.heightProperty());
                iv.setOpacity(0.4); //Controla opacidade

            } else {
                // carta ocupa tudo
                iv.fitWidthProperty().bind(slot.widthProperty());
                iv.fitHeightProperty().bind(slot.heightProperty());

            }

        if (slot.getId().startsWith("P2")) {
            iv.setRotate(180); // vira de ponta cabeça
        }


        // Troca o conteúdo do slot pela imagem
        slot.getChildren().setAll(iv);


    }


    /**
     * Atualiza a imagem de um slot do tabuleiro.
     *
     * @param root      layout principal da tela (VBox)
     * @param slotId    ID do slot (ex: "P1-0", "P2-3")
     * @param imagePath caminho da imagem (ex: "/img/regular/grizzly.png")
     *
     * Este metodo detecta automaticamente se o slot pertence a P2 (topo)
     * e aplica rotação de 180° nesse caso.
     *
     * Exemplo:
     *   addCardSlot(root, "P2-2", "/img/regular/grizzly.png");
     */

    private void addCardSlot(Parent root, String slotId, String imagePath){
        StackPane slot = (StackPane) root.lookup("#" + slotId);

        if (slot != null) {
            setSlotImage(slot, imagePath);
        } else {
            System.err.println("Slot não encontrado (verifique o ID: use P1-0..P1-7 ou P2-0..P2-7).");
        }
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


    /**
     * Remove a imagem de um slot do tabuleiro e restaura o estado vazio.
     *
     * @param root   layout principal da tela (VBox)
     * @param slotId ID do slot (ex: "P1-0", "P2-3")
     *
     * Exemplo:
     *   removeCardSlot(root, "P1-2");
     */
    private void removeCardSlot(Parent root, String slotId) {
        // Busca o StackPane com o ID especificado
        StackPane slot = (StackPane) root.lookup("#" + slotId);

        if (slot != null) {
            // Limpa o conteúdo do slot
            slot.getChildren().clear();

            // Adiciona novamente o rótulo (ID) para identificação visual
            Label tag = new Label(slotId);
            tag.setTextFill(Color.web("#f0e6d2", 0.65));
            tag.setFont(Font.font("Consolas", 12));
            slot.getChildren().add(tag);

            // Restaura o estilo original do slot vazio
            slot.setStyle(
                    "-fx-background-color: rgba(80,60,60,0.35);" +
                            "-fx-border-color: #8a6a6a;" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-width: 2;"
            );
        }
    }



 //Fim
}


