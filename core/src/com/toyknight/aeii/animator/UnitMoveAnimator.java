package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

import java.util.ArrayList;

/**
 * Created by toyknight on 4/21/2015.
 */
public class UnitMoveAnimator extends UnitAnimator {

    private final int ts;
    private final Unit unit;
    private final GameScreen screen;
    private final ArrayList<Point> path;

    private int current_location;
    private float x_offset;
    private float y_offset;

    public UnitMoveAnimator(Unit unit, ArrayList<Point> path, GameScreen screen, int ts) {
        super(unit, unit.getX(), unit.getY());
        this.screen = screen;
        this.path = path;
        this.unit = unit;
        this.ts = ts;

        current_location = 0;
        x_offset = 0f;
        y_offset = 0f;
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        if (path.size() > 0) {
            Point current = path.get(current_location);
            screen.getUnitRenderer().drawUnitWithInformation(batch, unit, current.x, current.y, x_offset, y_offset);
        }
    }

    @Override
    public void update(float delta) {
        if (current_location < path.size() - 1) {
            float offset_delta = ts / (4 * (1f / delta) / 30f);
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
            if (Math.abs(x_offset) >= ts || Math.abs(y_offset) >= ts) {
                x_offset = 0;
                y_offset = 0;
                current_location++;
            }
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return current_location >= path.size() - 1;
    }

}
