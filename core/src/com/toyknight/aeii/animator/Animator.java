package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by toyknight on 4/2/2015.
 */
public abstract class Animator {

    private float state_time;

    public Animator() {
        state_time = 0f;
    }

    public final void addStateTime(float time) {
        state_time += time;
    }

    public final float getStateTime() {
        return state_time;
    }

    abstract public void render(SpriteBatch batch, int x, int y);

    abstract public void update(float delta_time);

    abstract public boolean isAnimationFinished();

}
