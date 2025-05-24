package org.readutf.engine.arena;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.exception.BuildFormatException;
import org.readutf.buildformat.common.meta.BuildMeta;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;

/**
 * Represents a platform for managing arenas in a specific world/environment.
 * <p>
 * This interface provides methods for placing builds into a world and freeing arenas.
 * It is designed to be implemented for specific world types.
 *
 * @param <WORLD> the type representing the world/environment where arenas are managed
 */
public interface ArenaPlatform<WORLD> {

    /**
     * Places the build into a world, and returns the origin
     */
    @NotNull
    BuildPlacement<WORLD> placeBuild(int buildId, @NotNull BuildMeta build) throws ArenaLoadException, BuildFormatException;

    /**
     * Frees an arena.
     */
    void freeArena(Arena<WORLD, ?> arena) throws ArenaLoadException;
}
