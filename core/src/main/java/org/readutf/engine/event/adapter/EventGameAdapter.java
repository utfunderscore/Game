package org.readutf.engine.event.adapter;

import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.event.exceptions.EventAdaptException;

public interface EventGameAdapter {

    /**
     * Converts an event to a game.
     *
     * @param event the event to convert
     * @return the converted game, or null if the conversion failed
     */
    @Nullable
    Game<?, ?,?> convert(Object event) throws EventAdaptException;
}
