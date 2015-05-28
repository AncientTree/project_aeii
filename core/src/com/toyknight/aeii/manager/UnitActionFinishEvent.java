package com.toyknight.aeii.manager;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.utils.UnitToolkit;

import java.io.Serializable;

/**
 * Created by toyknight on 5/28/2015.
 */
public class UnitActionFinishEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05272015L;

    private final int target_x;
    private final int target_y;

    public UnitActionFinishEvent(int target_x, int target_y) {
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public Point getFocus() {
        return new Point(-1, -1);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getUnit(target_x, target_y) != null;
    }

    @Override
    public void execute(GameManager manager) {
        Unit unit = manager.getGame().getMap().getUnit(target_x, target_y);
        if (UnitToolkit.canMoveAgain(unit)) {
            manager.beginRemovePhase();
        } else {
            manager.setState(GameManager.STATE_SELECT);
            unit.setStandby(true);
        }
    }

}
