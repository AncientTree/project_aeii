package com.toyknight.aeii.net.task;

import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.manager.GameEvent;

/**
 * @author toyknight 8/29/2015.
 */
public abstract class GameEventSendingTask extends AsyncTask<Void> {

    protected final GameEvent event;

    public GameEventSendingTask(GameEvent event) {
        this.event = event;
    }

    public GameEvent getEvent() {
        return event;
    }

}
