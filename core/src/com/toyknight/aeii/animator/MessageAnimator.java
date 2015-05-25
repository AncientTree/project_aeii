package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 5/25/2015.
 */
public class MessageAnimator extends MapAnimator {

    private final float delay;
    private final String message_upper;
    private final String message_lower;

    private float alpha = 1.0f;

    public MessageAnimator(String message, float delay) {
        this(message, null, delay);
    }

    public MessageAnimator(String message_upper, String message_lower, float delay) {
        this.delay = delay;
        this.message_upper = message_upper;
        this.message_lower = message_lower;
    }

    @Override
    public void render(SpriteBatch batch, GameScreen screen) {
        int width = screen.getViewportWidth();
        int height = message_lower == null ? ts : 2 * ts;
        int x = 0;
        int y = (screen.getViewportHeight() - height) / 2 + ts;
        batch.begin();
        ResourceManager.setBatchAlpha(batch, alpha);
        batch.draw(ResourceManager.getPanelBackground(), x, y, width, height);
        BorderRenderer.drawTopBottomBorder(batch, x, y, width, height);
        if (message_lower == null) {
            FontRenderer.drawLabelCenter(batch, message_upper, x, y, width, height);
        } else {
            FontRenderer.drawLabelCenter(batch, message_upper, x, y + height / 2, width, height / 2);
            FontRenderer.drawLabelCenter(batch, message_lower, x, y, width, height / 2);
        }
        //restore alpha
        ResourceManager.setBatchAlpha(batch, 1.0f);
        batch.end();
    }

    public void update(float delta_time) {
        addStateTime(delta_time);
        if (getStateTime() > delay) {
            alpha -= delta_time / (1.0f / 30) * 0.2f;
            if (alpha < 0f) {
                alpha = 0f;
            }
        }
    }

    public boolean isAnimationFinished() {
        return alpha <= 0f;
    }

}
