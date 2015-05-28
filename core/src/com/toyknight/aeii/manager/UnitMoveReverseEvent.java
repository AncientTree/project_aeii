package com.toyknight.aeii.manager;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;

import java.io.Serializable;

/**
 * Created by toyknight on 5/28/2015.
 */
public class UnitMoveReverseEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05272015L;

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
    public Point getFocus() {
        return new Point(origin_x, origin_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(unit_x, unit_y);
        return target != null && game.canUnitMove(target, origin_x, origin_y);
    }

    @Override
    public void execute(GameManager manager) {
        Unit unit = manager.getGame().getMap().getUnit(unit_x, unit_y);
        manager.getGame().moveUnit(unit_x, unit_y, origin_x, origin_y);
        unit.setCurrentMovementPoint(unit.getMovementPoint());
        manager.beginMovePhase();
    }

}
