package org.readutf.engine.event.exceptions;

import org.readutf.engine.GameException;

public class EventAdaptException extends GameException {

    public EventAdaptException() {
    }

    public EventAdaptException(String message) {
        super(message);
    }

    public EventAdaptException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventAdaptException(Throwable cause) {
        super(cause);
    }
}
