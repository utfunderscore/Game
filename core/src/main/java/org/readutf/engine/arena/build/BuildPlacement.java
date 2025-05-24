package org.readutf.engine.arena.build;

import org.readutf.buildformat.common.markers.Marker;
import org.readutf.buildformat.common.markers.Position;
import java.util.List;

public record BuildPlacement<WORLD>(WORLD world, Position origin, List<Marker> markers) {}
