package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;

/**
 * @author toyknight 5/30/2015.
 */
public class UnitBuyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5272015L;

    private final int index;
    private final int team;
    private final int x;
    private final int y;
    private final int price;

    public UnitBuyEvent() {
        this(-1, -1, -1, -1, -1);
    }

    public UnitBuyEvent(int index, int team, int x, int y, int price) {
        this.index = index;
        this.team = team;
        this.x = x;
        this.y = y;
        this.price = price;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(x, y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameManager manager) {
        if (index == UnitFactory.getCommanderIndex()) {
            manager.getGame().restoreCommander(team, x, y);
        } else {
            manager.getGame().createUnit(index, team, x, y);
        }
        manager.getGame().getCurrentPlayer().changeGold(-price);
        manager.setSelectedUnit(manager.getGame().getMap().getUnit(x, y));

        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            manager.beginMovePhase();
        }
    }

}
