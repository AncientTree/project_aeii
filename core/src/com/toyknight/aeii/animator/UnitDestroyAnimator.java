package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.MapCanvas;

/**
 * @author toyknight 5/20/2015.
 */
public class UnitDestroyAnimator extends UnitAnimator {

    private final Animation spark_animation;

    public UnitDestroyAnimator(MapCanvas canvas, Unit unit) {
        super(canvas);
        this.addUnit(unit, "target");
        this.spark_animation = new Animation(1f / 15, ResourceManager.getWhiteSparkFrames());
    }

    @Override
    public void render(Batch batch) {
        Unit unit = getUnit("target");
        getCanvas().getUnitRenderer().drawUnit(batch, unit, unit.getX(), unit.getY());
        int screen_x = getCanvas().getXOnScreen(unit.getX());
        int screen_y = getCanvas().getYOnScreen(unit.getY());
        batch.draw(spark_animation.getKeyFrame(getStateTime()), screen_x, screen_y, ts(), ts());
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 15 * 6;
    }

}
