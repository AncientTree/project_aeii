package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 5/17/2015.
 */
public class DustAriseAnimator extends MapAnimator {

    private final int map_x;
    private final int map_y;

    private final Animation dust_animation;

    public DustAriseAnimator(int map_x, int map_y) {
        super(map_x, map_y);
        this.dust_animation = new Animation(1f / 15, ResourceManager.getDustFrames());
        this.map_x = map_x;
        this.map_y = map_y;
    }

    @Override
    public void render(Batch batch) {
        int dust_height = ts() * 20 / 24;
        int dust_sx = getCanvas().getXOnScreen(map_x);
        int dust_sy = getCanvas().getYOnScreen(map_y);
        int offset = (int) (getStateTime() / (1f / 15)) * ts() / 6;
        batch.draw(dust_animation.getKeyFrame(
                getStateTime()), dust_sx, dust_sy + ts() - dust_height + offset, ts(), dust_height);
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 15 * 3;
    }

}
