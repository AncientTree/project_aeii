package net.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.animation.Animator;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.screen.MapCanvas;

/**
 * @author toyknight 4/14/2015.
 */
public interface AnimationDispatcher {

    void setCanvas(MapCanvas canvas);

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

    void submitUnitAttackAnimation(Unit target, int damage);

    void submitUnitAttackAnimation(int target_x, int target_y);

    void submitUnitSparkAnimation(Unit unit);

    void submitUnitMoveAnimation(Unit unit, Array<Position> path);

    void submitReinforceAnimation(Array<Unit> reinforcements, int from_x, int from_y);

    void submitCrystalStealAnimation(int map_x, int map_y, int target_x, int target_y);

    void submitFlyOverAnimation(Unit flier, Unit target, int start_x, int start_y);

    void submitUnitCarryAnimation(Unit carrier, Unit target, int dest_x, int dest_y);

    void submitHavensFuryAnimation(Unit target);

    void submitAnimation(Animator animation);

    void reset();

    void updateAnimation(float delta);

    Animator getCurrentAnimation();

    boolean isAnimating();

}
