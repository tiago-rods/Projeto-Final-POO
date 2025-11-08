package events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    // Mapeia: tipo de evento → lista de listeners.
    private final Map<EventType, List<EventListener>> listeners;

    // construtor do EventBus para que ele possa ser chamado nas classes de controle q vao usar ele
    public EventBus() {
        this.listeners = new HashMap<>();
    }

    // Adiciona um listener para um tipo específico de event.
    // OBS: na main teria: EventBus.subscribe(nome do Evento, função chamada por esse onEvent);
    public void subscribe(EventType type, EventListener listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
        // computeIfAbsent -> cria a lista quando necessário.
    }

    public void unsubscribe(EventType type, EventListener listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).remove(listener);
    }

    // Ao apertar um botão, por exemplo, publica um evento. Ao publicar esse evento,
    // recupera a lista de listeners para o event.getClass() (o evento acontecido) e itera chamando onEvent para cada função que contenha esse evento
    public void publish(Event event) {
        List<EventListener> eventListeners = listeners.get(event.getType());
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Error processing the event: " + e.getMessage());
                }
            }
        }
        // Log para debug
        System.out.println("📢 EVENT: " + event);
    }
}

