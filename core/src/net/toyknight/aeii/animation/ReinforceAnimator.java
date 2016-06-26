package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 6/24/2016.
 */
public class ReinforceAnimator extends UnitAnimator {

    private final int unit_count;

    public ReinforceAnimator(Array<Unit> reinforcements) {
        unit_count = reinforcements.size;
        for (int i = 0; i < reinforcements.size; i++) {
            Unit unit = reinforcements.get(i);
            addLocation(unit.getX(), unit.getY());
            addUnit(unit, "unit" + i);
        }
    }

    @Override
    public void render(Batch batch) {
        int n = (int) (getStateTime() / 0.5f) - 1;
        if (n >= 0) {
            for (int i = 0; i <= n; i++) {
                if (i < unit_count) {
                    Unit unit = getUnit("unit" + i);
                    getCanvas().getUnitRenderer().drawUnit(batch, unit, unit.getX(), unit.getY());
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
    }

    @Override
    public boolean isAnimationFinished() {
        return (int) (getStateTime() / 0.5f) - 1 > unit_count;
    }

}
