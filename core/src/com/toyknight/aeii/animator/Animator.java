package com.toyknight.aeii.animator;

/**
 * Created by toyknight on 4/2/2015.
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

    public boolean isAnimationFinished() {
        return false;
    }

    public static void setTileSize(int ts) {
        Animator.ts = ts;
    }

}
