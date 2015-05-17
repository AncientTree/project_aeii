package com.toyknight.aeii.event;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.animator.UnitAttackAnimator;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;

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
    public void execute(GameCore game, AnimationDispatcher animation_dispatcher) {
        Unit attacker = game.getMap().getUnit(attacker_x, attacker_y);
        Unit target = game.getMap().getUnit(target_x, target_y);
        if (target == null) {
            animation_dispatcher.submitAnimation(new UnitAttackAnimator(attacker, target_x, target_y));
        } else {
            animation_dispatcher.submitAnimation(new UnitAttackAnimator(attacker, target, damage));
            game.changeUnitHp(target_x, target_y, -damage);
        }
        game.increaseUnitExperience(attacker_x, attacker_y, experience);
    }

}
