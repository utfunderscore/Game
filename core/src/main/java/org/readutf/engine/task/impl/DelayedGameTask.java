package org.readutf.engine.task.impl;

import org.readutf.engine.task.GameTask;

public abstract class DelayedGameTask extends GameTask {

    protected final long delay;

    public DelayedGameTask(long delay) {
        this.delay = delay;
    }

    public abstract void run();

    @Override
    public void tick() {
        if (System.currentTimeMillis() - startTime >= delay) {
            run();
            markForRemoval();
        }
    }
}
