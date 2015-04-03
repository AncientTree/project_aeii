package com.toyknight.aeii.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.animator.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by toyknight on 4/2/2015.
 */
public class LogoScreen implements Screen {

    private final AEIIApplication context;

    private final Queue<Animator> logo_animator_queue = new LinkedList();

    private AELogoGlowAnimation ae_logo_glow_animator;
    private Animator current_animator;

    private SpriteBatch batch;

    public LogoScreen(AEIIApplication context) {
        this.context = context;
    }

    public void create() {
        batch = new SpriteBatch();

        ae_logo_glow_animator = new AELogoGlowAnimation();

        Animator ms_logo_animator = new MSLogoAnimator();
        logo_animator_queue.add(ms_logo_animator);

        Animator bg_fade_animator = new BackgroundFadeAnimator(1.0f, 1.0f, 1.0f);
        logo_animator_queue.add(bg_fade_animator);

        Animator ae_logo_animator = new AELogoAnimator();
        logo_animator_queue.add(ae_logo_animator);
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
            //context.gotoMainMenuScreen();
            ae_logo_glow_animator.render(batch);
            ae_logo_glow_animator.update(delta);
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
