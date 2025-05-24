package org.readutf.engine;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.markers.Position;

public interface GamePlatform<WORLD> {

    /**
     * Checks if a player is online
     *
     * @param playerId Player UUID
     * @return True if online
     */
    boolean isOnline(@NotNull UUID playerId);

    /**
     * Sends a message to a player
     *
     * @param playerId  Player UUID
     * @param component Message component
     */
    void messagePlayer(@NotNull UUID playerId, @NotNull Component component);

    @NotNull CompletableFuture<Void> teleport(@NotNull UUID playerId, @NotNull Position position, @NotNull WORLD world);

}
