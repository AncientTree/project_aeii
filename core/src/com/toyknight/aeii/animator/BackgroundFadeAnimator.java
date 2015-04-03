package com.toyknight.aeii.animator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by toyknight on 4/3/2015.
 */
public class BackgroundFadeAnimator extends Animator {

    private final float start_r;
    private final float start_g;
    private final float start_b;

    private float r;
    private float g;
    private float b;
    private ShapeRenderer shape_renderer;

    public BackgroundFadeAnimator(float r, float g, float b) {
        this.start_r = r;
        this.start_g = g;
        this.start_b = b;
        this.r = r;
        this.g = g;
        this.b = b;
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
    }

    @Override
    public void render(SpriteBatch batch) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(r, g, b, 1.0f);
        shape_renderer.rect(0, 0, Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());
        shape_renderer.end();
    }

    @Override
    public void update(float delta_time) {
        if(r > 0f) {
            r -= start_r/(1.0f/delta_time/2.0f);
            g -= start_g/(1.0f/delta_time/2.0f);
            b -= start_b/(1.0f/delta_time/2.0f);
        } else {
            r = 0f;
            g = 0f;
            b = 0f;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return r == 0f;
    }
}
