package org.readutf.engine.team.exception;

import org.readutf.engine.GameException;

public class TeamSelectException extends GameException {

    public TeamSelectException() {
    }

    public TeamSelectException(String message) {
        super(message);
    }

    public TeamSelectException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamSelectException(Throwable cause) {
        super(cause);
    }
}
