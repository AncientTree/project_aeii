package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by toyknight on 4/21/2015.
 */
public class UnitMoveEvent implements GameEvent, Serializable {

    private final int unit_x;
    private final int unit_y;
    private final int dest_x;
    private final int dest_y;
    private final ArrayList<Point> move_path;

    public UnitMoveEvent(int unit_x, int unit_y, int dest_x, int dest_y, ArrayList<Point> move_path) {
        this.unit_x = unit_x;
        this.unit_y = unit_y;
        this.dest_x = dest_x;
        this.dest_y = dest_y;
        this.move_path = move_path;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getUnit(unit_x, unit_y) != null && game.getMap().canMove(dest_x, dest_y);
    }

    @Override
    public void execute(GameCore game, AnimationDispatcher dispatcher) {
        game.moveUnit(unit_x, unit_y, dest_x, dest_y);
        
    }
}
