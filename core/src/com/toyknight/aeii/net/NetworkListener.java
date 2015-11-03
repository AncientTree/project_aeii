package com.toyknight.aeii.net;

import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.serializable.GameSave;

/**
 * @author toyknight 8/25/2015.
 */
public interface NetworkListener {

    void onDisconnect();

    void onPlayerJoin(int id, String username);

    void onPlayerLeave(int id, String username);

    void onPlayerReconnect(int id, String username, Integer[] teams);

    void onAllocationUpdate(Integer[] allocation, Integer[] types);

    void onAllianceUpdate(Integer[] alliance);

    void onGameStart(GameSave game_save);

    void onReceiveGameEvent(GameEvent event);

    void onReceiveMessage(String username, String message);

}
