package org.readutf.engine.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;

@Getter
@RequiredArgsConstructor
public class GameEvent {

    private @NotNull final Game<?, ?, ?> game;
}
