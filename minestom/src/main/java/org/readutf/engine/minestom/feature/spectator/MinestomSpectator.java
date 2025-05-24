package org.readutf.engine.minestom.feature.spectator;

import java.util.UUID;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.feature.spectator.SpectatorPlatform;
import org.readutf.engine.feature.spectator.SpectatorSystem;
import org.readutf.engine.minestom.MinestomPlatform;

public class MinestomSpectator implements SpectatorPlatform {

    private @NotNull final SpectatorSystem spectatorSystem;

    public MinestomSpectator(@NotNull SpectatorSystem spectatorSystem) {
        this.spectatorSystem = spectatorSystem;
    }

    @Override
    public void setSpectatorState(UUID playerId) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if(player == null) return;

        player.setAllowFlying(true);
        player.setFlying(true);
        player.setInvisible(true);

        player.setAutoViewable(false);
        player.updateViewableRule(viewer -> spectatorSystem.isSpectator(player.getUuid()));
    }

    @Override
    public void setNormalState(UUID playerId) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if(player == null) return;

        player.setAllowFlying(false);
        player.setFlying(false);
        player.setInvisible(false);

        player.updateViewableRule(viewer -> true);

    }
}
