package com.toyknight.aeii.manager;

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
