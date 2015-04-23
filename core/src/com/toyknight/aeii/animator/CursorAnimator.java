package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/19/2015.
 */
public class CursorAnimator extends Animator {

    private final int dx;
    private final int dy;
    private final int size;
    private final GameScreen screen;
    private final Animation cursor_animation;

    public CursorAnimator(GameScreen screen, int ts) {
        this.size = ts / 24 * 26;
        this.dx = (ts - size) / 2;
        this.dy = (ts - size) / 2;
        this.screen = screen;
        Texture cursor_texture = ResourceManager.getCursorTexture();
        this.cursor_animation = ResourceManager.createAnimation(cursor_texture, 2, 1, 0.3f);
    }

    public void render(SpriteBatch batch, int map_x, int map_y) {
        batch.begin();
        int screen_x = screen.getXOnScreen(map_x);
        int screen_y = screen.getYOnScreen(map_y);
        TextureRegion current_frame = cursor_animation.getKeyFrame(getStateTime(), true);
        batch.draw(current_frame, screen_x + dx, screen_y + dy, size, size);
        batch.end();
    }

    @Override
    public void update(float delta_time) {
        addStateTime(delta_time);
    }

    @Override
    public boolean isAnimationFinished() {
        return false;
    }
}
