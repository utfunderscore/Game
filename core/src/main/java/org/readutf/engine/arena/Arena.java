package org.readutf.engine.arena;

import java.util.Objects;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.markers.Position;

import java.util.List;
import org.readutf.buildformat.common.meta.BuildMeta;

@Getter
public final class Arena<WORLD, T extends BuildFormat> {
    @Getter private final int arenaId;
    private @NotNull final BuildMeta buildMeta;
    private @NotNull final WORLD world;
    private @NotNull final T format;
    private @NotNull final List<Position> positions;

    public Arena(
            int arenaId,
            @NotNull BuildMeta buildMeta,
            @NotNull WORLD world,
            @NotNull T format,
            @NotNull List<Position> positions
    ) {
        this.arenaId = arenaId;
        this.buildMeta = buildMeta;
        this.world = world;
        this.format = format;
        this.positions = positions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Arena) obj;
        return this.arenaId == that.arenaId &&
                Objects.equals(this.buildMeta, that.buildMeta) &&
                Objects.equals(this.world, that.world) &&
                Objects.equals(this.format, that.format) &&
                Objects.equals(this.positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arenaId, buildMeta, world, format, positions);
    }

    @Override
    public String toString() {
        return "Arena[" +
                "arenaId=" + arenaId + ", " +
                "build=" + buildMeta + ", " +
                "world=" + world + ", " +
                "format=" + format + ", " +
                "positions=" + positions + ']';
    }

}
