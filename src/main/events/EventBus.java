package events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static EventBus instance; // Armazena uma única instância de classe
    private final Map<EventType, List<GameEventListener>> listeners; // Map é um HashMap que armazena valores para diferentes tipos de eventos em uma lista de GameEventListenerc

    private EventBus() {
        this.listeners = new EnumMap<>(EventType.class); // HashMap específico para Enums, c
    }

    public static EventBus getInstance() { //Garante que só tem uma instância no EventBus
        if (instance == null) { // Se ainda não existe, cria uma nova
            instance = new EventBus(); //
        }
        return instance;
    }

    public void subscribe(EventType type, GameEventListener listener) {
        listeners.computeIfAbsent(type, key -> new ArrayList<>()).add(listener);


        // MODO MAIS FACIL POREM MAIS DIFICIL DE ENTENDER:

        //  List<GameEventListener> listenerList = listeners.get(type);
        //        if (listenerList == null) {
        //            listenerList = new ArrayList<>();
        //            listeners.put(type, listenerList);
        //        }
        //        listenerList.add(listener);

        // Este metodo verifica se a chave (type) já existe no Map
        // - Se a chave NÃO EXISTE: executa a função lambda e coloca o resultado no Map
        // - funçao lamba possui o formato (chave -> corpo da função)
        // - Logo se a chave não existe ainda, armazena no Map
        // - Se a chave JÁ EXISTE: retorna o valor existente (não executa a lambda)
        // - Sempre retorna o valor associado à chave (novo ou existente)

    }

    public void publish(GameEvent event) {
        List<GameEventListener> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            List<GameEventListener> copy = new ArrayList<>(typeListeners);
            for (GameEventListener listener : copy) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Error processing the event: " + e.getMessage());
                }
            }
        }
    }
}

/*      MODO MAIS EFICIENTE PORÉM MAIS DIFICIL DE ENTENDER.
        // Busca os listeners interessados neste tipo de evento
        if (typeListeners != null) { //verifica se há alguem inscrito neste evento, caso seja null, não faz nada
            // Cria uma cópia para evitar ConcurrentModificationException
            new ArrayList<>(typeListeners).forEach(listener -> { //cria uma cópia da lista original,
                // assim garantido que iterar-se-á sobre uma lista congelada
                // para cada listener na cópia, executa o código abaixo, (forEach é um tipo de loop)
                try {
                    listener.onEvent(event); //chama o metodo onEvent do listener e passa o evento como parametro
                } catch (Exception e) {
                    System.err.println("Error processing the event: " + e.getMessage());
                }
            });
        }
    }
}
*/
