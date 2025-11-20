package cards;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;
import java.util.UUID;

// StackPane belongs to javaFX class = a container that stacks child nodes (such as images, text, and shapes)
public class Card extends StackPane {
    // We can track the card by your id
    private final String idCard;
    private final String name;
    private final ImageView imageView;
    private final String imagePath;

    // Starts with -1 because card starts off board
    private int posLine = -1;
    private int posCol = -1;

    public static final double CARD_WIDTH = 100;
    public static final double CARD_HEIGHT = 150;

    public static final double HP_WIDTH_RATIO = 0.2634;
    public static final double HP_HEIGHT_RATIO = 0.30;

    // Construtor
    public Card(String idCard, String name, String imagePath) {
        this.idCard = idCard;
        this.name = name;
        this.imagePath = imagePath;

        Image img = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath)));
        imageView = new ImageView(img);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        getChildren().setAll(imageView);
    }

    // Construtor alternativo: cria uma carta apenas com o nome,
    // gerando automaticamente um ID único e o caminho da imagem baseado no nome.
    public Card(String name) {
        this(UUID.randomUUID().toString(), name, "/img/regular/" + name.toLowerCase() + ".png");
    }

    public void highlight(boolean on) {
        setStyle(on
                ? "-fx-effect: dropshadow(gaussian, #f0e6d2, 18, 0.3, 0, 0);"
                : "");
    }

    public void changeLifeIcon(int hp) {
        // Remove qualquer ícone anterior (se existir)
        getChildren().removeIf(node -> "lifeIcon".equals(node.getId()));

        // --- INÍCIO DA CORREÇÃO ---
        // Se a vida for 0 ou menos, não há ícone para mostrar.
        // A carta será removida pela 'cleanupDeadCreatures' em breve.
        if (hp <= 0) {
            return;
        }
        // --- FIM DA CORREÇÃO ---

        // Determina a imagem com base no HP
        String iconPath = "/img/hp/" + hp + ".png"; // exemplo: /img/hp/3.png

        // Verificação defensiva para evitar NPE se o ícone (1.png, 2.png, etc.)
        // também estiver faltando.
        java.io.InputStream iconStream = getClass().getResourceAsStream(iconPath);
        if (iconStream == null) {
            System.err.println("!! AVISO: Ícone de vida não encontrado em: " + iconPath);
            return; // Não quebra o jogo, apenas não mostra o ícone.
        }

        Image hpImg = new Image(iconStream);
        ImageView hpView = new ImageView(hpImg);
        hpView.setPreserveRatio(true);
        hpView.setSmooth(true);
        hpView.setCache(true);
        hpView.setId("lifeIcon");

        double hpWidth = CARD_WIDTH * HP_WIDTH_RATIO;
        double hpHeight = CARD_HEIGHT * HP_HEIGHT_RATIO;

        hpView.setFitWidth(hpWidth);
        hpView.setFitHeight(hpHeight);

        // Posiciona no canto inferior direito
        StackPane.setAlignment(hpView, javafx.geometry.Pos.BOTTOM_RIGHT);

        // Adiciona o ícone por cima
        getChildren().add(hpView);
    }

    // getters
    public String getImagePath() {
        return imagePath;
    }

    public String getIdCard() {
        return idCard;
    }

    public String getName() {
        return name;
    }

    public int getPosLine() {
        return posLine;
    }

    public int getPosCol() {
        return posCol;
    }

    public void setPos(int posLine, int posCol) {
        this.posLine = posLine;
        this.posCol = posCol;
    }

}