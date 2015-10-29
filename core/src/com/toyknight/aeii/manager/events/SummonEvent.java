package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.animator.SummonAnimator;
import com.toyknight.aeii.animator.UnitLevelUpAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;

/**
 * @author toyknight 5/21/2015.
 */
public class SummonEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5212015L;

    private final int summoner_x;
    private final int summoner_y;
    private final int target_x;
    private final int target_y;
    private final int experience;

    public SummonEvent() {
        this(-1, -1, -1, -1, -1);
    }

    public SummonEvent(int summoner_x, int summoner_y, int target_x, int target_y, int experience) {
        this.summoner_x = summoner_x;
        this.summoner_y = summoner_y;
        this.target_x = target_x;
        this.target_y = target_y;
        this.experience = experience;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().isTomb(target_x, target_y);
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Unit summoner = game.getMap().getUnit(summoner_x, summoner_y);
        game.getMap().removeTomb(target_x, target_y);
        game.createUnit(UnitFactory.getSkeletonIndex(), summoner.getTeam(), target_x, target_y);
        manager.submitAnimation(new SummonAnimator(summoner, target_x, target_y));
        boolean level_up = summoner.gainExperience(experience);
        if (level_up) {
            manager.submitAnimation(new UnitLevelUpAnimator(summoner));
        }

        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            manager.onUnitActionFinished(summoner);
        }
    }
}
