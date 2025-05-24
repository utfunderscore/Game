package org.readutf.engine.feature.spawning;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.common.markers.Position;

@FunctionalInterface
public interface SpawnFinder {

    @Nullable Position find(UUID playerId);

}
