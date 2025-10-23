// Main.java
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // --- TESTE de caminhos de recursos (remova depois que tudo estiver OK) ---
        testRes("/img/regular/coyote.png");
        testRes("/img/regular/grizzly.png");
        testRes("/img/regular/opossum.png");   // certifique-se que existe esse nome
        testRes("/img/regular/rabbit.png");
        testRes("/img/regular/stoat.png");
        testRes("/img/regular/wolf.png");
        testRes("/img/regular/wolf_talking.png");
        testRes("/img/regular/raven.png");
        testRes("/img/regular/sparrow.png");
        testRes("/img/regular/squirrel.png");

        // --- 1) Criar e preparar os baralhos ---
        Deck deck = new Deck();
        deck.shuffle();           // embaralhar baralho principal
        deck.shuffleSquirrels();  // opcional: embaralhar esquilos

        System.out.println("Cartas no baralho principal: " + deck.getRemainingCards());
        // Removido o log que usava getDrawPile() para contar esquilos (era enganoso)

        // --- 2) Comprar cartas para "mão" do jogador ---
        List<CreatureCard> mao = deck.draw(5);        // compra 5 do baralho principal
        CreatureCard esquilo1 = deck.drawSquirrel();  // compra 1 esquilo
        CreatureCard esquilo2 = deck.drawSquirrel();  // compra outro esquilo

        // Construir um painel para exibir as cartas compradas
        FlowPane maoPane = new FlowPane();
        maoPane.setHgap(16);
        maoPane.setVgap(16);
        maoPane.setPadding(new Insets(12));

        // Adicionar cartas normais
        for (CreatureCard c : mao) {
            maoPane.getChildren().add(c); // CreatureCard já é um StackPane (Node)
            System.out.printf(
                    "Comprou: %s [ATK=%d, HP=%d, Blood=%d, Bones=%d, Free=%s]%n",
                    c.getName(), c.getAttack(), c.getHealth(), c.getBloodCost(), c.getBonesCost(), c.isFree()
            );
        }

        // Adicionar esquilos (se existirem)
        HBox squirrelsBox = new HBox(16);
        squirrelsBox.setAlignment(Pos.CENTER_LEFT);
        if (esquilo1 != null) {
            squirrelsBox.getChildren().add(esquilo1);
            System.out.println("Comprou: Squirrel");
        }
        if (esquilo2 != null) {
            squirrelsBox.getChildren().add(esquilo2);
            System.out.println("Comprou: Squirrel");
        }

        // --- 3) Testar dano em criatura (lógica básica) ---
        if (!mao.isEmpty()) {
            CreatureCard alvo = mao.get(0);
            int hpAntes = alvo.getHealth();
            int dano = 2;
            alvo.takeDamage(dano);
            int hpDepois = alvo.getHealth();
            System.out.printf(
                    "Dano aplicado em %s: -%d HP (antes=%d, depois=%d)%n",
                    alvo.getName(), dano, hpAntes, hpDepois
            );
        }

        // --- 4) Montar UI simples ---
        Label maoLabel = new Label("Mão (5 cartas do baralho principal):");
        Label squirrelLabel = new Label("Esquilos comprados:");

        VBox rootContent = new VBox(8,
                maoLabel,
                maoPane,
                squirrelLabel,
                squirrelsBox
        );
        rootContent.setPadding(new Insets(16));

        ScrollPane scroll = new ScrollPane(rootContent);
        scroll.setFitToWidth(true);

        Scene scene = new Scene(scroll, 900, 600);
        stage.setTitle("Teste do Baralho e Cartas");
        stage.setScene(scene);
        stage.show();
    }

    // --- auxiliar para testar se a imagem está acessível no classpath ---
    private static void testRes(String p) {
        System.out.println(p + " -> " + Card.class.getResource(p));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
