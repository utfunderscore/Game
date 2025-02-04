package org.readutf.game.engine

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.impl.*
import org.readutf.game.engine.schedular.GameSchedulerFactory
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.SResult
import java.util.UUID
import java.util.function.Predicate
import kotlin.jvm.Throws

typealias GenericGame = Game<*, *>

abstract class Game<ARENA : Arena<*>, TEAM : GameTeam>(
    private val schedulerFactory: GameSchedulerFactory,
    val eventManager: GameEventManager,
) {
    private val logger = KotlinLogging.logger { }
    val gameId: String = GameManager.generateGameId()
    private var stageCreators: ArrayDeque<StageCreator<ARENA, TEAM>> = ArrayDeque()
    var currentStage: Stage<ARENA, TEAM>? = null
    var arena: ARENA? = null
    private var teams = LinkedHashMap<String, TEAM>()
    var gameState: GameState = GameState.STARTUP
    val scheduler = schedulerFactory.build()

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun registerTeam(team: TEAM): SResult<Unit> {
        val teamName = team.teamName
        logger.info { "Adding team $teamName to game ($gameId)" }
        if (teams.containsKey(teamName)) return Err("Team already exists with the name $teamName")

        teams[teamName] = team

        return Ok(Unit)
    }

    fun start(): SResult<Unit> {
        logger.info { "Starting game" }
        GameManager.activeGames[gameId] = this
        if (gameState != GameState.STARTUP) {
            return Err("Game is not in startup state")
        }
        startNextStage().getOrElse { return Err(it) }

        if (arena == null) return Err("No arena is active")

        currentStage?.onStart()
        gameState = GameState.ACTIVE
        return Ok(Unit)
    }

    fun startNextStage(): SResult<Stage<ARENA, TEAM>> {
        logger.info { "Starting next stage" }

        val nextStageCreator = stageCreators.removeFirstOrNull() ?: return Err("No more stages to start")

        return startNextStage(nextStageCreator)
    }

    fun startNextStage(nextStageCreator: StageCreator<ARENA, TEAM>): SResult<Stage<ARENA, TEAM>> {
        val localCurrentStage = currentStage
        if (localCurrentStage != null) {
            localCurrentStage.unregisterListeners()
            localCurrentStage.onFinish().getOrElse { return Err(it) }
        }

        val previous = currentStage
        val nextStage = nextStageCreator.create(this, previous).getOrElse { return Err(it) }
        currentStage = nextStage

        callEvent(StageStartEvent(nextStage, previous))

        nextStage.onStart().getOrElse { return Err(it) }

        return Ok(currentStage!!)
    }

    fun end(): SResult<Unit> {
        callEvent(GameEndEvent(this))

        if (gameState != GameState.ACTIVE) {
            return Err("GameState is not active")
        }

        arena?.free()

        return Ok(Unit)
    }

    @Throws(Exception::class)
    fun crash(result: SResult<*>?) {
        callEvent(GameCrashEvent(this))

        arena?.free()

        logger.error { "Game $gameId crashed: ${result?.getOr(null) ?: "Unknown Reason"}" }

        throw Exception("Game crashed")
    }

    fun changeArena(arena: ARENA) {
        logger.info { "Changing to arena $arena" }

        this.arena = arena

        if (gameState == GameState.ACTIVE) {
            for (onlinePlayer in getOnlinePlayers()) {
//                spawnPlayer(onlinePlayer)
            }
        }
    }

    fun registerStage(vararg stageCreator: StageCreator<ARENA, TEAM>) {
        stageCreators.addAll(stageCreator)
    }

    fun <T : GameEvent> callEvent(event: T): T {
        eventManager.callEvent(event, this)
        return event
    }

    fun getPlayers(): List<UUID> = teams.values.flatMap { it.players }

    abstract fun getOnlinePlayers(): List<UUID>

    abstract fun messagePlayer(playerId: UUID, component: Component)

    private fun addPlayer(
        playerId: UUID,
        team: GameTeam,
    ): SResult<Unit> {
        logger.info { "Adding player $playerId to team ${team.teamName}" }

        GameManager.playerToGame[playerId] = this
        team.players.add(playerId)
        callEvent(GameJoinEvent(this, playerId))

        return Ok(Unit)
    }

    fun addPlayer(
        playerId: UUID,
        teamName: String,
    ): SResult<Unit> {
        val team = teams[teamName] ?: return Err("Team $teamName does not exist")
        return addPlayer(playerId, team)
    }

    fun addPlayer(
        player: UUID,
        predicate: Predicate<TEAM>,
    ): SResult<Unit> {
        val team =
            teams.values.firstOrNull { predicate.test(it) }
                ?: return Err("No team matches the predicate")

        return addPlayer(player, team)
    }

    fun removePlayer(playerId: UUID): SResult<Unit> {
        logger.info { "Removing player $playerId" }

        val team = getTeam(playerId) ?: return Err("GamePlayer is not in a team")
        team.players.remove(playerId)
        callEvent(GameLeaveEvent(this, playerId))

        return Ok(Unit)
    }

    fun getTeam(playerId: UUID): TEAM? = teams.values.find { it.players.contains(playerId) }

    fun getTeam(teamName: String): TEAM? = teams.entries.firstOrNull { entry -> entry.key.equals(teamName, true) }?.value

    fun getTeams(): List<TEAM> = teams.values.toList()

    fun getArena(): SResult<ARENA> = arena?.let { Ok(it) } ?: Err("No arena is active")

    fun messageAll(component: Component) {
        for (onlinePlayer in getOnlinePlayers()) {
            messagePlayer(onlinePlayer, component)
        }
    }

    fun getTeamId(team: GameTeam): Int = teams.values.indexOf(team)
}
