package org.readutf.engine.event.impl.stage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;
import org.readutf.engine.stage.Stage;

@Getter
public class StagePreChangeEvent extends GameEvent {

    private final @NotNull Stage<?, ?, ?> previousStage;
    private final @NotNull Stage<?, ?, ?> currentStage;

    public StagePreChangeEvent(@NotNull Game<?, ?,?> game, @NotNull Stage<?, ?, ?> previousStage, @NotNull Stage<?, ?, ?> currentStage) {
        super(game);
        this.previousStage = previousStage;
        this.currentStage = currentStage;
    }
}
