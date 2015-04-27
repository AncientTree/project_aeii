package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.entity.GameCore;

/**
 * Created by toyknight on 4/3/2015.
 */
public interface GameEvent {

    public boolean canExecute(GameCore game);

    public void execute(GameCore game, AnimationDispatcher animation_dispatcher);

}
