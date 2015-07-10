package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.MapEditorScreen;

/**
 * Created by toyknight on 7/9/2015.
 */
public class UnitButton extends Button {

    private final int ts;
    private final Unit unit;
    private final MapEditorScreen editor;

    public UnitButton(MapEditorScreen editor, Unit unit, int ts) {
        this.ts = ts;
        this.unit = unit;
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
        editor.setSelectedUnit(unit);
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
        batch.draw(
                ResourceManager.getUnitTexture(unit.getPackage(), editor.getSelectedTeam(), unit.getIndex(), 0, 0),
                getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

}
