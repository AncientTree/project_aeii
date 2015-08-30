package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * Created by toyknight on 4/26/2015.
 */
public class UnitStandbyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 04262015L;

    private final int unit_x;
    private final int unit_y;

    public UnitStandbyEvent(int unit_x, int unit_y) {
        this.unit_x = unit_x;
        this.unit_y = unit_y;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(unit_x, unit_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(unit_x, unit_y);
        return !target.isStandby() && target.getCurrentHp() > 0;
    }

    @Override
    public void execute(GameManager manager) {
        manager.getGame().standbyUnit(unit_x, unit_y);
        manager.setState(GameManager.STATE_SELECT);
    }
}
