package com.toyknight.aeii.net.task;

import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;

import java.io.IOException;

/**
 * Created by toyknight on 8/29/2015.
 */
public class GameEventSendingTask extends AsyncTask<Void> {

    private final GameEvent event;

    public GameEventSendingTask(GameEvent event) {
        this.event = event;
    }

    @Override
    public Void doTask() throws IOException {
        GameHost.getContext().getNetworkManager().sendGameEvent(event);
        return null;
    }

    @Override
    public void onFinish(Void result) {
    }

    @Override
    public void onFail(String message) {
    }

}
