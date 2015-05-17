package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.animator.DustAriseAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Tile;

import java.io.Serializable;

/**
 * Created by toyknight on 5/17/2015.
 */
public class TileDestroyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05172015L;

    private final int tile_x;
    private final int tile_y;

    public TileDestroyEvent(int tile_x, int tile_y) {
        this.tile_x = tile_x;
        this.tile_y = tile_y;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getTile(tile_x, tile_y).isDestroyable();
    }

    @Override
    public void execute(GameCore game, AnimationDispatcher animation_dispatcher) {
        Tile tile = game.getMap().getTile(tile_x, tile_y);
        game.setTile(tile.getDestroyedTileIndex(), tile_x, tile_y);
        animation_dispatcher.submitAnimation(new DustAriseAnimator(tile_x, tile_y));
    }

}
