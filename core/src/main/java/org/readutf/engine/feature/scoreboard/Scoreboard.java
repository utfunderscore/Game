package org.readutf.engine.feature.scoreboard;

import java.util.UUID;
import net.kyori.adventure.text.Component;
import java.util.List;

public interface Scoreboard {
    Component getTitle(UUID playerId);

    List<Component> getLines(UUID playerId);
}