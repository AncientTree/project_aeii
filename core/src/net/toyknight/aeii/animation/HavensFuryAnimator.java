package net.toyknight.aeii.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.entity.Unit;

import java.util.Random;

/**
 * @author toyknight 6/30/2016.
 */
public class HavensFuryAnimator extends UnitAnimator {

    private static final Random random = new Random(System.currentTimeMillis());

    private final TextureRegion[] smoke_frames;

    private final float[][] smoke_offset_x;
    private final float[][] smoke_offset_y;

    private final Animation spark_animation;

    private final float target_screen_x;
    private final float target_screen_y;

    private float spark_screen_y;

    private int shake_times = 0;

    private float shake_delay = 0f;

    public HavensFuryAnimator(Unit target) {
        smoke_frames = ResourceManager.createFrames(ResourceManager.getSmokeTexture(), 4, 1);
        smoke_offset_x = new float[4][4];
        smoke_offset_y = new float[4][4];
        for (int index = 0; index < 4; index++) {
            for (int frame = 0; frame < 4; frame++) {
                smoke_offset_x[index][frame] = 0f;
                smoke_offset_y[index][frame] = 0f;
            }
        }
        Texture texture_white_spark = ResourceManager.getWhiteSparkTexture();
        this.spark_animation = new Animation(1f / 15, ResourceManager.createFrames(texture_white_spark, 6, 1));
        target_screen_x = getCanvas().getXOnScreen(target.getX());
        target_screen_y = getCanvas().getYOnScreen(target.getY());
        spark_screen_y = Gdx.graphics.getHeight();
        addUnit(target, "target");
    }

    @Override
    public void render(Batch batch) {
        Unit target = getUnit("target");
        getCanvas().getUnitRenderer().drawUnitWithInformation(batch, target, target.getX(), target.getY());
        for (int frame = 0; frame < 4; frame++) {
            for (int index = 0; index < 4 - frame; index++) {
                batch.draw(smoke_frames[frame],
                        target_screen_x + smoke_offset_x[index][frame],
                        spark_screen_y + frame * ts / 2 + smoke_offset_y[index][frame],
                        ts * 13 / 24, ts * 10 / 24);
            }
        }
        if (spark_screen_y > target_screen_y) {
            batch.draw(spark_animation.getKeyFrame(getStateTime(), true), target_screen_x, spark_screen_y, ts(), ts());
        } else {
            batch.draw(spark_animation.getKeyFrames()[0], target_screen_x, spark_screen_y, ts(), ts());
        }
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
        if (spark_screen_y > target_screen_y) {
            spark_screen_y -= ts() * 6 / (1f / delta);
            spark_screen_y = spark_screen_y < target_screen_y ? target_screen_y : spark_screen_y;
            for (int index = 0; index < 4; index++) {
                for (int frame = 0; frame < 4; frame++) {
                    smoke_offset_x[index][frame] = random.nextInt(ts() / 2);
                    smoke_offset_y[index][frame] = random.nextInt(ts() / 2);
                }
            }
        } else {
            if (shake_delay < 1f / 20) {
                shake_delay += delta;
            } else {
                shake_delay = 0;
                shake_times++;
                if (shake_times < 7) {
                    int offset_x = random.nextInt(ts() / 3) - ts() / 6;
                    int offset_y = random.nextInt(ts() / 3) - ts() / 6;
                    getCanvas().setOffsetX(offset_x);
                    getCanvas().setOffsetY(offset_y);
                } else {
                    getCanvas().setOffsetX(0f);
                    getCanvas().setOffsetY(0f);
                }
            }
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return spark_screen_y <= target_screen_y && shake_times >= 7;
    }

}
