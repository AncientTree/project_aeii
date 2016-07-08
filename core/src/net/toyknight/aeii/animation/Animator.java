package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 4/2/2015.
 */
public class Animator {

    protected final int ts;

    private final GameContext context;

    private float state_time = 0f;

    public Animator(GameContext context) {
        this.context = context;
        this.ts = context == null ? 48 : context.getTileSize();
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

    public GameContext getContext() {
        return context;
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

}
