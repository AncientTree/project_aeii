package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/29/2016.
 */
public class UnitCarryAnimator extends UnitAnimator {

    private final Unit carrier;
    private final Unit target;

    private final int dest_x;
    private final int dest_y;

    private float offset = 0f;

    public UnitCarryAnimator(Unit carrier, Unit target, int dest_x, int dest_y) {
        this.carrier = UnitFactory.cloneUnit(carrier);
        this.target = UnitFactory.cloneUnit(target);
        this.dest_x = dest_x;
        this.dest_y = dest_y;
    }

    @Override
    public void render(Batch batch) {
        getCanvas().getUnitRenderer().drawUnit(
                batch, target, carrier.getX(), carrier.getY(),
                getCarrierOffsetX() + getTargetOffsetX(), getCarrierOffsetY() + getTargetOffsetY());
        getCanvas().getUnitRenderer().drawUnit(
                batch, carrier, carrier.getX(), carrier.getY(), getCarrierOffsetX(), getCarrierOffsetY());
    }

    private float getCarrierOffsetX() {
        int x = carrier.getX();
        if (x < dest_x) {
            return offset;
        }
        if (x > dest_x) {
            return -offset;
        }
        return 0f;
    }

    private float getTargetOffsetX() {
        int x = carrier.getX();
        if (x < dest_x) {
            return -ts() / 2;
        }
        if (x > dest_x) {
            return ts() / 2;
        }
        return 0f;
    }

    private float getCarrierOffsetY() {
        int y = carrier.getY();
        if (y < dest_y) {
            return -offset;
        }
        if (y > dest_y) {
            return offset;
        }
        return 0f;
    }

    private float getTargetOffsetY() {
        int y = carrier.getY();
        if (y < dest_y) {
            return ts() / 2;
        }
        if (y > dest_y) {
            return -ts() / 2;
        }
        return 0f;
    }

    @Override
    public void update(float delta) {
        offset += ts() * 5 / (1f / delta);
        if (offset > ts()) {
            offset = 0;
            int x = carrier.getX();
            if (x < dest_x) {
                carrier.setX(x + 1);
            }
            if (x > dest_x) {
                carrier.setX(x - 1);
            }
            int y = carrier.getY();
            if (y < dest_y) {
                carrier.setY(y + 1);
            }
            if (y > dest_y) {
                carrier.setY(y - 1);
            }
            getCanvas().focus(carrier.getX(), carrier.getY(), true);
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return carrier.isAt(dest_x, dest_y);
    }

}
