package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 5/30/2015.
 */
public class CircleButton extends AEIIButton {

    public static final int SMALL = 0x1;
    public static final int LARGE = 0x2;

    private final int type;
    private final TextureRegion icon;
    private boolean is_held;

    public CircleButton(GameContext context, int type, Texture icon) {
        this(context, type, new TextureRegion(icon, icon.getWidth(), icon.getHeight()));
    }

    public CircleButton(GameContext context, int type, TextureRegion icon) {
        super(context);
        this.type = type;
        this.icon = icon;
        this.is_held = false;
        setStyle(new ButtonStyle());
        setSize(getPrefWidth(), getPrefHeight());
    }

    @Override
    public float getPrefWidth() {
        switch (type) {
            case SMALL:
                return ts * 20 / 24;
            case LARGE:
                return ts * 32 / 24;
            default:
                return super.getPrefWidth();
        }
    }

    @Override
    public float getPrefHeight() {
        switch (type) {
            case SMALL:
                return ts * 21 / 24;
            case LARGE:
                return ts * 33 / 24;
            default:
                return super.getPrefHeight();
        }
    }

    public void setHold(boolean hold) {
        this.is_held = hold;
    }

    public boolean isHeld() {
        return is_held;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        int circle_index = isPressed() || isHeld() ? 1 : 0;
        switch (type) {
            case SMALL:
                batch.draw(getResources().getSmallCircleTexture(circle_index), x, y, width, height);
                break;
            case LARGE:
                batch.draw(getResources().getBigCircleTexture(circle_index), x, y, width, height);
                break;
        }
        float scale = width / getPrefWidth();
        float icon_height = icon.getRegionHeight() * ts / 24 * scale;
        float icon_width = icon.getRegionWidth() * ts / 24 * scale;
        float dx = (width - icon_width) / 2;
        float dy = (height - icon_height) / 2;
        batch.draw(icon, x + dx, y + dy, icon_width, icon_height);
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
