package org.readutf.engine.event;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.adapter.EventGameAdapter;
import org.readutf.engine.event.adapter.TypedEventAdapter;
import org.readutf.engine.event.adapter.impl.GameEventAdapter;
import org.readutf.engine.event.exceptions.EventAdaptException;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.listener.GameListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameEventManager {

    private final Logger logger = LoggerFactory.getLogger(GameEventManager.class.getName());

    private final @NotNull GameEventPlatform gameEventPlatform;
    private final Map<Class<?>, EventGameAdapter> eventAdapters = new HashMap<>();
    private final Set<Class<?>> registeredTypes = new HashSet<>();
    private final Set<Class<?>> noAdapters = new HashSet<>();
    private final Map<Game<?, ?, ?>, Map<Class<?>, List<GameListener>>> GameListeners = new LinkedHashMap<>();
    private final Set<Class<?>> eventStackTraceEnabled = new HashSet<>();

    public GameEventManager(@NotNull GameEventPlatform gameEventPlatform) {
        this.gameEventPlatform = gameEventPlatform;
        eventAdapters.put(GameEvent.class, new GameEventAdapter());
    }

    public <T> void registerEventAdapter(Class<T> type, TypedEventAdapter<T> adapter) {
        if (eventAdapters.containsKey(type)) {
            logger.warn("Event adapter for type {} is already registered, replacing it.", type);
        }
        eventAdapters.put(type, adapter);
    }

    public <T> @NotNull T callEvent(@NotNull T event, @NotNull Game<?, ?, ?> game) throws EventDispatchException {
        logger.debug("Calling event: {}", event.getClass().getSimpleName());

        Map<Class<?>, List<GameListener>> gameListeners = GameListeners.get(game);
        if (gameListeners == null) {
            if (eventStackTraceEnabled.contains(event.getClass())) {
                logger.info("No listeners found for game: {}", game);
            }
            return event;
        }

        List<GameListener> listeners = gameListeners.get(event.getClass());
        if (listeners == null) {
            if (eventStackTraceEnabled.contains(event.getClass())) {
                logger.info("No listeners found for event type: {}", event.getClass());
            }
            return event;
        }

        for (GameListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                logger.error(
                        "Error occurred while calling {} event listener",
                        event.getClass().getSimpleName(),
                        e);
                throw new EventDispatchException("Error dispatching event", e);
            }
        }

        return event;
    }

    private void eventHandler(@NotNull Object event) {
        Class<?> eventType = event.getClass();
        @NotNull Collection<EventGameAdapter> adapters = findAdapters(event);

        if (adapters.isEmpty()) {
            if (!noAdapters.contains(eventType)) {
                noAdapters.add(eventType);
                logger.info("No event adapter found for event type: {}", eventType);
            }
            return;
        }

        Game<?, ?, ?> foundGame = null;
        for (EventGameAdapter adapter : adapters) {
            try {
                foundGame = adapter.convert(event);
            } catch (EventAdaptException e) {
                logger.error("Error occurred while converting event", e);
            }
            if (foundGame != null) break;
        }

        if (foundGame == null) return;

        try {
            callEvent(event, foundGame);
        } catch (EventDispatchException e) {
            logger.error(
                    "Error occurred while calling {} event listener",
                    event.getClass().getSimpleName(),
                    e);
        }
    }

    public void registerListener(@NotNull Game<?, ?, ?> game, @NotNull Class<?> eventClass, GameListener listener)
            throws EventDispatchException {
        if (!registeredTypes.contains(eventClass)) {
            registeredTypes.add(eventClass);
            logger.info("Registering listener for event type: {}", eventClass);
            gameEventPlatform.registerEventListener(game, eventClass, this::eventHandler);
        }

        Map<Class<?>, List<GameListener>> gameMap = GameListeners.computeIfAbsent(game, k -> new LinkedHashMap<>());
        List<GameListener> listeners = gameMap.computeIfAbsent(eventClass, k -> new ArrayList<>());

        listeners.add(listener);
    }

    public void unregisterListener(
            @NotNull Game<?, ?, ?> game, @NotNull Class<?> eventClass, @NotNull GameListener listener) {
        Map<Class<?>, List<GameListener>> map = GameListeners.get(game);
        if (map == null) return;

        List<GameListener> listeners = map.get(eventClass);
        if (listeners != null && listeners.remove(listener)) {
            logger.info("Unregistered listener for event type: {}", eventClass);
        }
    }

    public void registerAdapter(@NotNull Class<?> eventType, @NotNull GameEventAdapter adapter) {
        eventAdapters.put(eventType, adapter);
    }

    private @NotNull Collection<EventGameAdapter> findAdapters(@NotNull Object event) {
        List<EventGameAdapter> result = new ArrayList<>();
        Class<?> eventClass = event.getClass();
        for (Map.Entry<Class<?>, EventGameAdapter> entry : eventAdapters.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventClass)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public void enableEventStackTrace(Class<?> clazz) {
        eventStackTraceEnabled.add(clazz);
    }

    public void shutdown(@NotNull Game<?, ?, ?> game) {
        gameEventPlatform.unregisterListeners(game);
        GameListeners.remove(game);
    }
}