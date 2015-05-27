package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by toyknight on 5/26/2015.
 */
public class UnitStatusUpdateEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05262015L;

    private final int team;

    public UnitStatusUpdateEvent(int team) {
        this.team = team;
    }

    @Override
    public Point getFocus() {
        return new Point(-1, -1);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameCore game, AnimationDispatcher animation_dispatcher) {
        HashSet<Point> unit_position_set = new HashSet(game.getMap().getUnitPositionSet());
        for (Point position : unit_position_set) {
            Unit unit = game.getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                unit.updateStatus();
            }
        }
    }

}
