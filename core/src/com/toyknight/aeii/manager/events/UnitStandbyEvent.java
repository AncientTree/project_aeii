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

        HashMap<Point, Integer> hp_change_map = new HashMap<Point, Integer>();
        if (unit.hasAbility(Ability.HEALING_AURA)) {
            int heal = 15 + unit.getLevel() * 5;
            Set<Point> attackable_positions = manager.createAttackablePositions(unit);
            attackable_positions.add(game.getMap().getPosition(unit.getX(), unit.getY()));
            for (Point target_position : attackable_positions) {
                //there's a unit at the position
                Unit target = game.getMap().getUnit(target_position.x, target_position.y);
                if (game.canHeal(unit, target) && !target.isSkeleton()) {
                    hp_change_map.put(target_position, heal);
                }
            }
            manager.executeGameEvent(new HpChangeEvent(hp_change_map), false);
        }
    }

}
