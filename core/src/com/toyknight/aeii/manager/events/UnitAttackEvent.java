package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.UnitToolkit;

import java.io.Serializable;

/**
 * @author toyknight 5/6/2015.
 */
public class UnitAttackEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5062015L;

    private final int attacker_x;
    private final int attacker_y;
    private final int target_x;
    private final int target_y;

    private final int experience;
    private final int damage;

    public UnitAttackEvent() {
        this(-1, -1, -1, -1, -1, -1);
    }

    public UnitAttackEvent(int attacker_x, int attacker_y, int target_x, int target_y, int damage, int experience) {
        this.attacker_x = attacker_x;
        this.attacker_y = attacker_y;
        this.target_x = target_x;
        this.target_y = target_y;

        this.experience = experience;
        this.damage = damage;
    }

    @Override
    public Point getFocus(GameCore game) {
        return new Point(target_x, target_y);
    }

    @Override
    public boolean canExecute(GameCore game) {
        Unit attacker = game.getMap().getUnit(attacker_x, attacker_y);
        Unit target = game.getMap().getUnit(target_x, target_y);
        return attacker != null
                && (target != null || attacker.hasAbility(Ability.DESTROYER)
                && game.getMap().getTile(target_x, target_y) != null
                && game.getMap().getTile(target_x, target_y).isDestroyable());
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Unit attacker = game.getMap().getUnit(attacker_x, attacker_y);
        Unit defender = game.getMap().getUnit(target_x, target_y);
        if (defender == null) {
            manager.submitUnitAttackAnimation(attacker, target_x, target_y);
        } else {
            defender.changeCurrentHp(-damage);
            UnitToolkit.attachAttackStatus(attacker, defender);
            manager.submitUnitAttackAnimation(attacker, defender, damage);
            if (defender.getCurrentHp() <= 0) {
                //update statistics
                game.getStatistics().addDestroy(attacker.getTeam(), defender.getPrice());
                //destroy defender
                game.destroyUnit(defender.getX(), defender.getY());
                //submit animation
                manager.submitUnitDestroyAnimation(defender);
                manager.submitDustAriseAnimation(defender.getX(), defender.getY());
                game.updateGameStatus();
            }
        }
        boolean level_up = attacker.gainExperience(experience);
        if (level_up) {
            manager.submitUnitLevelUpAnimation(attacker);
        }

        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            if (defender == null) {
                manager.onUnitActionFinished(attacker);
            } else {
                if (attacker.getTeam() == game.getCurrentTeam() &&
                        (!manager.getUnitToolkit().canCounter(defender, attacker) || defender.getCurrentHp() <= 0)) {
                    manager.onUnitActionFinished(attacker);
                }
                if (attacker.getTeam() != game.getCurrentTeam()) {
                    manager.onUnitActionFinished(defender);
                }
            }
        }
    }

    @Override
    public GameEvent getCopy() {
        return new UnitAttackEvent(attacker_x, attacker_y, target_x, target_y, damage, experience);
    }

}
