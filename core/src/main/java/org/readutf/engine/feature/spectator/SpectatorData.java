package org.readutf.engine.feature.spectator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.readutf.buildformat.common.markers.Position;

/**
 * Represents the data of a spectator
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public final class SpectatorData {
    private final UUID playerId;
    private final LocalDateTime deathTime;
    private LocalDateTime respawnTime;
    private boolean respawn;
    private Position position;
}