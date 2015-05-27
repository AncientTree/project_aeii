package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 5/17/2015.
 */
public class DustAriseAnimator extends MapAnimator {

    private final int map_x;
    private final int map_y;

    private final Animation dust_animation;

    public DustAriseAnimator(int map_x, int map_y) {
        this.dust_animation = new Animation(1f / 15, ResourceManager.getDustFrames());
        this.map_x = map_x;
        this.map_y = map_y;
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        int ts = screen.getContext().getTileSize();
        int dust_height = ts / 24 * 20;
        int dust_sx = screen.getXOnScreen(map_x);
        int dust_sy = screen.getYOnScreen(map_y);
        int offset = (int) (getStateTime() / (1f / 15)) * ts / 6;
        batch.draw(dust_animation.getKeyFrame(
                getStateTime()), dust_sx, dust_sy + ts - dust_height + offset, ts, dust_height);
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
