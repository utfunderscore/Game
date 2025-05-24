package org.readutf.engine.task;

import lombok.Getter;

public abstract class GameTask {

    protected long startTime = System.currentTimeMillis();

    @Getter
    protected boolean markedForRemoval = false;

    public abstract void tick();

    public void markForRemoval() {
        markedForRemoval = true;
    }

    public void cancel() {
        markForRemoval();
    }
}
