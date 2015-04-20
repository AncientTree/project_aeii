package com.toyknight.aeii;

import com.toyknight.aeii.event.GameEvent;
import com.toyknight.aeii.listener.GameEventListener;

/**
 * Created by toyknight on 4/18/2015.
 */
public interface GameEventDispatcher {

    public void addGameEventListener(GameEventListener listener);

    public void submitGameEvent(GameEvent event);

    public boolean hasNextEvent();

    public void dispatchEvent();

}
