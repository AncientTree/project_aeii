package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 5/17/2015.
 */
public class UnitAttackAnimator extends UnitAnimator {

    private static final String ATTACKER_KEY = "attacker";
    private static final String TARGET_KEY = "target";

    private final int damage;
    private final int target_x;
    private final int target_y;

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
        screen.getUnitRenderer().drawUnitWithInformation(batch, attacker, attacker.getX(), attacker.getY());
        if (target != null) {
            //draw target
        }
        batch.begin();
        int ts = screen.getContext().getTileSize();
        int spark_size = ts / 24 * 20;
        int spark_offset = (ts - spark_size) / 2;
        batch.draw(
                attack_spark_animation.getKeyFrame(getStateTime(), true),
                screen.getXOnScreen(target_x) + spark_offset, screen.getYOnScreen(target_y) + spark_offset, spark_size, spark_size);
        if (damage >= 0) {
            //draw damage
        }
        batch.end();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 30 * 12;
    }


}
