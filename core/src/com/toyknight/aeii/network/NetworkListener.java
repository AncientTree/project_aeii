package com.toyknight.aeii.network;

import org.json.JSONObject;

/**
 * @author toyknight 8/25/2015.
 */
public interface NetworkListener {

    void onDisconnect();

    void onPlayerJoin(int id, String username);

    void onPlayerLeave(int id, String username, int host);

    void onAllocationUpdate(int[] alliance, int[] allocation, int[] types);

    void onGameStart();

    void onReceiveGameEvent(JSONObject event);

    void onReceiveMessage(String username, String message);

}
