package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.animator.MessageAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.GameManager;
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
    public Point getFocus() {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getTile(target_x, target_y).isCapturable();
    }

    @Override
    public void execute(GameManager manager) {
        Tile target_tile = manager.getGame().getMap().getTile(target_x, target_y);
        manager.getGame().setTile(target_tile.getCapturedTileIndex(team), target_x, target_y);
        manager.submitAnimation(new MessageAnimator(Language.getText("LB_OCCUPIED"), 0.5f));
        if (manager.getGame().getCurrentPlayer() instanceof LocalPlayer || GameHost.isHost()) {
            manager.onUnitActionFinished(manager.getGame().getMap().getUnit(target_x, target_y));
        }
    }

}
