package org.readutf.engine.team;

import org.readutf.engine.team.exception.TeamSelectException;

import java.util.UUID;

public interface TeamSelector<T extends GameTeam> {

    T getTeam(UUID playerId) throws TeamSelectException;
}
