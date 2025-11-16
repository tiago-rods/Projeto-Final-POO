package events;

// Qualquer classe que implemente essa interface deve implementar onEvent, que é chamado quando o evento acontece
// Nas funções, é só chamar onEvent( Evento específico )
public interface EventListener {
    void onEvent(Event event);
}