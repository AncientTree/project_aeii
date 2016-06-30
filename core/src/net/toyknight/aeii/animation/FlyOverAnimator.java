package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/29/2016.
 */
public class FlyOverAnimator extends UnitAnimator {

    private float offset = 0f;

    private final Unit flier;
    private final Unit target;

    public FlyOverAnimator(Unit flier, Unit target, int start_x, int start_y) {
        this.flier = UnitFactory.cloneUnit(flier);
        this.flier.setX(start_x);
        this.flier.setY(start_y);
        this.target = UnitFactory.cloneUnit(target);
        addLocation(target.getX(), target.getY());
    }

    @Override
    public void render(Batch batch) {
        getCanvas().getUnitRenderer().drawUnit(batch, target, target.getX(), target.getY());
        getCanvas().getUnitRenderer().drawUnit(batch, flier, flier.getX(), flier.getY(), getOffsetX(), getOffsetY());
    }

    private float getOffsetX() {
        int x = flier.getX();
        if (x < target.getX()) {
            return offset;
        }
        if (x > target.getX()) {
            return -offset;
        }
        return 0f;
    }

    private float getOffsetY() {
        int y = flier.getY();
        if (y < target.getY()) {
            return -offset;
        }
        if (y > target.getY()) {
            return offset;
        }
        return 0f;
    }

    @Override
    public void update(float delta) {
        offset += ts() * 5 / (1f / delta);
        if (offset > ts()) {
            offset = 0;
            int x = flier.getX();
            if (x < target.getX()) {
                flier.setX(x + 1);
            }
            if (x > target.getX()) {
                flier.setX(x - 1);
            }
            int y = flier.getY();
            if (y < target.getY()) {
                flier.setY(y + 1);
            }
            if (y > target.getY()) {
                flier.setY(y - 1);
            }
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return flier.isAt(target.getX(), target.getY());
    }

}
