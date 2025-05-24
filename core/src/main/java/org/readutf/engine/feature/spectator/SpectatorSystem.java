package org.readutf.engine.feature.spectator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.feature.System;
import org.readutf.engine.feature.spawning.SpawningSystem;
import org.readutf.engine.task.GameTask;

/**
 * System responsible for managing spectators in the game.
 */
public class SpectatorSystem implements System {

    /**
     * Reference to the game instance.
     */
    private final @NotNull Game<?, ?, ?> game;

    /**
     * Handler responsible for respawning players.
     */
    private final @NotNull SpawningSystem respawnHandler;

    /**
     * Platform-specific spectator logic.
     */
    private final @NotNull SpectatorPlatform spectatorPlatform;

    /**
     * Map of player UUIDs to their spectator data.
     */
    private final @NotNull Map<UUID, SpectatorData> spectators = new HashMap<>();

    /**
     * Constructs a new SpectatorSystem.
     *
     * @param game              the game instance (must not be null)
     * @param respawnHandler    the respawn handler (must not be null)
     * @param spectatorPlatform the spectator platform logic (must not be null)
     */
    public SpectatorSystem(
            @NotNull Game<?, ?, ?> game,
            @NotNull SpawningSystem respawnHandler,
            @NotNull SpectatorPlatform spectatorPlatform) {
        this.game = game;
        this.respawnHandler = respawnHandler;
        this.spectatorPlatform = spectatorPlatform;
    }

    /**
     * Gets the repeating tasks for this system.
     *
     * @return a list of game tasks (never null)
     */
    @Override
    public @NotNull List<GameTask> getTasks() {
        return List.of(new SpectatorTask(this));
    }

    /**
     * Sets a player as a spectator with the given data.
     *
     * @param spectatorData the spectator data (must not be null)
     */
    public void setSpectator(@NotNull SpectatorData spectatorData) {
        spectators.put(spectatorData.getPlayerId(), spectatorData);

        game.callEvent(new SpectatorEvent(game, spectatorData));

        spectatorPlatform.setSpectatorState(spectatorData.getPlayerId());

        spectators.put(spectatorData.getPlayerId(), spectatorData);
    }

    /**
     * Respawns a spectator player.
     *
     * @param spectatorData the spectator data (must not be null)
     */
    public void respawnSpectator(@NotNull SpectatorData spectatorData) {
        SpectatorData data = spectators.get(spectatorData.getPlayerId());
        if (data == null) return;

        spectators.remove(spectatorData.getPlayerId());
        respawnHandler.spawn(data.getPlayerId());
        spectatorPlatform.setNormalState(data.getPlayerId());
    }

    /**
     * Gets a list of online spectators' UUIDs.
     *
     * @return a list of online spectator UUIDs (never null)
     */
    public @NotNull List<UUID> getOnlineSpectators() {
        List<UUID> onlineSpectators = new ArrayList<>();
        for (UUID uuid : spectators.keySet()) {
            if (game.getOnlinePlayers().contains(uuid)) {
                onlineSpectators.add(uuid);
            }
        }
        return onlineSpectators;
    }

    /**
     * Checks if a player is alive (not a spectator).
     *
     * @param playerId the player UUID (must not be null)
     * @return true if alive, false if spectator
     */
    public boolean isAlive(@NotNull UUID playerId) {
        return !spectators.containsKey(playerId);
    }

    /**
     * Checks if a player is a spectator.
     *
     * @param playerId the player UUID (must not be null)
     * @return true if spectator, false otherwise
     */
    public boolean isSpectator(@NotNull UUID playerId) {
        return spectators.containsKey(playerId);
    }

    /**
     * Gets a list of alive (non-spectator) players' UUIDs.
     *
     * @return a list of alive players' UUIDs (never null)
     */
    public @NotNull List<UUID> getAlivePlayers() {
        List<UUID> alivePlayers = new ArrayList<>();
        for (UUID uuid : game.getOnlinePlayers()) {
            if (!spectators.containsKey(uuid)) {
                alivePlayers.add(uuid);
            }
        }
        return alivePlayers;
    }

    /**
     * Gets a list of all spectator data objects.
     *
     * @return a list of spectator data (never null)
     */
    public @NotNull List<SpectatorData> getSpectators() {
        return new ArrayList<>(spectators.values());
    }

    /**
     * Gets the spectator data for a player, or null if not a spectator.
     *
     * @param playerId the player UUID (must not be null)
     * @return the spectator data, or null if not found
     */
    public @Nullable SpectatorData getSpectatorData(@NotNull UUID playerId) {
        return spectators.get(playerId);
    }
}