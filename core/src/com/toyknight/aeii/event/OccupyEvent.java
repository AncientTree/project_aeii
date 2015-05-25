package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.animator.MessageAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.utils.Language;

import java.io.Serializable;

/**
 * Created by toyknight on 5/25/2015.
 */
public class OccupyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05252015L;

    private final int target_x;
    private final int target_y;
    private final int team;

    public OccupyEvent(int target_x, int target_y, int team) {
        this.target_x = target_x;
        this.target_y = target_y;
        this.team = team;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getTile(target_x, target_y).isCapturable();
    }

    @Override
    public void execute(GameCore game, AnimationDispatcher animation_dispatcher) {
        Tile target_tile = game.getMap().getTile(target_x, target_y);
        game.setTile(target_tile.getCapturedTileIndex(team), target_x, target_y);
        animation_dispatcher.submitAnimation(new MessageAnimator(Language.getText("LB_OCCUPIED"), 0.5f));
    }

}
