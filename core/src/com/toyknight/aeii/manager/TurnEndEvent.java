package com.toyknight.aeii.manager;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.animator.MessageAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.utils.Language;

import java.io.Serializable;

/**
 * Created by toyknight on 5/26/2015.
 */
public class TurnEndEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05262015L;

    @Override
    public boolean canExecute(GameCore game) {
        return !game.isGameOver();
    }

    public Point getFocus() {
        return new Point(-1, -1);
    }

    @Override
    public void execute(GameManager manager) {
        manager.setState(GameManager.STATE_SELECT);
        GameCore game = manager.getGame();
        game.nextTurn();
        int income = game.gainIncome(game.getCurrentTeam());
        if (game.getCurrentPlayer() instanceof LocalPlayer) {
            manager.submitAnimation(new MessageAnimator(
                    Language.getText("LB_CURRENT_TURN") + ": " + game.getCurrentTurn(),
                    Language.getText("LB_INCOME") + ": " + income,
                    0.8f
            ));
        } else {
            manager.submitAnimation(new MessageAnimator(
                    Language.getText("LB_CURRENT_TURN") + ": " + game.getCurrentTurn(),
                    Language.getText("LB_INCOME") + ": ?",
                    0.8f
            ));
        }
    }

}
