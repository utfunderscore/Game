package org.readutf.engine.minestom;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.common.markers.Position;
import org.readutf.engine.GamePlatform;

public class MinestomPlatform implements GamePlatform<Instance> {

    public static @Nullable Player getPlayer(UUID playerId) {
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId);
    }

    @Override
    public boolean isOnline(@NotNull UUID playerId) {
        return getPlayer(playerId) != null;
    }

    @Override
    public void messagePlayer(@NotNull UUID playerId, @NotNull Component component) {
        Player player = getPlayer(playerId);
        if (player == null) return;
        player.sendMessage(component);
    }

    @Override
    public @NotNull CompletableFuture<Void> teleport(@NotNull UUID playerId, @NotNull Position position, @NotNull Instance instance) {
        Player player = getPlayer(playerId);
        if (player == null) return CompletableFuture.completedFuture(null);

        return player.setInstance(instance, new Pos(position.x(), position.y(), position.z()));
    }
}
