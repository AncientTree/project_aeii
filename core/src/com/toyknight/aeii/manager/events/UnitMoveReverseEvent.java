package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 5/28/2015.
 */
public class UnitMoveReverseEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5272015L;

    private final int unit_x;
    private final int unit_y;
    private final int origin_x;
    private final int origin_y;

    public UnitMoveReverseEvent(int unit_x, int unit_y, int origin_x, int origin_y) {
        this.unit_x = unit_x;
        this.unit_y = unit_y;
        this.origin_x = origin_x;
        this.origin_y = origin_y;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(origin_x, origin_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(unit_x, unit_y);
        return target != null;
    }

    @Override
    public void execute(GameManager manager) {
        Unit unit = manager.getGame().getMap().getUnit(unit_x, unit_y);
        if (manager.getGame().getMap().canMove(origin_x, origin_y)) {
            manager.getGame().getMap().moveUnit(unit, origin_x, origin_y);
        }
        unit.setCurrentMovementPoint(unit.getMovementPoint());

        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            manager.beginMovePhase();
        }
    }

}
