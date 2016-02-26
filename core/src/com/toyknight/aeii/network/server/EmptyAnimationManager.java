package com.toyknight.aeii.network.server;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.animation.Animator;
import com.toyknight.aeii.entity.Position;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.AnimationDispatcher;
import com.toyknight.aeii.manager.AnimationListener;

/**
 * @author toyknight 11/2/2015.
 */
public class EmptyAnimationManager implements AnimationDispatcher {

    @Override
    public void setListener(AnimationListener listener) {
    }

    @Override
    public void submitHpChangeAnimation(ObjectMap<Position, Integer> change_map, ObjectSet<Unit> units) {
    }

    @Override
    public void submitHpChangeAnimation(Unit unit, int change) {
    }

    @Override
    public void submitMessageAnimation(String message, float delay) {
    }

    @Override
    public void submitMessageAnimation(String message_upper, String message_lower, float delay) {
    }

    @Override
    public void submitSummonAnimation(Unit summoner, int target_x, int target_y) {
    }

    @Override
    public void submitUnitLevelUpAnimation(Unit unit) {
    }

    @Override
    public void submitDustAriseAnimation(int map_x, int map_y) {
    }

    @Override
    public void submitUnitAttackAnimation(Unit attacker, Unit target, int damage) {
    }

    @Override
    public void submitUnitAttackAnimation(Unit attacker, int target_x, int target_y) {
    }

    @Override
    public void submitUnitDestroyAnimation(Unit unit) {
    }

    @Override
    public void submitUnitMoveAnimation(Unit unit, Array<Position> path) {
    }

    @Override
    public void submitAnimation(Animator animation) {
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateAnimation(float delta) {
    }

    @Override
    public Animator getCurrentAnimation() {
        return null;
    }

    @Override
    public boolean isAnimating() {
        return false;
    }

}
