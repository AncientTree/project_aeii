package net.toyknight.aeii.server;

import org.json.JSONObject;

/**
 * @author toyknight 7/18/2016.
 */
public interface RoomListener {

    void onGameEventExecuted(Room room, JSONObject event, int player_id);

    void onCheatingDetected(Room room, int player_id, Throwable cause);

}
