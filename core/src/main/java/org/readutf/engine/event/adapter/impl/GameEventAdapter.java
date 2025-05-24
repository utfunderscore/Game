package org.readutf.engine.event.adapter.impl;

import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;
import org.readutf.engine.event.adapter.EventGameAdapter;
import org.readutf.engine.event.adapter.TypedEventAdapter;

public class GameEventAdapter implements TypedEventAdapter<GameEvent> {
    @Override
    public @Nullable Game<?, ?, ?> convertEvent(GameEvent event) {
        return event.getGame();
    }
}
