package com.toyknight.aeii.listener;

import com.toyknight.aeii.manager.GameEvent;

/**
 * Created by toyknight on 4/18/2015.
 */
public interface EventDispatcherListener {

    public void onEventDispatched(GameEvent event);

}
