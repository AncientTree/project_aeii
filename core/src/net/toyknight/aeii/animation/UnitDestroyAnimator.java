package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 5/20/2015.
 */
public class UnitDestroyAnimator extends UnitAnimator {

    private final Animation spark_animation;

    public UnitDestroyAnimator(Unit unit) {
        this.addUnit(unit, "target");
        Texture texture_white_spark = ResourceManager.getWhiteSparkTexture();
        this.spark_animation = new Animation(1f / 15, ResourceManager.createFrames(texture_white_spark, 6, 1));
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
