package org.readutf.engine.exception;

import org.readutf.engine.GameException;

public class GameEndException extends GameException {

    public GameEndException() {
    }

    public GameEndException(String message) {
        super(message);
    }

    public GameEndException(String message, Throwable cause) {
        super(message, cause);
    }
}
