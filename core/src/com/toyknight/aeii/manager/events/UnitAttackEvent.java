package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.animator.DustAriseAnimator;
import com.toyknight.aeii.animator.UnitAttackAnimator;
import com.toyknight.aeii.animator.UnitDestroyAnimator;
import com.toyknight.aeii.animator.UnitLevelUpAnimator;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.UnitToolkit;

import java.io.Serializable;

/**
 * Created by toyknight on 5/6/2015.
 */
public class UnitAttackEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 05062015L;

    private final int attacker_x;
    private final int attacker_y;
    private final int target_x;
    private final int target_y;

    private final int experience;
    private final int damage;


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
        if (attacker == null) {
            return false;
        } else {
            if (target == null) {
                return attacker.hasAbility(Ability.DESTROYER) && game.getMap().getTile(target_x, target_y).isDestroyable();
            } else {
                return true;
            }
        }
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Unit attacker = game.getMap().getUnit(attacker_x, attacker_y);
        Unit defender = game.getMap().getUnit(target_x, target_y);
        if (defender == null) {
            manager.submitAnimation(new UnitAttackAnimator(attacker, target_x, target_y));
        } else {
            defender.changeCurrentHp(-damage);
            UnitToolkit.attachAttackStatus(attacker, defender);
            manager.submitAnimation(new UnitAttackAnimator(attacker, defender, damage));
            if (defender.getCurrentHp() <= 0) {
                //update statistics
                game.getStatistics().addDestroy(attacker.getTeam(), game.getUnitPrice(defender.getIndex(), defender.getPrice()));
                //destroy defender
                game.destroyUnit(defender.getX(), defender.getY());
                //submit animation
                manager.submitAnimation(new UnitDestroyAnimator(defender));
                manager.submitAnimation(new DustAriseAnimator(defender.getX(), defender.getY()));
                GameHost.updateGameStatus();
            }
        }
        boolean level_up = attacker.gainExperience(experience);
        if (level_up) {
            manager.submitAnimation(new UnitLevelUpAnimator(attacker));
        }

        if (manager.getGame().getCurrentPlayer().isLocalPlayer()) {
            if (defender == null) {
                manager.onUnitActionFinished(attacker);
            } else {
                if (attacker.getTeam() == game.getCurrentTeam() &&
                        (!UnitToolkit.canCounter(defender, attacker) || defender.getCurrentHp() <= 0)) {
                    manager.onUnitActionFinished(attacker);
                }
                if (attacker.getTeam() != game.getCurrentTeam()) {
                    manager.onUnitActionFinished(defender);
                }
            }
        }
    }

}
