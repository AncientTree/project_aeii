package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;

/**
 * Created by toyknight on 5/30/2015.
 */
public class CircleButton extends Button {

    public static final int SMALL = 0x1;
    public static final int LARGE = 0x2;

    private final int ts;
    private final int type;
    private final TextureRegion icon;

    public CircleButton(int type, TextureRegion icon, int ts) {
        this.type = type;
        this.icon = icon;
        this.ts = ts;
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        int circle_index = isPressed() ? 1 : 0;
        switch (type) {
            case SMALL:
                batch.draw(ResourceManager.getSmallCircleTexture(circle_index), x, y, width, height);
                break;
            case LARGE:
                batch.draw(ResourceManager.getBigCircleTexture(circle_index), x, y, width, height);
                break;
        }
        float icon_height = height / 21 * 16;
        float icon_width = icon_height * icon.getRegionWidth() / icon.getRegionHeight();
        float dx = (width - icon_width) / 2;
        float dy = (height - icon_height) / 2;
        batch.draw(icon, x + dx, y + dy, icon_width, icon_height);
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
