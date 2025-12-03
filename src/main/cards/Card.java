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

    // Tamanho base "lógico" (se quiser usar em cálculos externos)
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

        // Imagem sempre ocupa o espaço atual da carta
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        // ❌ não travamos min/pref/max aqui.
        // Quem controla o tamanho é o layout (GameScreen/slots).
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

        // Se a vida for 0 ou menos, não há ícone para mostrar.
        if (hp <= 0) {
            return;
        }

        // Determina a imagem com base no HP
        String iconPath = "/img/hp/" + hp + ".png"; // exemplo: /img/hp/3.png

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

        // ✅ Ícone proporcional ao tamanho ATUAL da carta
        hpView.fitWidthProperty().bind(widthProperty().multiply(HP_WIDTH_RATIO));
        hpView.fitHeightProperty().bind(heightProperty().multiply(HP_HEIGHT_RATIO));

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
