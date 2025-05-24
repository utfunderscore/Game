package org.readutf.engine.arena;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.BuildManager;
import org.readutf.buildformat.common.exception.BuildFormatException;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.format.BuildFormatChecksum;
import org.readutf.buildformat.common.format.BuildFormatManager;
import org.readutf.buildformat.common.format.requirements.RequirementData;
import org.readutf.buildformat.common.markers.Marker;
import org.readutf.buildformat.common.meta.BuildMeta;
import org.readutf.buildformat.common.meta.BuildMetaStore;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the loading and retrieval of arenas within the game engine.
 * <p>
 * Responsible for:
 * <ul>
 *   <li>Loading arenas by name and build format, assigning unique IDs, and placing them in the world.</li>
 *   <li>Retrieving lists of build names that match specific formats and checksums.</li>
 * </ul>
 *
 * @param <WORLD> the type representing the world/environment where arenas are placed
 */
@RequiredArgsConstructor
@Slf4j
public class ArenaManager<WORLD> {

    private @NotNull final BuildMetaStore buildMetaStore;
    private @NotNull final ArenaPlatform<WORLD> arenaPlatform;
    private @NotNull final AtomicInteger idTracker;

    /**
     * Loads an arena by name and build format class.
     * Retrieves the build, assigns a unique ID, places it in the world,
     * constructs the format, and returns a new Arena instance.
     *
     * @param name     the name of the build to load
     * @param clazz    the class of the build format
     * @param <FORMAT> the type of the build format
     * @return a new Arena instance or null if the build is not found
     * @throws ArenaLoadException if loading fails or the build format is invalid
     */
    public @Nullable <FORMAT extends BuildFormat> Arena<WORLD, FORMAT> loadArena(String name, @NotNull Class<FORMAT> clazz) throws ArenaLoadException {
        @Nullable BuildMeta buildMeta;
        try {
            buildMeta = buildMetaStore.getByName(name);

            if (buildMeta == null) {
                return null;
            }

            int id = idTracker.getAndIncrement();
            BuildPlacement<WORLD> placement = arenaPlatform.placeBuild(id, buildMeta);

            FORMAT format;

            format = BuildFormatManager.constructBuildFormat(placement.markers(), clazz);

            return new Arena<>(
                    id,
                    buildMeta,
                    placement.world(),
                    format,
                    placement.markers().stream().map(Marker::getTargetPosition).toList()
            );

        } catch (BuildFormatException e) {
            throw new ArenaLoadException(e);
        }
    }

    /**
     * Retrieves a list of build names that match the specified format and class.
     *
     * @param format the format identifier to filter builds
     * @param kClass the class of the build format to validate against
     * @param <T>    the type of the build format
     * @return a list of build names matching the format and checksum
     * @throws BuildFormatException if validation or checksum generation fails
     */
    public <T extends BuildFormat> @NotNull List<String> getByFormat(@NotNull String format, @NotNull Class<T> kClass) throws BuildFormatException {
        Map<String, BuildFormatChecksum> buildsByFormat = buildMetaStore.getBuildsByFormat(format);

        List<RequirementData> requirements = BuildFormatManager.getValidators(kClass);
        byte[] targetChecksum = BuildFormatManager.generateChecksum(requirements);

        List<String> builds = new ArrayList<>();

        for (Map.Entry<String, BuildFormatChecksum> entry : buildsByFormat.entrySet()) {
            String build = entry.getKey();
            BuildFormatChecksum checksum = entry.getValue();

            if (Arrays.equals(checksum.checksum(), targetChecksum)) {
                builds.add(build);
            } else {
                log.info("Invalid checksum received for {} {} != {}", build, Arrays.toString(checksum.checksum()), Arrays.toString(targetChecksum));
            }
        }

        return builds;
    }


}
