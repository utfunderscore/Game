## Arena Management

One of the pain points when building gamemodes that work across
multiple maps is managing spawn points, objectives, and other position
data. This arena system aims to make it easier to manage these.

### Placing a marker

A marker is a sign that is placed within the arena, and defines a position.
Markers must follow the following format:

```
[Marker]
<name>
<offset>
```

Where `<name>` is the name of the marker, and `<offset>` is the offset from the sign block.
> [!NOTE]\
> Offsets are optional, but can be used to fine-tune the position of the marker,
> or define a position that does not have a valid block to place the sign on.

### Defining position requirements

The following is an example of
a [PositionData](Engine/src/main/kotlin/org/readutf/game/engine/settings/location/PositionData.kt) class taken
from a relevant test.

```kotlin
/**
 * Data class representing an imaginary game mode's position requirements
 */
class ValidPositionData(
    @Position("testPosition") val testPosition: Marker,
    @Position(startsWith = "testListPositions") val startWith: List<Marker>,
    @Position(endsWith = "endsWithPosition") val endsWith: Marker,
    val subClassPosition: SubPositionData
) : PositionData

/**
 * A subclass of [ValidPositionData] with nested position data
 */
class SubPositionData(
    @Position("subClassPosition") val innerClass: Marker
) : PositionData
```

This class defines a set of positions that are required for our imaginary gamemode to function.
When creating an arena, we can validate that the arena has the correct markers to satisfy these requirements.
This ensures that build types and development environments have the correct markers and prevents
runtime errors due to missing markers.

## Building the project

This project uses the Gradle build system. To build the project, run the following command:

```
./gradlew shadowJar
```

The Engine module is intended to be used as a library and does not produce an executable jar.
To start a demo game server, a minestom server is generated at /Server/build/libs/Server
