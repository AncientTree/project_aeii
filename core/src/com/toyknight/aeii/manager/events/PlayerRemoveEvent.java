package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * Created by toyknight on 8/23/2015.
 */
public class PlayerRemoveEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 8232015L;

    private final int team;

    public PlayerRemoveEvent(int team) {
        this.team = team;
    }

    @Override
    public Point getFocus(GameCore game) {
        return null;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getPlayer(team) != null;
    }

    @Override
    public void execute(GameManager manager) {
        manager.getGame().removePlayer(team);
    }
}
