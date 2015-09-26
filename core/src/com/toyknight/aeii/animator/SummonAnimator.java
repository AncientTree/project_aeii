package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 5/21/2015.
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
    public void render(Batch batch) {
        int offset = ts * 2 / 24;
        Unit summoner = getUnit("summoner");
        int target_sx = getCanvas().getXOnScreen(target_x);
        int target_sy = getCanvas().getYOnScreen(target_y);
        getCanvas().getUnitRenderer().drawUnitWithInformation(batch, summoner, summoner.getX(), summoner.getY());
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
