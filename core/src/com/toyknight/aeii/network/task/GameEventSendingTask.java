package com.toyknight.aeii.network.task;

import com.toyknight.aeii.concurrent.AsyncTask;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.network.NetworkManager;

/**
 * @author toyknight 8/29/2015.
 */
public abstract class GameEventSendingTask extends AsyncTask<Void> {

    private final GameEvent event;

    public GameEventSendingTask(GameEvent event) {
        this.event = event;
    }

    public final GameEvent getEvent() {
        return event;
    }

    @Override
    public final Void doTask() {
        NetworkManager.sendGameEvent(getEvent());
        return null;
    }

}
