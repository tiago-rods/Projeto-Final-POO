package cards;

/**
 * Representa a balança de Inscryption:
 *   valor  0  = neutro
 *   valor +5 = lado do Player 1 (vantagem/vitória)
 *   valor -5 = lado do Player 2 (vantagem/vitória)
 *
 * Não conhece UI nem JavaFX, é só lógica.
 */
public class Scale {

    // limite inferior (lado do Player 2) e superior (lado do Player 1)
    private final int minValue;
    private final int maxValue;

    // valor atual da balança (0 no início)
    private int value;

    public Scale() {
        this(-5, 5); // padrão
    }

    public Scale(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = 0;
    }

    /** Reseta a balança para o centro (0). */
    public void reset() {
        this.value = 0;
    }

    /**
     * Aplica dano direto à balança, no mesmo estilo do Inscryption.
     *
     * @param attacker Quem causou o dano.
     * @param damage   Quanto de dano direto foi causado ao oponente.
     *
     * A classe não precisa conhecer GameLogic nem Board,
     * apenas assume que attacker.getOrder() é 1 ou 2.
     */
    public void applyDirectDamage(Player attacker, int damage) {
        if (attacker == null || damage <= 0) {
            return;
        }

        if (attacker.getOrder() == 1) {
            // Player 1 empurra a balança para o lado positivo
            value += damage;
        } else if (attacker.getOrder() == 2) {
            // Player 2 empurra a balança para o lado negativo
            value -= damage;
        }

        clamp();
        System.out.println("[GameScale] Nova balança: " + value);
    }

    /** Garante que o valor fique entre minValue e maxValue. */
    private void clamp() {
        if (value > maxValue) value = maxValue;
        if (value < minValue) value = minValue;
    }

    // ===== Getters úteis para lógica e UI =====

    public int getValue() {
        return value;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    /** true se a balança está pendendo para o lado do Player 1. */
    public boolean isLeaningToPlayer1() {
        return value > 0;
    }

    /** true se a balança está pendendo para o lado do Player 2. */
    public boolean isLeaningToPlayer2() {
        return value < 0;
    }

    /** true se atingiu o limite de vitória do Player 1. */
    public boolean reachedPlayer1Win() {
        return value >= maxValue;
    }

    /** true se atingiu o limite de vitória do Player 2. */
    public boolean reachedPlayer2Win() {
        return value <= minValue;
    }

    /**
     * Útil para UI: transforma o valor em índice de 0..(max-min).
     * Ex: min=-5, max=5 -> intervalo 0..10.
     */
    public int asIndex() {
        return value - minValue;
    }

    @Override
    public String toString() {
        return "GameScale{value=" + value + "}";
    }
}
