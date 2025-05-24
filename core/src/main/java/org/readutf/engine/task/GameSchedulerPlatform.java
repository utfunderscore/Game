package org.readutf.engine.task;

public interface GameSchedulerPlatform {

    /**
     * Schedules a task. The implementation defines how this is actually run.
     *
     * @param runnable the task to execute
     */
    void scheduleTask(Runnable runnable);

}
