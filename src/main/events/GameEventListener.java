package events;

@FunctionalInterface //java disse que é boa prática mas não faz muita coisa, serve para interfaces com 1 metodo abstrato
public interface GameEventListener {
    void onEvent(GameEvent event);
}
