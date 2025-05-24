package org.readutf.engine.task;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class GameScheduler {

    private static final Logger logger = LoggerFactory.getLogger(GameScheduler.class);

    private final Map<@NotNull UUID, @NotNull List<@NotNull GameTask>> gameTasks = new HashMap<>();


    public GameScheduler(GameSchedulerPlatform platform) {
        platform.scheduleTask(() -> {
            Map<UUID, List<GameTask>> copy = new HashMap<>(gameTasks);
            gameTasks.clear();

            for (Map.Entry<UUID, List<GameTask>> entry : copy.entrySet()) {
                UUID gameId = entry.getKey();
                List<GameTask> tasks = entry.getValue();

                for (@NotNull GameTask task : tasks) {
                    if (task.isMarkedForRemoval()) continue;
                    task.tick();

                    if (!task.isMarkedForRemoval()) {
                        gameTasks.computeIfAbsent(gameId, k -> new ArrayList<>()).add(task);
                    }
                }
            }
        });
    }

    /**
     * Schedules a task tied to a specific game instance.
     *
     * @param game     the game to associate the task with
     * @param gameTask the task to schedule
     */
    public void schedule(@NotNull Game<?, ?, ?> game, @NotNull GameTask gameTask) {
        logger.info("Scheduling task {}", gameTask);
        gameTasks.computeIfAbsent(game.getId(), k -> new ArrayList<>()).add(gameTask);
    }

    /**
     * Schedules a task tied to a specific stage. The task only runs if the stage is still active.
     *
     * @param stage    the stage to bind the task to
     * @param gameTask the task to execute
     */
    public void schedule(@NotNull Stage<?, ?, ?> stage, @NotNull GameTask gameTask) {
        logger.info("Scheduling task {}", gameTask);

        GameTask wrapped = new GameTask() {
            @Override
            public void tick() {
                if (stage.getGame().getCurrentStage() == stage) {
                    gameTask.tick();
                }
            }
        };

        schedule(stage.getGame(), wrapped);
    }

    /**
     * Cancels all scheduled tasks for a given game by marking them for removal.
     *
     * @param game the game whose tasks should be cancelled
     */
    public void cancelGameTasks(@NotNull Game<?, ?, ?> game) {
        logger.info("Cancelling tasks for game {}", game.getId());

        List<GameTask> tasks = gameTasks.get(game.getId());
        if (tasks != null) {
            for (GameTask task : tasks) {
                task.markForRemoval();
            }
        }
    }
}
