package org.readutf.engine.arena.exception;

import org.readutf.engine.GameException;

public class ArenaLoadException extends GameException {

    public ArenaLoadException() {}

    public ArenaLoadException(String message) {
        super(message);
    }

    public ArenaLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArenaLoadException(Throwable cause) {
        super(cause);
    }
}
