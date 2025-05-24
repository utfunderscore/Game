package org.readutf.engine.feature;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.event.listener.GameListener;
import org.readutf.engine.event.listener.ListenerData;
import org.readutf.engine.task.GameTask;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface System {

    /**
     * Returns a map of event types to their corresponding listeners.
     * Subclasses can override to provide actual event listeners.
     */
    default @NotNull List<ListenerData<?>> getListeners() {
        return List.of();
    }

    /**
     * Returns a list of tasks to be run for this feature.
     * Subclasses can override to provide scheduled tasks.
     */
    default @NotNull List<GameTask> getTasks() {
        return Collections.emptyList();
    }

    /**
     * Shutdown logic to be implemented by subclasses.
     */
    default void shutdown() {}
}
