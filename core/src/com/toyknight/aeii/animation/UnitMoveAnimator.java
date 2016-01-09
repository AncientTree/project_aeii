package com.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 4/21/2015.
 */
public class UnitMoveAnimator extends UnitAnimator {

    private static final String MOVER_KEY = "mover";

    private final Array<Point> path;

    private int current_location;
    private float x_offset;
    private float y_offset;

    public UnitMoveAnimator(Unit unit, Array<Point> path) {
        this.addUnit(unit, MOVER_KEY);
        this.path = path;

        current_location = 0;
        x_offset = 0f;
        y_offset = 0f;
    }

    @Override
    public void render(Batch batch) {
        if (path.size > 0) {
            Point current = path.get(current_location);
            getCanvas().getUnitRenderer().drawUnitWithInformation(batch, getUnit(MOVER_KEY), current.x, current.y, x_offset, y_offset);
        }
    }

    @Override
    public void update(float delta) {
        if (current_location < path.size - 1) {
            float offset_delta = ts() / (4 * (1f / delta) / 30f);
            Point current = path.get(current_location);
            Point next = path.get(current_location + 1);
            if (current.x > next.x) {
                x_offset -= offset_delta;
            }
            if (current.x < next.x) {
                x_offset += offset_delta;
            }
            if (current.y > next.y) {
                y_offset += offset_delta;
            }
            if (current.y < next.y) {
                y_offset -= offset_delta;
            }
            if (Math.abs(x_offset) >= ts() || Math.abs(y_offset) >= ts()) {
                x_offset = 0;
                y_offset = 0;
                current_location++;
            }
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return current_location >= path.size - 1;
    }

}
