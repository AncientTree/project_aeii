package com.toyknight.aeii.manager;

import com.toyknight.aeii.manager.events.GameEvent;

/**
 * @author toyknight on 4/18/2015.
 */
public interface GameManagerListener {

    void onGameEventSubmitted(GameEvent event);

    void onMapFocusRequired(int map_x, int map_y);

    void onManagerStateChanged();

    void onButtonUpdateRequested();

    void onGameOver();

}
