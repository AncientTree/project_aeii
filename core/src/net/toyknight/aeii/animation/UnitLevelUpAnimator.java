package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.renderer.UnitRenderer;

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
        } else {
            batch.setShader(ResourceManager.getWhiteMaskShader((current_frame - 10) * 0.1f));
        }
        UnitRenderer.drawUnit_(batch, unit, screen_x, screen_y, 0, ts());
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
