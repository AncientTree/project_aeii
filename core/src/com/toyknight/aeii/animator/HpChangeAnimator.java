package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.renderer.UnitRenderer;
import com.toyknight.aeii.screen.GameScreen;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by toyknight on 5/26/2015.
 */
public class HpChangeAnimator extends UnitAnimator {

    private final int[] y_offset = {2, 0, -1, -1, -2, -2, -2, -2, -1, -1, 0, 1, 2, 4, 6, 4, 3, 4, 6, 6, 6, 6};

    private final HashMap<Point, Integer> change_map;
    private final int dy;

    private int current_frame = 0;

    public HpChangeAnimator(HashMap<Point, Integer> change_map, Set<Unit> units) {
        int index = 0;
        for (Unit unit : units) {
            addUnit(unit, Integer.toString(index));
            index++;
        }
        this.change_map = change_map;
        this.dy = (ts - FontRenderer.getLCharHeight()) / 2;
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        for (int index = 0; index < getUnitCount(); index++) {
            Unit unit = getUnit(Integer.toString(index));
            int change = change_map.get(screen.getGame().getMap().getPosition(unit.getX(), unit.getY()));
            screen.getUnitRenderer().drawUnitWithInformation(batch, unit, unit.getX(), unit.getY());
            int sx = screen.getXOnScreen(unit.getX());
            int sy = screen.getYOnScreen(unit.getY());
            if (screen.isWithinPaintArea(sx, sy)) {
                int dx = (ts - FontRenderer.getLNumberWidth(Math.abs(change), true)) / 2;
                if (change > 0) {
                    FontRenderer.drawPositiveLNumber(batch, change, sx + dx, sy + dy - y_offset[current_frame] * ts / 24);
                }
                if (change < 0) {
                    FontRenderer.drawNegativeLNumber(batch, Math.abs(change), sx + dx, sy + dy - y_offset[current_frame] * ts / 24);
                }
                batch.flush();
            }
        }
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
        current_frame = (int) (getStateTime() / (1f / 30));
        if (current_frame > 21) {
            current_frame = 21;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 30 * 21;
    }

}
