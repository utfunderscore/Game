package org.readutf.engine.feature.scoreboard;

import java.util.UUID;

public interface ScoreboardPlatform {

    void refreshScoreboard(UUID playerId, Scoreboard scoreboard);

}
