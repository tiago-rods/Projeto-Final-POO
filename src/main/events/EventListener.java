package events;

// Generics <T>: permite que um EventListener declare que lida com um tipo específico de evento
// Qualquer classe que implemente essa interface deve implementar onEvent, que é chamado quando o evento acontece
// Nas funções, é só chamar onEvent( Evento específico )
public interface EventListener {
    void onEvent(Event event);
}