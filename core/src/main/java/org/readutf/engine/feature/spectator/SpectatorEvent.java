package org.readutf.engine.feature.spectator;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

public class SpectatorEvent extends GameEvent {

    private final @NotNull SpectatorData spectatorData;

    public SpectatorEvent(@NotNull Game<?, ?, ?> game, @NotNull SpectatorData spectatorData) {
        super(game);
        this.spectatorData = spectatorData;
    }
}
