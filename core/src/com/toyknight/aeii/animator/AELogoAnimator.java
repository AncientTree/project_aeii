package com.toyknight.aeii.animator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;

/**
 * Created by toyknight on 4/3/2015.
 */
public class AELogoAnimator extends Animator {

    private final int WIDTH = 240;
    private final int HEIGHT = 85;

    private final Animation animation;

    public AELogoAnimator() {
        animation = ResourceManager.createAnimation(ResourceManager.getAELogoTexture(), 8, 5, 0.065f);
    }


    @Override
    public void render(SpriteBatch batch, int x, int y) {
        batch.begin();
        TextureRegion current_frame = animation.getKeyFrame(getStateTime(), true);
        int draw_x = (Gdx.app.getGraphics().getWidth() - WIDTH) / 2;
        int draw_y = (Gdx.app.getGraphics().getHeight() - HEIGHT) / 2;
        batch.draw(current_frame, draw_x, draw_y);
        batch.end();
    }

    @Override
    public void update(float delta_time) {
        addStateTime(delta_time);
    }

    @Override
    public boolean isAnimationFinished() {
        return animation.isAnimationFinished(getStateTime());
    }

}
