package org.readutf.engine.event.impl.game;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

import java.util.UUID;

@Getter
public class GameLeaveEvent extends GameEvent {

    private final @NotNull UUID playerId;

    public GameLeaveEvent(@NotNull Game<?, ?, ?> game, @NotNull UUID playerId) {
        super(game);
        this.playerId = playerId;
    }
}
