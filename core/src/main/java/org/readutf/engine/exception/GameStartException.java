package org.readutf.engine.exception;

import org.readutf.engine.GameException;

public class GameStartException extends GameException {
    public GameStartException() {
    }

    public GameStartException(String message) {
        super(message);
    }

    public GameStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
