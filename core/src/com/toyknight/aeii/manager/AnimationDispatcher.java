package com.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.animation.*;
import com.toyknight.aeii.entity.Position;
import com.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 4/14/2015.
 */
public interface AnimationDispatcher {

    void setListener(AnimationListener listener);

    void submitHpChangeAnimation(ObjectMap<Position, Integer> change_map, ObjectSet<Unit> units);

    void submitHpChangeAnimation(Unit unit, int change);

    void submitMessageAnimation(String message, float delay);

    void submitMessageAnimation(String message_upper, String message_lower, float delay);

    void submitSummonAnimation(Unit summoner, int target_x, int target_y);

    void submitUnitLevelUpAnimation(Unit unit);

    void submitDustAriseAnimation(int map_x, int map_y);

    void submitUnitAttackAnimation(Unit attacker, Unit target, int damage);

    void submitUnitAttackAnimation(Unit attacker, int target_x, int target_y);

    void submitUnitDestroyAnimation(Unit unit);

    void submitUnitMoveAnimation(Unit unit, Array<Position> path);

    void submitAnimation(Animator animation);

    void reset();

    void updateAnimation(float delta);

    Animator getCurrentAnimation();

    boolean isAnimating();

}
