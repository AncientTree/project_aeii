package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 5/20/2015.
 */
public class UnitDestroyAnimator extends UnitAnimator {

    private final Animation spark_animation;

    public UnitDestroyAnimator(Unit unit) {
        this.addUnit(unit, "target");
        this.spark_animation = new Animation(1f / 15, ResourceManager.getWhiteSparkFrames());
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        Unit unit = getUnit("target");
        screen.getUnitRenderer().drawUnit(batch, unit, unit.getX(), unit.getY());
        batch.begin();
        int screen_x = screen.getXOnScreen(unit.getX());
        int screen_y = screen.getYOnScreen(unit.getY());
        batch.draw(spark_animation.getKeyFrame(getStateTime()), screen_x, screen_y, ts, ts);
        batch.end();
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
