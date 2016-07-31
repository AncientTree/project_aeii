package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 9/7/2015.
 */
public class MessageBoard extends AEIITable {

    private final float content_width;

    private boolean fading;

    private float alpha = 3f;

    public MessageBoard(GameContext context, float content_width) {
        super(context);
        this.bottom();
        this.fading = true;
        this.content_width = content_width;
        this.setSize(content_width, Gdx.graphics.getHeight() - ts * 57 / 24);
        this.setCullingArea(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    public void setFading(boolean fading) {
        this.fading = fading;
    }

    public void display() {
        alpha = 3f;
    }

    public float getAlpha() {
        if (alpha > 1f) {
            return 1f;
        }
        if (alpha < 0f) {
            return 0f;
        }
        return alpha;
    }

    public void appendMessage(String username, String message) {
        String content = username == null ? ">" + message : ">" + username + ": " + message;
        Label label_message = new Label(content, getContext().getSkin());
        label_message.setWrap(true);
        add(label_message).width(content_width).padLeft(ts / 8).padBottom(ts / 8).row();
        display();
    }

    public void clearMessages() {
        clearChildren();
    }

    public void update(float delta) {
        if (alpha > 0 && fading) {
            if (alpha > 1) {
                alpha -= delta;
            } else {
                alpha -= delta * 3;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, getAlpha());
    }

}
