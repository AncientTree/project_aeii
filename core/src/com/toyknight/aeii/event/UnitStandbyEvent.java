package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Unit;

/**
 * Created by toyknight on 4/26/2015.
 */
public class UnitStandbyEvent implements GameEvent {

    private final int unit_x;
    private final int unit_y;

    public UnitStandbyEvent(int unit_x, int unit_y) {
        this.unit_x = unit_x;
        this.unit_y = unit_y;
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(unit_x, unit_y);
        return !target.isStandby() && target.getCurrentHp() > 0;
    }

    @Override
    public void execute(GameCore game, AnimationDispatcher animation_dispatcher) {
        game.standbyUnit(unit_x, unit_y);
    }
}
