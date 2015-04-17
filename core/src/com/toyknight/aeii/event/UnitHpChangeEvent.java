package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 *
 * @author toyknight
 */
public class UnitHpChangeEvent implements GameEvent {

    private final GameCore game;
    private final Unit unit;
    private final int change;

    public UnitHpChangeEvent(GameCore game, Unit unit, int change) {
        this.game = game;
        this.unit = unit;
        this.change = change;
    }

    protected GameCore getGame() {
        return game;
    }

    @Override
    public boolean canExecute() {
        return unit.getCurrentHp() > 0 && validateHpChange(unit, change) != 0;
    }

    @Override
    public void execute(GameListener listener) {
        int actual_change = validateHpChange(unit, change);
        int changed_hp = unit.getCurrentHp() + actual_change;
        unit.setCurrentHp(changed_hp);
        listener.onUnitHpChange(unit, actual_change);
        if (unit.getCurrentHp() <= 0) {
            new UnitDestroyEvent(getGame(), unit).execute(listener);
        }
    }

    private int validateHpChange(Unit unit, int change) {
        if (unit.getCurrentHp() + change <= 0) {
            return -unit.getCurrentHp();
        }
        if (unit.getCurrentHp() + change >= unit.getMaxHp()) {
            return unit.getMaxHp() - unit.getCurrentHp();
        }
        return change;
    }

}
