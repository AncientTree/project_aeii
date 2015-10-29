package com.toyknight.aeii.manager.events;

import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;
import java.util.HashSet;

/**
 * @author toyknight 5/26/2015.
 */
public class UnitStatusUpdateEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5262015L;

    private final int team;

    public UnitStatusUpdateEvent() {
        this(-1);
    }

    public UnitStatusUpdateEvent(int team) {
        this.team = team;
    }

    @Override
    public Point getFocus(GameCore game) {
        return null;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Array<Point> unit_position_set = game.getMap().getUnitPositionSet().toArray();
        for (Point position : unit_position_set) {
            Unit unit = game.getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                unit.updateStatus();
                game.resetUnit(unit);
            }
        }
    }

}
