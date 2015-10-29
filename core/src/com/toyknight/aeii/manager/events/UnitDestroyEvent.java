package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 5/26/2015.
 */
public class UnitDestroyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5262015L;

    private final int target_x;
    private final int target_y;

    public UnitDestroyEvent() {
        this(-1, -1);
    }

    public UnitDestroyEvent(int target_x, int target_y) {
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getUnit(target_x, target_y) != null;
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Unit unit = game.getMap().getUnit(target_x, target_y);

        game.destroyUnit(unit.getX(), unit.getY());
        manager.submitUnitDestroyAnimation(unit);
        manager.submitDustAriseAnimation(unit.getX(), unit.getY());

        game.updateGameStatus();
    }

    @Override
    public GameEvent getCopy() {
        return new UnitDestroyEvent(target_x, target_y);
    }

}
