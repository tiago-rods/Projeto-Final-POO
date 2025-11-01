package cards;

import items.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Player {
    private final String name;
    private final int order; // define se é jogador 1 ou 2
    private int lives = 2; // cada jogador começa com 2 vidas
    private int bones = 0; // quantidade de ossos que o jogador possui

    private final Deque<Card> deck = new ArrayDeque<>(); // baralho do jogador
    private final List<Card> hand = new ArrayList<>(); // cartas na mão do jogador
    private final List<Card> graveyard = new ArrayList<>(); // cartas no cemitério do jogador
    private final List<Items> items = new ArrayList<>(); // itens do jogador

    public Player(String name, int order) { // construtor
        this.name = name;
        this.order = order;
    }

    public String getName() { return name; } // retorna o nome do jogador
    public int getOrder() { return order; } // retorna o lado do jogador

    public int getLives() { return lives; } // retorna o número de vidas do jogador
    public void loseLife() { if (lives > 0) lives--; } // jogador perde uma vida

    public int getBones() { return bones; } // retorna a quantidade de ossos do jogador
    public void addBones(int amount) { bones += amount; } // jogador ganha ossos
    public void spendBones(int amount) { bones -= amount; } // jogador gasta ossos

    public Deque<Card> getDeck() { return deck; } // retorna o baralho do jogador
    public List<Card> getHand() { return hand; } // retorna as cartas na mão do jogador
    public List<Card> getGraveyard() { return graveyard; } // retorna as cartas no cemitério do jogador
    public List<Items> getItems() { return items; } // retorna os itens do jogador

    public Card removeCardFromHand(int position) {
        if (position < 0 || position >= hand.size()) {
            return null;
        }
        return hand.remove(position); // retorna a carta já excluída da mão do jogador
    }

    public boolean isAlive() { return lives > 0; } // verifica se o jogador ainda está vivo

}