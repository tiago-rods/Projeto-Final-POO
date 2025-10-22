import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Objects;

// StackPane belongs to javaFX class = a container that stacks child nodes (such as images, text, and shapes)
public class Card extends StackPane {
    // We can track the card by your id
    private final String idCard;
    private final String name;
    protected Image image;

    // protected --> constructor can be called by this class or a son
    protected Card(String idCard, String name, String imagePath) {
        this.idCard = idCard;
        this.name = name;
        // gets current class, than find the file within the project and open it as a stream
        this.image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));

        buildVisual();
    }

    public String getIdCard() { return idCard; }
    public String getName() { return name; }



    private void buildVisual() {
        // Card's bg
        Rectangle background = new Rectangle(150, 200);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.setFill(Color.BEIGE);
        background.setStroke(Color.BLACK);

        // Card's image
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(130);
        imageView.setFitHeight(100);
        imageView.setTranslateY(-20);

        // Card's name
        Text nameText = new Text(name);
        nameText.setFont(Font.font("Arial", 16));
        nameText.setFill(Color.BLACK);
        nameText.setTranslateY(-85);

        this.getChildren().addAll(background, imageView, nameText);
    }

}