package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * Created by toyknight on 5/28/2015.
 */
public class UnitSelectEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05272015L;

    private final int target_x;
    private final int target_y;

    public UnitSelectEvent(int target_x, int target_y) {
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public Point getFocus() {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(target_x, target_y);
        if (target == null) {
            return false;
        } else {
            return game.getCurrentTeam() == target.getTeam();
        }
    }

    @Override
    public void execute(GameManager manager) {
        manager.setSelectedUnit(manager.getGame().getMap().getUnit(target_x, target_y));
        manager.beginMovePhase();
    }
}
