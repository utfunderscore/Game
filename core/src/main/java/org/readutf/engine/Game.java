package org.readutf.engine;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEvent;
import org.readutf.engine.event.GameEventManager;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.impl.game.*;
import org.readutf.engine.event.impl.stage.StagePreChangeEvent;
import org.readutf.engine.event.listener.ListenerData;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.exception.GameEndException;
import org.readutf.engine.exception.GameStartException;
import org.readutf.engine.feature.System;
import org.readutf.engine.stage.Stage;
import org.readutf.engine.stage.StageCreator;
import org.readutf.engine.stage.exception.StageChangeException;
import org.readutf.engine.task.GameScheduler;
import org.readutf.engine.task.GameTask;
import org.readutf.engine.team.GameTeam;
import org.readutf.engine.team.TeamSelector;
import org.readutf.engine.team.exception.TeamSelectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core game class that manages the game lifecycle, players, teams, stages, and events.
 * Handles transitions between game states and coordinates game-related operations.
 *
 * @param <ARENA> The type of arena used in the game
 * @param <TEAM>  The type of team used in the game
 */
public class Game<WORLD, ARENA extends Arena<WORLD, ?>, TEAM extends GameTeam> {

    // Logger for this class
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Game unique identifier
    @NotNull
    private final UUID id;

    @NotNull
    @Getter
    private final GamePlatform<WORLD> platform;

    // Game scheduler for managing timed tasks
    @NotNull
    @Getter
    private final GameScheduler scheduler;

    // Event manager for handling game events
    @NotNull
    private final GameEventManager eventManager;

    // Team selector for assigning players to teams
    @NotNull
    private TeamSelector<TEAM> teamSelector;

    // Queue of stage creators for the game progression
    @NotNull
    private final Deque<StageCreator<WORLD, ARENA, TEAM>> stageCreators = new ArrayDeque<>();

    // Current active stage
    @Nullable
    private Stage<WORLD, ARENA, TEAM> currentStage;

    // Current active arena
    @Getter
    @Nullable
    private ARENA arena;

    // Map of team names to team instances
    @NotNull
    private final LinkedHashMap<String, TEAM> teams = new LinkedHashMap<>();

    // Current state of the game
    @NotNull
    private GameState gameState = GameState.STARTUP;

    // List of active features for the game
    @NotNull
    private final List<System> systems = new ArrayList<>();

    /**
     * Creates a new game instance with the specified components
     *
     * @param scheduler    Game scheduler for timing tasks
     * @param eventManager Event system for the game
     * @param teamSelector Team assignment strategy
     */
    public Game(
            @NotNull GamePlatform<WORLD> platform,
            @NotNull GameScheduler scheduler,
            @NotNull GameEventManager eventManager,
            @NotNull TeamSelector<TEAM> teamSelector) {
        this.platform = platform;
        this.scheduler = scheduler;
        this.eventManager = eventManager;
        this.teamSelector = teamSelector;
        this.id = UUID.randomUUID();
    }

    /**
     * Registers a new team to the game
     *
     * @param team Team to register
     */
    public void registerTeam(@NotNull TEAM team) throws GameException {
        String teamName = team.getTeamName();
        logger.info("Adding team {} to game ({})", teamName, id);

        if (teams.containsKey(teamName)) {
            logger.error("Team already exists with the name {}", teamName);
            throw new GameException("Team already exists with the name " + teamName);
        }

        teams.put(teamName, team);
    }

    /**
     * Starts the game by initializing the first stage
     *
     * @throws GameException if game is not in startup state or no arena is active
     */
    public void start() throws GameException {
        logger.info("Starting game");

        if (gameState != GameState.STARTUP) {
            logger.error("Game is not in startup state");
            throw new GameStartException("Game is not in startup state");
        }

        startNextStage();

        if (arena == null) {
            logger.error("No arena is active");
            throw new GameStartException("No arena is active");
        }

        if (currentStage != null) {
            try {
                currentStage.onStart();
            } catch (Exception e) {
                throw new GameStartException("Failed to start stage", e);
            }
        }

        gameState = GameState.ACTIVE;
    }

    /**
     * Starts the next stage in the game sequence
     *
     * @return The new active stage
     * @throws GameException if no more stages are available
     */
    @NotNull
    public Stage<WORLD, ARENA, TEAM> startNextStage() throws GameException {
        StageCreator<WORLD, ARENA, TEAM> nextStageCreator = stageCreators.pollFirst();
        if (nextStageCreator == null) {
            logger.error("No more stages to start");
            throw new StageChangeException("No more stages to start");
        }

        return startNextStage(nextStageCreator);
    }

    /**
     * Starts a specific stage using the provided creator
     *
     * @param nextStageCreator Creator for the next stage
     * @return The new active stage
     * @throws GameException if stage creation or initialization fails
     */
    @NotNull
    public Stage<WORLD, ARENA, TEAM> startNextStage(@NotNull StageCreator<WORLD, ARENA, TEAM> nextStageCreator) throws GameException {
        logger.info("Starting next stage...");

        if (currentStage != null) {
            currentStage.unregisterListeners();
            try {
                currentStage.onFinish();
            } catch (Exception e) {
                throw new GameException("Failed to start next stage", e);
            }

            for (System system : currentStage.getSystems()) {
                java.lang.System.out.println(
                        "Shutting down feature " + system.getClass().getSimpleName());
                system.shutdown();
            }
        }

        Stage<WORLD, ARENA, TEAM> previous = currentStage;
        Stage<WORLD, ARENA, TEAM> nextStage = nextStageCreator.startNextStage(this, previous);

        logger.info("Starting stage {}", nextStage.getClass().getSimpleName());

        currentStage = nextStage;

        callEvent(new StagePreChangeEvent(this, nextStage, previous));

        try {
            nextStage.onStart();

            callEvent(new StagePreChangeEvent(this, nextStage, previous));
        } catch (Exception e) {
            throw new GameException("Failed to start next stage", e);
        }

        return currentStage;
    }

    /**
     * Ends the game and performs cleanup
     *
     * @throws GameEndException if game is not in active state
     */
    public void end() throws GameEndException {
        callEvent(new GameEndEvent(this));

        if (gameState != GameState.ACTIVE) {
            logger.error("GameState is not active");
            throw new GameEndException("GameState is not active");
        }

        // TODO: Free the arena

        scheduler.cancelGameTasks(this);
        eventManager.shutdown(this);

        for (System system : systems) {
            java.lang.System.out.println(
                    "Shutting down feature " + system.getClass().getSimpleName());
            system.shutdown();
        }
        systems.clear();
    }

    /**
     * Schedules a task to run during this game
     *
     * @param gameTask Task to schedule
     */
    public void schedule(@NotNull GameTask gameTask) {
        scheduler.schedule(this, gameTask);
    }

    /**
     * Handles catastrophic game failure by cleaning up resources
     *
     * @param cause Optional cause of the crash
     * @throws Exception always thrown after cleanup
     */
    public void crash(@Nullable Throwable cause) throws Exception {
        callEvent(new GameCrashEvent(this));

        // TODO: Free arena on crash

        String reason = cause != null ? cause.toString() : "Unknown Reason";
        logger.error("Game {} crashed: {}", id, reason);

        throw new Exception("Game crashed");
    }

    /**
     * Adds a feature to the game
     *
     * @param feature Feature to add
     * @param <T>     Type of feature
     * @return The added feature
     */
    @NotNull
    public <T extends System> T addFeature(@NotNull T feature) throws EventDispatchException {
        systems.add(feature);

        for (ListenerData<?> listener : feature.getListeners()) {
            eventManager.registerListener(this, listener.type(), listener.listener());
        }

        for (GameTask task : feature.getTasks()) {
            scheduler.schedule(this, task);
        }

        return feature;
    }

    /**
     * Gets a feature by its class
     *
     * @param clazz Class of the feature
     * @param <T>   Type of feature
     * @return The feature or null if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends System> T getFeature(@NotNull Class<? extends T> clazz) {
        // First check game features
        for (System system : systems) {
            if (system.getClass().equals(clazz)) {
                return (T) system;
            }
        }

        // Then check stage features
        if (currentStage != null) {
            for (System system : currentStage.getSystems()) {
                if (system.getClass().equals(clazz)) {
                    return (T) system;
                }
            }
        }

        return null;
    }

    /**
     * Registers an event listener
     *
     * @param eventClass        Class of event to listen for
     * @param typedGameListener Listener implementation
     * @param <T>               Event type
     */
    public <T> void registerListener(@NotNull Class<T> eventClass, @NotNull TypedGameListener<T> typedGameListener)
            throws EventDispatchException {
        eventManager.registerListener(this, eventClass, typedGameListener);
    }

    /**
     * Changes the current arena
     *
     * @param arena New arena
     */
    public void changeArena(@NotNull ARENA arena) {
        logger.info("Changing to arena {}", arena);

        ARENA previousArena = this.arena;
        this.arena = arena;

        callEvent(new GameArenaChangeEvent(this, arena, previousArena));
    }

    /**
     * Registers stages to the game
     *
     * @param stageCreators Stage creators in sequence
     */
    @SafeVarargs
    public final void registerStage(@NotNull StageCreator<WORLD, ARENA, TEAM>... stageCreators) {
        Collections.addAll(this.stageCreators, stageCreators);
    }

    /**
     * Dispatches a game event
     *
     * @param event Event to dispatch
     * @param <T>   Event type
     * @return The event after processing
     */
    @NotNull
    public <T extends GameEvent> T callEvent(@NotNull T event) {
        try {
            eventManager.callEvent(event, this);
        } catch (EventDispatchException e) {
            logger.error("Event dispatch exception", e);
        }
        return event;
    }

    /**
     * Adds a player to the game
     *
     * @param playerId Player UUID
     * @throws TeamSelectException if team selection fails
     */
    public void addPlayer(@NotNull UUID playerId) throws TeamSelectException {
        TEAM team = teamSelector.getTeam(playerId);
        logger.info("Adding player {} to team {}", playerId, team.getTeamName());

        team.getPlayers().add(playerId);
        teams.put(team.getTeamName(), team);

        callEvent(new GameJoinEvent(this, playerId));
    }

    /**
     * Removes a player from the game
     *
     * @param playerId Player UUID
     * @throws Exception if player is not in a team
     */
    public void removePlayer(@NotNull UUID playerId) throws Exception {
        logger.info("Removing player {}", playerId);

        TEAM team = getTeam(playerId);
        if (team == null) {
            logger.error("GamePlayer is not in a team");
            throw new Exception("GamePlayer is not in a team");
        }

        team.getPlayers().remove(playerId);
        callEvent(new GameLeaveEvent(this, playerId));
    }

    /**
     * Sends a message to all online players
     *
     * @param component Message component
     */
    public void messageAll(@NotNull Component component) {
        for (UUID onlinePlayer : getOnlinePlayers()) {
            platform.messagePlayer(onlinePlayer, component);
        }
    }

    /**
     * Gets all players in the game
     *
     * @return List of player UUIDs
     */
    @NotNull
    public List<UUID> getPlayers() {
        return teams.values().stream()
                .flatMap(team -> team.getPlayers().stream())
                .collect(Collectors.toList());
    }

    /**
     * Gets all online players in the game
     *
     * @return List of online player UUIDs
     */
    @NotNull
    public List<UUID> getOnlinePlayers() {
        return getPlayers().stream().filter(platform::isOnline).collect(Collectors.toList());
    }

    /**
     * Gets a player's team
     *
     * @param playerId Player UUID
     * @return Team or null if not found
     */
    @Nullable
    public TEAM getTeam(@NotNull UUID playerId) {
        for (TEAM team : teams.values()) {
            if (team.getPlayers().contains(playerId)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Gets a team by name
     *
     * @param teamName Team name
     * @return Team or null if not found
     */
    @Nullable
    public TEAM getTeam(@NotNull String teamName) {
        for (Map.Entry<String, TEAM> entry : teams.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(teamName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets all teams
     *
     * @return List of teams
     */
    @NotNull
    public List<TEAM> getTeams() {
        return new ArrayList<>(teams.values());
    }

    /**
     * Gets the index of a team
     *
     * @param team Team to get index for
     * @return Team index
     */
    public int getTeamId(@NotNull TEAM team) {
        return new ArrayList<>(teams.values()).indexOf(team);
    }

    /**
     * Gets the game's unique identifier
     *
     * @return Game ID
     */
    @NotNull
    public UUID getId() {
        return id;
    }

    /**
     * Gets the game's event manager
     *
     * @return Event manager
     */
    @NotNull
    public GameEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the team selector
     *
     * @return Team selector
     */
    @NotNull
    public TeamSelector<TEAM> getTeamSelector() {
        return teamSelector;
    }

    /**
     * Sets the team selector
     *
     * @param teamSelector New team selector
     */
    public void setTeamSelector(@NotNull TeamSelector<TEAM> teamSelector) {
        this.teamSelector = teamSelector;
    }

    /**
     * Gets the current stage
     *
     * @return Current stage or null if no stage is active
     */
    @Nullable
    public Stage<WORLD, ARENA, TEAM> getCurrentStage() {
        return currentStage;
    }

    /**
     * Sets the current stage
     *
     * @param currentStage New current stage
     */
    public void setCurrentStage(@Nullable Stage<WORLD, ARENA, TEAM> currentStage) {
        this.currentStage = currentStage;
    }

    /**
     * Gets the current game state
     *
     * @return Game state
     */
    @NotNull
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the game state
     *
     * @param gameState New game state
     */
    public void setGameState(@NotNull GameState gameState) {
        this.gameState = gameState;
    }
}