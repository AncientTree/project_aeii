package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.TextureUtil;

import java.util.Random;

/**
 * @author toyknight 5/17/2015.
 */
public class UnitAttackAnimator extends UnitAnimator {

    private static final String ATTACKER_KEY = "attacker";
    private static final String TARGET_KEY = "target";

    private static final Random random = new Random(System.currentTimeMillis());

    private final int damage;
    private final int target_x;
    private final int target_y;

    private int target_dx = 0;
    private int target_dy = 0;
    private int current_step = 0;

    private final Animation attack_spark_animation;

    public UnitAttackAnimator(Unit attacker, Unit target, int damage) {
        Texture texture_attack_spark = AER.resources.getAttackSparkTexture();
        this.attack_spark_animation = new Animation(1f / 30, TextureUtil.createFrames(texture_attack_spark, 6, 1));
        this.addUnit(attacker, ATTACKER_KEY);
        this.addUnit(target, TARGET_KEY);
        this.target_x = target.getX();
        this.target_y = target.getY();
        this.damage = damage;
    }

    public UnitAttackAnimator(Unit attacker, int target_x, int target_y) {
        Texture texture_attack_spark = AER.resources.getAttackSparkTexture();
        this.attack_spark_animation = new Animation(1f / 30, TextureUtil.createFrames(texture_attack_spark, 6, 1));
        this.addUnit(attacker, ATTACKER_KEY);
        this.damage = -1;
        this.target_x = target_x;
        this.target_y = target_y;
    }

    public UnitAttackAnimator(Unit target, int damage) {
        Texture texture_attack_spark = AER.resources.getAttackSparkTexture();
        this.attack_spark_animation = new Animation(1f / 30, TextureUtil.createFrames(texture_attack_spark, 6, 1));
        this.addUnit(target, TARGET_KEY);
        this.damage = damage;
        this.target_x = target.getX();
        this.target_y = target.getY();
    }

    public UnitAttackAnimator(int target_x, int target_y) {
        Texture texture_attack_spark = AER.resources.getAttackSparkTexture();
        this.attack_spark_animation = new Animation(1f / 30, TextureUtil.createFrames(texture_attack_spark, 6, 1));
        this.damage = -1;
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public void render(Batch batch) {
        Unit attacker = getUnit(ATTACKER_KEY);
        Unit target = getUnit(TARGET_KEY);
        int target_sx = getCanvas().getXOnScreen(target_x);
        int target_sy = getCanvas().getYOnScreen(target_y);
        //paint tile
        int tile_index = getCanvas().getMap().getTileIndex(target_x, target_y);
        batch.draw(AER.resources.getTileTexture(tile_index), target_sx, target_sy, ts(), ts());
        //paint units
        if (attacker != null) {
            CanvasRenderer.drawUnitWithInformation(batch, attacker, attacker.getX(), attacker.getY());
        }
        if (target != null) {
            CanvasRenderer.drawUnitWithInformation(batch, target, target_x, target_y, target_dx, target_dy);
        }
        //paint spark
        int spark_size = ts() * 20 / 24;
        int spark_offset = (ts() - spark_size) / 2;
        batch.draw(
                attack_spark_animation.getKeyFrame(getStateTime(), true),
                getCanvas().getXOnScreen(target_x) + spark_offset,
                getCanvas().getYOnScreen(target_y) + spark_offset,
                spark_size, spark_size);
        //paint damage
        if (damage >= 0) {
            int damage_dx = (ts() - AER.font.getLNumberWidth(damage, true)) / 2;
            AER.font.drawNegativeLNumber(batch, damage, target_sx + damage_dx, target_sy);
        }
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
        int new_current_step = (int) (getStateTime() / (1f / 15));
        if (new_current_step != current_step) {
            current_step = new_current_step;
            target_dx = random.nextInt(ts() / 12) - ts() / 24;
            target_dy = random.nextInt(ts() / 12) - ts() / 24;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 30 * 12;
    }


}
