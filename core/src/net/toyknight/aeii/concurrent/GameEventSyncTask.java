package net.toyknight.aeii.concurrent;

import net.toyknight.aeii.network.NetworkManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 8/29/2015.
 */
public abstract class GameEventSyncTask extends AsyncTask<Void> {

    private final int manager_state;

    public GameEventSyncTask(int manager_state) {
        this.manager_state = manager_state;
    }

    @Override
    public final Void doTask() throws JSONException {
        NetworkManager.syncGameEvent(manager_state);
        return null;
    }

}
