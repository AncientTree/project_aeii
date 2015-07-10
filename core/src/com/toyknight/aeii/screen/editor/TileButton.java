package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.MapEditorScreen;

/**
 * Created by toyknight on 7/9/2015.
 */
public class TileButton extends Button {

    private final int ts;
    private final short index;
    private final MapEditorScreen editor;

    public TileButton(MapEditorScreen editor, short index, int ts) {
        this.ts = ts;
        this.index = index;
        this.editor = editor;
        setStyle(new ButtonStyle());
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doClick();
            }
        });
    }

    private void doClick() {
        editor.setSelectedTileIndex(index);
    }

    @Override
    public float getPrefWidth() {
        return ts;
    }

    @Override
    public float getPrefHeight() {
        return ts;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getTileTexture(index), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

}
