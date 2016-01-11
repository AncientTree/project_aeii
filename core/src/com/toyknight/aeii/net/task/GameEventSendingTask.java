package com.toyknight.aeii.net.task;

import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.net.NetworkManager;

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
