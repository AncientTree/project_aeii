package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 4/2/2015.
 */
public class Animator {

    protected final int ts;

    private float state_time = 0f;

    public Animator() {
        this.ts = AER.ts;
    }

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

}
