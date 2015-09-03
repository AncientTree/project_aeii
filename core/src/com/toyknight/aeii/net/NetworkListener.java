package com.toyknight.aeii.net;

import com.toyknight.aeii.manager.events.GameEvent;

/**
 * Created by toyknight on 8/25/2015.
 */
public interface NetworkListener {

    void onDisconnect();

    void onPlayerJoin(String service_name, String username);

    void onPlayerLeave(String service_name, String username);

    void onAllocationUpdate(String[] allocation, Integer[] types);

    void onAllianceUpdate(Integer[] alliance);

    void onGameStart();

    void onReceiveGameEvent(GameEvent event);

}
