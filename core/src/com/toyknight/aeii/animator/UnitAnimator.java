package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by toyknight on 4/19/2015.
 */
public class UnitAnimator extends Animator {

    public boolean hasLocation(int x, int y) {
        return false;
    }

    @Override
    public void render(SpriteBatch batch, int x, int y) {

    }

    @Override
    public void update(float delta_time) {

    }

    @Override
    public boolean isAnimationFinished() {
        return false;
    }
}
