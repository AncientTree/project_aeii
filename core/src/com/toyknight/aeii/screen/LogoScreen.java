package com.toyknight.aeii.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.animator.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 4/2/2015.
 */
public class LogoScreen implements Screen {

    private final GameContext context;
    private final SpriteBatch batch;
    private final Queue<Animator> logo_animator_queue;

    private Animator current_animator;

    public LogoScreen(GameContext context) {
        this.context = context;
        this.batch = new SpriteBatch();
        this.logo_animator_queue = new LinkedList<Animator>();

        Animator ms_logo_animator = new MSLogoAnimator();
        logo_animator_queue.add(ms_logo_animator);

        Animator bg_fade_animator = new BackgroundFadeAnimator(1.0f, 1.0f, 1.0f);
        logo_animator_queue.add(bg_fade_animator);
    }

    @Override
    public void show() {
        current_animator = logo_animator_queue.poll();
    }

    @Override
    public void render(float delta) {
        if (current_animator != null) {
            current_animator.render(batch);
            current_animator.update(delta);
            if(current_animator.isAnimationFinished()) {
                current_animator = logo_animator_queue.poll();
            }
        } else {
            context.gotoMainMenuScreen();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
