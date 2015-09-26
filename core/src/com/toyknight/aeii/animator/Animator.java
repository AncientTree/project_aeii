package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * @author toyknight 4/2/2015.
 */
public class Animator {

    protected static int ts;

    private float state_time = 0f;

    public final void addStateTime(float time) {
        state_time += time;
    }

    public final float getStateTime() {
        return state_time;
    }

    public void update(float delta_time) {
    }

    public void render(Batch batch) {
    }

    public boolean isAnimationFinished() {
        return false;
    }

    public static void setTileSize(int ts) {
        Animator.ts = ts;
    }

}
