package org.readutf.engine.minestom.feature.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.*;
import org.readutf.engine.feature.scoreboard.Scoreboard;
import org.readutf.engine.feature.scoreboard.ScoreboardPlatform;
import org.readutf.engine.minestom.MinestomPlatform;

public class MinestomScoreboard implements ScoreboardPlatform {

    private final Map<UUID, List<Component>> previousLines = new HashMap<>();
    private final Map<UUID, Component> previousTitle = new HashMap<>();
    private final Map<UUID, Sidebar> nativeSidebar = new HashMap<>();

    @Override
    public void refreshScoreboard(UUID playerId, Scoreboard scoreboard) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if (player == null) return;

        Component newTitle = scoreboard.getTitle(playerId);

        Sidebar sideBar = nativeSidebar.get(playerId);
        if (sideBar == null) {
            sideBar = new Sidebar(newTitle);
            sideBar.addViewer(player);
            nativeSidebar.put(playerId, sideBar);
        }

        Component prevTitle = previousTitle.getOrDefault(player.getUuid(), Component.empty());
        if (!prevTitle.equals(newTitle)) {
            sideBar.setTitle(newTitle);
            previousTitle.put(player.getUuid(), newTitle);
        }

        List<Component> newLines = scoreboard.getLines(playerId);
        List<Component> prevLines = previousLines.getOrDefault(player.getUuid(), Collections.emptyList());

        if (newLines.size() == prevLines.size()) {
            boolean allMatch = true;
            for (int i = 0; i < newLines.size(); i++) {
                if (!newLines.get(i).equals(prevLines.get(i))) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) return;
        }

        int newLinesSize = newLines.size();

        for (int index = 0; index < newLines.size(); index++) {
            Component line = newLines.get(index);
            String lineKey = Integer.toString(index);
            if (sideBar.getLine(lineKey) == null) {
                sideBar.createLine(new Sidebar.ScoreboardLine(lineKey, line, newLinesSize - index));
            } else {
                sideBar.updateLineContent(lineKey, line);
            }
        }

        previousLines.put(player.getUuid(), newLines);
    }
}
