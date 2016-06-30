package net.toyknight.aeii.manager;

/**
 * @author toyknight on 4/18/2015.
 */
public interface GameManagerListener {

    void onMapFocusRequired(int map_x, int map_y, boolean focus_viewport);

    void onGameManagerStateChanged();

    void onCampaignMessageSubmitted();

    void onCampaignObjectiveRequested();

    void onGameOver();

}
