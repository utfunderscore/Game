package org.readutf.engine.event.impl.game;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEvent;

@Getter
public class GameArenaChangeEvent extends GameEvent {

    private final @NotNull Arena<?, ?> arena;
    private final @Nullable Arena<?, ?> previousArena;

    public GameArenaChangeEvent(@NotNull Game<?, ?, ?> game, @NotNull Arena<?, ?> arena, @Nullable Arena<?, ?> previousArena) {
        super(game);
        this.arena = arena;
        this.previousArena = previousArena;
    }
}
