package org.readutf.engine.minestom.event;

import java.util.function.Consumer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEventPlatform;
import org.readutf.engine.event.exceptions.EventDispatchException;

public class MinestomEventPlatform implements GameEventPlatform {

    private @NotNull final EventNode<Event> eventNode;

    public MinestomEventPlatform() {
        this.eventNode = MinecraftServer.getGlobalEventHandler().addChild(EventNode.all("game-events"));
    }

    @Override
    public <T> void registerEventListener(
            @NotNull Game<?, ?, ?> game, @NotNull Class<T> type, @NotNull Consumer<T> consumer)
            throws EventDispatchException {
        if (Event.class.isAssignableFrom(type)) {
            eventNode.addListener((Class<? extends Event>) type, event -> consumer.accept((T) event));
        }
    }

    @Override
    public void unregisterListeners(@NotNull Game<?, ?, ?> game) {}
}
