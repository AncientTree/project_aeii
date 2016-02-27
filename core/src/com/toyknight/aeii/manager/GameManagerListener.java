package com.toyknight.aeii.manager;

import org.json.JSONObject;

/**
 * @author toyknight on 4/18/2015.
 */
public interface GameManagerListener {

    void onGameEventSubmitted(JSONObject event);

    void onMapFocusRequired(int map_x, int map_y);

    void onManagerStateChanged();

    void onScreenUpdateRequested();

    void onGameOver();

}
