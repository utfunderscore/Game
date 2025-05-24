package org.readutf.engine.minestom.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.event.adapter.TypedEventAdapter;
import org.readutf.engine.minestom.PlatformUtils;

public class EntityEventAdapter implements TypedEventAdapter<EntityEvent> {
    @Override
    public @Nullable Game<?, ?, ?> convertEvent(EntityEvent event) {

        var entity = event.getEntity();

        if (entity instanceof Player player) {
            return GameManager.getGame(player.getUuid());
        }

        var instance = entity.getInstance();

        return PlatformUtils.getGame(instance);
    }
}
