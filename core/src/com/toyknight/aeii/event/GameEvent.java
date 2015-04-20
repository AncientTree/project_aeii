package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.entity.GameCore;

/**
 * Created by toyknight on 4/3/2015.
 */
public abstract class GameEvent {

    private final GameCore game;
    private final AnimationDispatcher animation_dispatcher;

    public GameEvent(GameCore game, AnimationDispatcher dispatcher) {
        this.game = game;
        this.animation_dispatcher = dispatcher;
    }

    protected GameCore getGame() {
        return game;
    }

    protected AnimationDispatcher getAnimationDispatcher() {
        return animation_dispatcher;
    }

    abstract public boolean canExecute();

    abstract public void execute();

}
