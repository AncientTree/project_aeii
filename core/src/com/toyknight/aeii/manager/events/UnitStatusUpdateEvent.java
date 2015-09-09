package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by toyknight on 5/26/2015.
 */
public class UnitStatusUpdateEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05262015L;

    private final int team;

    public UnitStatusUpdateEvent(int team) {
        this.team = team;
    }

    @Override
    public Point getFocus(GameCore game) {
        return null;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        HashSet<Point> unit_position_set = new HashSet<Point>(game.getMap().getUnitPositionSet());
        for (Point position : unit_position_set) {
            Unit unit = game.getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                unit.updateStatus();
                attachAuraStatus(unit, manager.getGame());
                unit.setCurrentMovementPoint(unit.getMovementPoint());
            }
        }
    }

    private void attachAuraStatus(Unit unit, GameCore game) {
        if (unit.hasAbility(Ability.ATTACK_AURA)) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        Unit target = game.getMap().getUnit(unit.getX() + i, unit.getY() + j);
                        if (target != null && game.getAlliance(unit.getTeam()) == game.getAlliance(target.getTeam())) {
                            target.attachStatus(new Status(Status.INSPIRED, 1));
                        }
                    }
                }
            }
        }
    }

}
