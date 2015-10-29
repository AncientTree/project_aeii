package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 9/9/2015.
 */
public class UnitHealEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 9092015L;

    private final int healer_x;
    private final int healer_y;
    private final int target_x;
    private final int target_y;
    private final int heal;
    private final int experience;

    public UnitHealEvent() {
        this(-1, -1, -1, -1, -1, -1);
    }

    public UnitHealEvent(int healer_x, int healer_y, int target_x, int target_y, int heal, int experience) {
        this.healer_x = healer_x;
        this.healer_y = healer_y;
        this.target_x = target_x;
        this.target_y = target_y;
        this.heal = heal;
        this.experience = experience;
    }

    @Override
    public Point getFocus(GameCore game) {
        return game.getMap().getPosition(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        return game.getMap().getUnit(healer_x, healer_y) != null && game.getMap().getUnit(target_x, target_y) != null;
    }

    @Override
    public void execute(GameManager manager) {
        Unit healer = manager.getGame().getMap().getUnit(healer_x, healer_y);
        Unit target = manager.getGame().getMap().getUnit(target_x, target_y);
        target.changeCurrentHp(heal);
        manager.submitHpChangeAnimation(target, heal);
        if (target.getCurrentHp() <= 0) {
            manager.executeGameEvent(new UnitDestroyEvent(target_x, target_y), false);
        }
        boolean level_up = healer.gainExperience(experience);
        if (level_up) {
            manager.submitUnitLevelUpAnimation(healer);
        }
        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            manager.onUnitActionFinished(healer);
        }
    }

    @Override
    public GameEvent getCopy() {
        return new UnitHealEvent(healer_x, healer_y, target_x, target_y, heal, experience);
    }

}
