package com.toyknight.aeii.event;

import com.toyknight.aeii.listener.GameListener;

/**
 * Created by toyknight on 4/3/2015.
 */
public interface GameEvent {

    public boolean canExecute();

    public void execute(GameListener listener);

}
