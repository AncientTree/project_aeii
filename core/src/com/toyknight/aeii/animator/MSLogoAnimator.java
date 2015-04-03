package com.toyknight.aeii.animator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.toyknight.aeii.TextureManager;

/**
 * Created by toyknight on 4/2/2015.
 */
public class MSLogoAnimator extends Animator {

    private final int WIDTH = 178;
    private final int HEIGHT = 178;

    private final Animation animation;
    private final ShapeRenderer shape_renderer;

    private float time_waited = 0f;

    public MSLogoAnimator() {
        animation = TextureManager.createAnimation(TextureManager.getMSLogoTexture(), 8, 5, 0.065f);
        shape_renderer = new ShapeRenderer();
        shape_renderer.setAutoShapeType(true);
    }

    @Override
    public void render(SpriteBatch batch) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        shape_renderer.rect(0, 0, Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());
        shape_renderer.end();

        batch.begin();
        TextureRegion current_frame = animation.getKeyFrame(getStateTime(), true);
        int draw_x = (Gdx.app.getGraphics().getWidth() - WIDTH) / 2;
        int draw_y = (Gdx.app.getGraphics().getHeight() - HEIGHT) / 2;
        batch.draw(current_frame, draw_x, draw_y);
        batch.end();
    }

    public void update(float delta_time) {
        if (time_waited < 1.7f) {
            time_waited += delta_time;
        } else {
            addStateTime(delta_time);
        }
    }

    public boolean isAnimationFinished() {
        return animation.isAnimationFinished(getStateTime());
    }

}
