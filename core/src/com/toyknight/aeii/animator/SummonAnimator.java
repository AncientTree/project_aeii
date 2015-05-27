package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 5/21/2015.
 */
public class SummonAnimator extends UnitAnimator {

    private final int target_x;
    private final int target_y;

    private final Animation spark_animation;

    public SummonAnimator(Unit summoner, int target_x, int target_y) {
        this.target_x = target_x;
        this.target_y = target_y;
        this.addLocation(target_x, target_y);
        if (summoner != null) {
            this.addUnit(summoner, "summoner");
        }
        this.spark_animation = new Animation(1f / 15, ResourceManager.getWhiteSparkFrames());
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        int offset = ts / 24 * 2;
        Unit summoner = getUnit("summoner");
        int target_sx = screen.getXOnScreen(target_x);
        int target_sy = screen.getYOnScreen(target_y);
        screen.getUnitRenderer().drawUnitWithInformation(batch, summoner, summoner.getX(), summoner.getY());
        batch.draw(spark_animation.getKeyFrame(getStateTime(), true), target_sx - offset, target_sy - offset, ts, ts);
        batch.draw(spark_animation.getKeyFrame(getStateTime(), true), target_sx - offset, target_sy + offset, ts, ts);
        batch.draw(spark_animation.getKeyFrame(getStateTime(), true), target_sx + offset, target_sy - offset, ts, ts);
        batch.draw(spark_animation.getKeyFrame(getStateTime(), true), target_sx + offset, target_sy + offset, ts, ts);
        batch.draw(spark_animation.getKeyFrame(getStateTime(), true), target_sx, target_sy, ts, ts);
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 15 * 12;
    }

}
