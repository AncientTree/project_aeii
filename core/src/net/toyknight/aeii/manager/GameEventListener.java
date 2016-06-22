package net.toyknight.aeii.manager;

import org.json.JSONObject;

/**
 * @author toyknight 11/2/2015.
 */
public interface GameEventListener {

    void onGameEventSubmitted(JSONObject event);

    void onGameEventFinished();

}
