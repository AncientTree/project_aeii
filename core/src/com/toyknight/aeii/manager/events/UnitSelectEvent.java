package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 5/28/2015.
 */
public class UnitSelectEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5272015L;

    private final int target_x;
    private final int target_y;

    public UnitSelectEvent() {
        this(-1, -1);
    }

    public UnitSelectEvent(int target_x, int target_y) {
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(target_x, target_y);
        return target != null && game.getCurrentTeam() == target.getTeam();
    }

    @Override
    public void execute(GameManager manager) {
        manager.setSelectedUnit(manager.getGame().getMap().getUnit(target_x, target_y));
        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            Unit selected_unit = manager.getSelectedUnit();
            Tile tile = manager.getGame().getMap().getTile(target_x, target_y);
            if (selected_unit.isCommander() && !selected_unit.isStandby() && manager.getGame().isCastleAccessible(tile)) {
                manager.setState(GameManager.STATE_BUY);
            } else {
                if (!selected_unit.isStandby()) {
                    manager.beginMovePhase();
                }
            }
        }
    }
}
