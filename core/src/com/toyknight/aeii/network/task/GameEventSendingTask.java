package com.toyknight.aeii.network.task;

import com.toyknight.aeii.concurrent.AsyncTask;
import com.toyknight.aeii.network.NetworkManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 8/29/2015.
 */
public abstract class GameEventSendingTask extends AsyncTask<Void> {

    private final JSONObject event;

    public GameEventSendingTask(JSONObject event) {
        this.event = event;
    }

    public final JSONObject getEvent() {
        return event;
    }

    @Override
    public final Void doTask() throws JSONException {
        NetworkManager.sendGameEvent(getEvent());
        return null;
    }

}
