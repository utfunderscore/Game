package org.readutf.engine.task.impl;

import org.readutf.engine.task.GameTask;

public abstract class RepeatingGameTask extends GameTask {

    protected final long delay;
    protected final long period;
    protected long lastTick = Long.MAX_VALUE;

    public RepeatingGameTask(long delay, long period) {
        this.delay = delay;
        this.period = period;
    }

    public abstract void run();

    @Override
    public void tick() {
        long now = System.currentTimeMillis();
        long sinceLastTick = now - lastTick;
        long sinceStart = now - startTime;

        if (sinceLastTick >= period) {
            run();
            return;
        }

        if (sinceStart >= delay) {
            run();
            lastTick = now;
        }
    }
}
