package com.toyknight.aeii.manager;

/**
 * @author toyknight on 4/18/2015.
 */
public interface GameManagerListener {

    void onMapFocusRequired(int map_x, int map_y);

    void onScreenUpdateRequested();

    void onGameOver();

}
