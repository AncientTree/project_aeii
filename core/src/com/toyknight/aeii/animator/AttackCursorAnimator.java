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
public class AttackCursorAnimator extends Animator {

    private final int dx;
    private final int dy;
    private final int width;
    private final int height;
    private final GameScreen screen;
    private final Animation attack_cursor_animation;

    public AttackCursorAnimator(GameScreen screen, int ts) {
        this.width = ts / 24 * 40;
        this.height = ts / 24 * 41;
        this.dx = (ts - width) / 2;
        this.dy = (ts - height) / 2;
        this.screen = screen;
        Texture attack_cursor_texture = ResourceManager.getAttackCursorTexture();
        this.attack_cursor_animation = ResourceManager.createAnimation(attack_cursor_texture, 3, 1, 0.3f);
    }

    @Override
    public void render(SpriteBatch batch, int map_x, int map_y) {
        batch.begin();
        int screen_x = screen.getXOnScreen(map_x);
        int screen_y = screen.getYOnScreen(map_y);
        TextureRegion current_frame = attack_cursor_animation.getKeyFrame(getStateTime(), true);
        batch.draw(current_frame, screen_x + dx, screen_y + dy, width, height);
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
