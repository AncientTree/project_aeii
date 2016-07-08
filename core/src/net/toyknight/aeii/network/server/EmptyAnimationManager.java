package net.toyknight.aeii.network.server;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.animation.Animator;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.manager.AnimationDispatcher;
import net.toyknight.aeii.manager.AnimationListener;
import net.toyknight.aeii.screen.MapCanvas;

/**
 * @author toyknight 11/2/2015.
 */
public class EmptyAnimationManager implements AnimationDispatcher {

    @Override
    public void setCanvas(MapCanvas canvas) {
    }

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
    public void submitUnitAttackAnimation(Unit target, int damage) {
    }

    @Override
    public void submitUnitAttackAnimation(int target_x, int target_y) {
    }

    @Override
    public void submitUnitSparkAnimation(Unit unit) {
    }

    @Override
    public void submitUnitMoveAnimation(Unit unit, Array<Position> path) {
    }

    @Override
    public void submitReinforceAnimation(Array<Unit> reinforcements, int from_x, int from_y) {
    }

    @Override
    public void submitCrystalStealAnimation(int map_x, int map_y, int target_x, int target_y) {
    }

    @Override
    public void submitFlyOverAnimation(Unit flier, Unit target, int start_x, int start_y) {
    }

    @Override
    public void submitUnitCarryAnimation(Unit carrier, Unit target, int dest_x, int dest_y) {
    }

    @Override
    public void submitHavensFuryAnimation(Unit target) {
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
