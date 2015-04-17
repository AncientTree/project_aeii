package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.SkirmishGame;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 * @author toyknight
 */
public class UnitMoveEvent implements GameEvent {

    private final GameCore game;
    private final Unit unit;
    private final int dest_x;
    private final int dest_y;

    public UnitMoveEvent(GameCore game, Unit unit, int dest_x, int dest_y) {
        this.game = game;
        this.unit = unit;
        this.dest_x = dest_x;
        this.dest_y = dest_y;
    }

    protected GameCore getGame() {
        return game;
    }

    @Override
    public boolean canExecute() {
        return getGame().getMap().canMove(dest_x, dest_y);
    }

    @Override
    public void execute(GameListener listener) {
        int start_x = unit.getX();
        int start_y = unit.getY();
        getGame().getMap().moveUnit(unit, dest_x, dest_y);
        listener.onUnitMove(unit, start_x, start_y, dest_x, dest_y);
        if (getGame() instanceof SkirmishGame) {
            ((SkirmishGame) getGame()).onUnitMoved(unit, dest_x, dest_y);
        }
    }

}
