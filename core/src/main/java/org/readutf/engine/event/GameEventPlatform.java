package org.readutf.engine.event;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.exceptions.EventDispatchException;

public interface GameEventPlatform {

    <T> void registerEventListener(@NotNull Game<?, ?, ?> game, @NotNull Class<T> type, @NotNull Consumer<T> consumer) throws EventDispatchException;

    void unregisterListeners(@NotNull Game<?, ?, ?> game);
}
