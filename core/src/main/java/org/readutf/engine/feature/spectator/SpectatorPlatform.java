package org.readutf.engine.feature.spectator;

import java.util.UUID;

public interface SpectatorPlatform {

    void setSpectatorState(UUID playerId);

    void setNormalState(UUID playerId);

}
