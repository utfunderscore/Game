package org.readutf.engine.stage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.stage.exception.StageChangeException;
import org.readutf.engine.team.GameTeam;

public interface StageCreator<WORLD, ARENA extends Arena<WORLD, ?>, TEAM extends GameTeam> {

    @NotNull
    Stage<WORLD, ARENA, TEAM> startNextStage(@NotNull Game<WORLD, ARENA, TEAM> game, @Nullable Stage<WORLD, ARENA, TEAM> previousStage) throws StageChangeException;
}
