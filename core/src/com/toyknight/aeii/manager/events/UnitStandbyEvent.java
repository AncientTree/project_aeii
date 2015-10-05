package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

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
        processAuraEffects(unit, manager);
        manager.setState(GameManager.STATE_SELECT);
    }

    private void processAuraEffects(Unit unit, GameManager manager) {
        GameCore game = manager.getGame();
        //all the buff auras
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Unit target = game.getMap().getUnit(unit.getX() + i, unit.getY() + j);
                if (target != null && !target.hasAbility(Ability.HEAVY_MACHINE)) {
                    if (unit.hasAbility(Ability.ATTACK_AURA) && !game.isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.INSPIRED, 0));
                    }
                    if (unit.hasAbility(Ability.SLOWING_MASTER) && game.isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.SLOWED, 1));
                    }
                }
            }
        }
        //the refresh aura
        HashMap<Point, Integer> hp_change_map = new HashMap<Point, Integer>();
        if (unit.hasAbility(Ability.REFRESH_AURA)) {
            int heal = 10 + unit.getLevel() * 5;
            Set<Point> attackable_positions = manager.createAttackablePositions(unit);
            attackable_positions.add(game.getMap().getPosition(unit.getX(), unit.getY()));
            for (Point target_position : attackable_positions) {
                Unit target = game.getMap().getUnit(target_position.x, target_position.y);
                if (target != null && !game.isEnemy(unit, target) && target.hasDebuff()) {
                    target.clearStatus();
                }
                if (game.canHeal(unit, target)) {
                    hp_change_map.put(target_position, heal);
                }

            }
        }
        //deal with tombs
        if (game.getMap().isTomb(unit_x, unit_y)) {
            game.getMap().removeTomb(unit_x, unit_y);
            if (!unit.hasAbility(Ability.HEAVY_MACHINE) && !unit.hasAbility(Ability.NECROMANCER)) {
                unit.attachStatus(new Status(Status.POISONED, 3));
            }
        }
        //
        if (unit.getCurrentHp() > unit.getMaxHp()) {
            Point position = game.getMap().getPosition(unit_x, unit_y);
            hp_change_map.put(position, unit.getMaxHp() - unit.getCurrentHp());
        }
        manager.executeGameEvent(new HpChangeEvent(hp_change_map), false);
    }

}
