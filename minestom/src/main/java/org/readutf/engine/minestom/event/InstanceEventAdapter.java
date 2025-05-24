package org.readutf.engine.minestom.event;

import net.minestom.server.event.trait.InstanceEvent;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.event.adapter.EventGameAdapter;
import org.readutf.engine.event.adapter.TypedEventAdapter;
import org.readutf.engine.minestom.PlatformUtils;
import org.readutf.engine.minestom.arena.MinestomArenaPlatform;

public class InstanceEventAdapter implements TypedEventAdapter<InstanceEvent> {

    @Override
    public @Nullable Game<?, ?, ?> convertEvent(InstanceEvent event) {
        return PlatformUtils.getGame(event.getInstance());
    }
}
