package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.screen.GameScreen;

import java.util.Random;

/**
 * Created by toyknight on 5/17/2015.
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
        this.attack_spark_animation = new Animation(1f / 30, ResourceManager.getAttackSparkFrames());
        this.addUnit(attacker, ATTACKER_KEY);
        this.addUnit(target, TARGET_KEY);
        this.target_x = target.getX();
        this.target_y = target.getY();
        this.damage = damage;
    }

    public UnitAttackAnimator(Unit attacker, int target_x, int target_y) {
        this.attack_spark_animation = new Animation(1f / 30, ResourceManager.getAttackSparkFrames());
        this.addUnit(attacker, ATTACKER_KEY);
        this.damage = -1;
        this.target_x = target_x;
        this.target_y = target_y;
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        Unit attacker = getUnit(ATTACKER_KEY);
        Unit target = getUnit(TARGET_KEY);
        int target_sx = screen.getXOnScreen(target_x);
        int target_sy = screen.getYOnScreen(target_y);
        //paint tile
        int tile_index = screen.getGame().getMap().getTileIndex(target_x, target_y);
        batch.draw(ResourceManager.getTileTexture(tile_index), target_sx, target_sy, ts, ts);
        //paint units
        screen.getUnitRenderer().drawUnitWithInformation(batch, attacker, attacker.getX(), attacker.getY());
        if (target != null) {
            screen.getUnitRenderer().drawUnitWithInformation(batch, target, target_x, target_y, target_dx, target_dy);
        }
        //paint spark
        int spark_size = ts * 20 / 24;
        int spark_offset = (ts - spark_size) / 2;
        batch.draw(
                attack_spark_animation.getKeyFrame(getStateTime(), true),
                screen.getXOnScreen(target_x) + spark_offset, screen.getYOnScreen(target_y) + spark_offset, spark_size, spark_size);
        //paint damage
        if (damage >= 0) {
            int damage_dx = (ts - FontRenderer.getLNumberWidth(damage, true)) / 2;
            FontRenderer.drawNegativeLNumber(batch, damage, target_sx + damage_dx, target_sy);
        }
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
        int new_current_step = (int) (getStateTime() / (1f / 15));
        if (new_current_step != current_step) {
            current_step = new_current_step;
            target_dx = random.nextInt(ts / 12) - ts / 24;
            target_dy = random.nextInt(ts / 12) - ts / 24;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 30 * 12;
    }


}
