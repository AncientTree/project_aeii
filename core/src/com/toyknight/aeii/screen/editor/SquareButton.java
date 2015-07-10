package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.toyknight.aeii.ResourceManager;

/**
 * Created by toyknight on 7/9/2015.
 */
public class SquareButton extends Button {

    private final int ts;
    private final Texture icon;

    private boolean is_held;

    public SquareButton(Texture icon, int ts) {
        this.ts = ts;
        this.icon = icon;
        this.is_held = false;
        setStyle(new ButtonStyle());
    }

    public void setHold(boolean hold) {
        this.is_held = hold;
    }

    public boolean isHeld() {
        return is_held;
    }

    @Override
    public float getPrefWidth() {
        return ts / 24 * 15;
    }

    @Override
    public float getPrefHeight() {
        return ts / 24 * 15;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isPressed() || is_held) {
            batch.draw(ResourceManager.getEditorTexture("button_square_pressed"), getX(), getY(), getWidth(), getHeight());
        } else {
            batch.draw(ResourceManager.getEditorTexture("button_square_active"), getX(), getY(), getWidth(), getHeight());
        }
        batch.draw(icon, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
