package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 5/20/2015.
 */
public class UnitLevelUpAnimator extends UnitAnimator {

    private int current_frame = 0;

    public UnitLevelUpAnimator(Unit unit) {
        addUnit(unit, "target");
    }

    @Override
    public void render(Batch batch) {
        Unit unit = getUnit("target");
        int screen_x = getCanvas().getXOnScreen(unit.getX());
        int screen_y = getCanvas().getYOnScreen(unit.getY());
        if (current_frame <= 10) {
            batch.setShader(ResourceManager.getWhiteMaskShader((10 - current_frame) * 0.1f));
            batch.draw(
                    ResourceManager.getUnitTexture(unit.getTeam(), unit.getIndex(), unit.getLevel() - 1, 0),
                    screen_x, screen_y, ts(), ts());
        } else {
            batch.setShader(ResourceManager.getWhiteMaskShader((current_frame - 10) * 0.1f));
            batch.draw(
                    ResourceManager.getUnitTexture(unit.getTeam(), unit.getIndex(), unit.getLevel(), 0),
                    screen_x, screen_y, ts(), ts());
        }
        batch.setShader(null);
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
        current_frame = (int) (getStateTime() / (1f / 15));
        if (current_frame > 20) {
            current_frame = 20;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 15 * 20;
    }

}
