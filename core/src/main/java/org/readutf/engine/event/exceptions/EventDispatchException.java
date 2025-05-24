package org.readutf.engine.event.exceptions;

import org.readutf.engine.GameException;

public class EventDispatchException extends GameException {
    public EventDispatchException(String message) {
        super(message);
    }

    public EventDispatchException() {
    }

    public EventDispatchException(Throwable cause) {
        super(cause);
    }

    public EventDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
