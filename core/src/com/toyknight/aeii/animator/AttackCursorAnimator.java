package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.MapCanvas;

/**
 * Created by toyknight on 4/19/2015.
 */
public class AttackCursorAnimator extends Animator {

    private final int dx;
    private final int dy;
    private final int width;
    private final int height;
    private final MapCanvas canvas;
    private final Animation attack_cursor_animation;

    public AttackCursorAnimator(MapCanvas canvas, int ts) {
        this.width = ts * 40 / 24;
        this.height = ts * 41 / 24;
        this.dx = (ts - width) / 2;
        this.dy = (ts - height) / 2;
        this.canvas = canvas;
        Texture attack_cursor_texture = ResourceManager.getAttackCursorTexture();
        this.attack_cursor_animation = ResourceManager.createAnimation(attack_cursor_texture, 3, 1, 0.3f);
    }

    public void render(SpriteBatch batch, int map_x, int map_y) {
        int screen_x = canvas.getXOnScreen(map_x);
        int screen_y = canvas.getYOnScreen(map_y);
        TextureRegion current_frame = attack_cursor_animation.getKeyFrame(getStateTime(), true);
        batch.draw(current_frame, screen_x + dx, screen_y + dy, width, height);
        batch.flush();
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
