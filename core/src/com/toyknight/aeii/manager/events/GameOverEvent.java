package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.animator.MessageAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.Language;

import java.io.Serializable;

/**
 * Created by toyknight on 8/23/2015.
 */
public class GameOverEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 8232015L;

    private final int alliance;

    public GameOverEvent(int alliance) {
        this.alliance = alliance;
    }

    @Override
    public Point getFocus() {
        return null;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameManager manager) {
        manager.submitAnimation(new MessageAnimator(Language.getText("LB_TEAM") + " " + alliance + " " + Language.getText("LB_WIN") + "!", 1.5f));
        GameHost.setGameOver(true);
    }

}
