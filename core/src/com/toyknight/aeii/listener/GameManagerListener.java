package com.toyknight.aeii.listener;

/**
 * Created by toyknight on 4/18/2015.
 */
public interface GameManagerListener {

    public void onMapFocusRequired(int map_x, int map_y);

    public void onManagerStateChanged(int last_state);

    public void onScreenUpdateRequested();

}
