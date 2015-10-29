package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.animator.DustAriseAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 5/17/2015.
 */
public class TileDestroyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5172015L;

    private final int target_x;
    private final int target_y;

    public TileDestroyEvent() {
        this(-1, -1);
    }

    public TileDestroyEvent(int target_x, int target_y) {
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getTile(target_x, target_y).isDestroyable();
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Tile tile = game.getMap().getTile(target_x, target_y);
        game.setTile(tile.getDestroyedTileIndex(), target_x, target_y);
        manager.submitAnimation(new DustAriseAnimator(target_x, target_y));
    }

}
