package org.readutf.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class GameManager {

    private @NotNull static final Map<UUID, Game<?, ?, ?>> gameTracker = new HashMap<>();

    public static void register(Game<?, ?, ?> game) {
        gameTracker.put(game.getId(), game);
    }

    public static @Nullable Game<?, ?, ?> getGame(UUID uuid) {
        return gameTracker.get(uuid);
    }

    public static @NotNull Collection<Game<?, ?, ?>> getGames() {
        return gameTracker.values();
    }

}
