package events;

// Nas funções, é só chamar onEvent( Evento específico )
public interface EventListener {
    void onEvent(Event event);
}