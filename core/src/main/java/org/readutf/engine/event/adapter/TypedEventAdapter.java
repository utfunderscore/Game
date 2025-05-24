package org.readutf.engine.event.adapter;

import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.event.exceptions.EventAdaptException;

public interface TypedEventAdapter<T> extends EventGameAdapter {

    @Nullable
    Game<?, ?, ?> convertEvent(T event);

    @Override
    @Nullable
    default Game<?, ?, ?> convert(Object event) throws EventAdaptException {
        try {
            return convertEvent((T) event);
        } catch (Exception e) {
            throw new EventAdaptException(e);
        }
    }
}
