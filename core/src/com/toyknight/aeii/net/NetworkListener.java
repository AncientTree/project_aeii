package com.toyknight.aeii.net;

import com.toyknight.aeii.manager.events.GameEvent;

/**
 * Created by toyknight on 8/25/2015.
 */
public interface NetworkListener {

    void onDisconnect();

    void onPlayerDisconnect(String username, boolean is_host);

    void onGameStart();

    void onReceiveGameEvent(GameEvent event);

}
