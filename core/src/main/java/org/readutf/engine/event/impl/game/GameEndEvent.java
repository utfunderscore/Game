package org.readutf.engine.event.impl.game;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

public class GameEndEvent extends GameEvent {

    public GameEndEvent(@NotNull Game<?, ?, ?> game) {
        super(game);
    }
}
