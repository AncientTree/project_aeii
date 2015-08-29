package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * Created by toyknight on 5/30/2015.
 */
public class UnitBuyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05272015L;

    private final String package_name;
    private final int index;
    private final int team;
    private final int x;
    private final int y;
    private final int price;

    public UnitBuyEvent(String package_name, int index, int team, int x, int y, int price) {
        this.package_name = package_name;
        this.index = index;
        this.team = team;
        this.x = x;
        this.y = y;
        this.price = price;
    }

    @Override
    public Point getFocus() {
        return new Point(x, y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameManager manager) {
        manager.getGame().createUnit(index, team, package_name, x, y);
        manager.getGame().getCurrentPlayer().changeGold(-price);
        manager.setSelectedUnit(manager.getGame().getMap().getUnit(x, y));

        if (manager.getGame().getCurrentPlayer().isLocalPlayer() || GameHost.isHost()) {
            manager.beginMovePhase();
        }
    }

}
