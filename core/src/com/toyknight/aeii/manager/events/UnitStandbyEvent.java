package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 4/26/2015.
 */
public class UnitStandbyEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 4262015L;

    private final int unit_x;
    private final int unit_y;

    public UnitStandbyEvent(int unit_x, int unit_y) {
        this.unit_x = unit_x;
        this.unit_y = unit_y;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(unit_x, unit_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit target = game.getMap().getUnit(unit_x, unit_y);
        return !target.isStandby() && target.getCurrentHp() > 0;
    }

    @Override
    public void execute(GameManager manager) {
        Unit unit = manager.getGame().getMap().getUnit(unit_x, unit_y);
        manager.getGame().standbyUnit(unit_x, unit_y);
        attachAuraStatus(unit, manager.getGame());
        manager.setState(GameManager.STATE_SELECT);
    }

    private void attachAuraStatus(Unit unit, GameCore game) {
        if (unit.hasAbility(Ability.ATTACK_AURA)) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        Unit target = game.getMap().getUnit(unit.getX() + i, unit.getY() + j);
                        if (target != null && game.getAlliance(unit.getTeam()) == game.getAlliance(target.getTeam())) {
                            target.attachStatus(new Status(Status.INSPIRED, 0));
                        }
                    }
                }
            }
        }
    }

}
