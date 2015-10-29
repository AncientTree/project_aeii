package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.Language;

import java.io.Serializable;

/**
 * @author toyknight 5/26/2015.
 */
public class TurnEndEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5262015L;

    @Override
    public boolean canExecute(GameCore game) {
        return !game.isGameOver();
    }

    public Point getFocus(GameCore game) {
        int team = game.getCurrentTeam();
        return game.getTeamFocus(team);
    }

    @Override
    public void execute(GameManager manager) {
        manager.setState(GameManager.STATE_SELECT);
        GameCore game = manager.getGame();
        game.nextTurn();
        int income = game.gainIncome(game.getCurrentTeam());
        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            manager.submitMessageAnimation(
                    Language.getText("LB_CURRENT_TURN") + ": " + game.getCurrentTurn(),
                    Language.getText("LB_INCOME") + ": " + income,
                    0.8f
            );
        } else {
            manager.submitMessageAnimation(
                    Language.getText("LB_CURRENT_TURN") + ": " + game.getCurrentTurn(),
                    Language.getText("LB_INCOME") + ": ?",
                    0.8f
            );
        }
    }

    @Override
    public GameEvent copy() {
        return new TurnEndEvent();
    }

}
