package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

import java.util.Set;

/**
 *
 * @author toyknight
 */
public class BuffUpdateEvent implements GameEvent {

    private final GameCore game;
    private final Set<Point> unit_position_set;

    public BuffUpdateEvent(GameCore game, Set<Point> unit_position_set) {
        this.game = game;
        this.unit_position_set = unit_position_set;
    }

    @Override
    public boolean canExecute() {
        return !unit_position_set.isEmpty();
    }

    @Override
    public void execute(GameListener listener) {
        for (Point position : unit_position_set) {
            Unit unit = game.getMap().getUnit(position.x, position.y);
            if(unit != null) {
                unit.updateBuff();
            }
        }
    }

}
