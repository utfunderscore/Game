package org.readutf.engine.team;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a team in the game, identified by a name and a list of players.
 */
public class GameTeam {

    private final @NotNull String teamName;
    private final List<@NotNull UUID> players = new ArrayList<>();

    /**
     * Constructs a new GameTeam with the given name.
     *
     * @param teamName the name of the team
     */
    public GameTeam(@NotNull String teamName) {
        this.teamName = teamName;
    }

    /**
     * Returns the name of the team.
     *
     * @return the team name
     */
    public @NotNull String getTeamName() {
        return teamName;
    }

    /**
     * Returns the list of player UUIDs in the team.
     *
     * @return the list of players
     */
    public @NotNull List<UUID> getPlayers() {
        return players;
    }

    /**
     * Adds a player to the team.
     *
     * @param playerId the UUID of the player
     */
    public void addPlayer(@NotNull UUID playerId) {
        players.add(playerId);
    }

    /**
     * Removes a player from the team.
     *
     * @param playerId the UUID of the player
     * @return true if the player was removed, false otherwise
     */
    public boolean removePlayer(@NotNull UUID playerId) {
        return players.remove(playerId);
    }
}
