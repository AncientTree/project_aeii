package net.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.manager.MapEditor;
import net.toyknight.aeii.renderer.UnitRenderer;

/**
 * @author toyknight 7/9/2015.
 */
public class UnitButton extends Button {

    private final int ts;
    private final Unit unit;
    private final MapEditor editor;

    public UnitButton(MapEditor editor, Unit unit, int ts) {
        this.ts = ts;
        this.unit = unit;
        this.editor = editor;
        setStyle(new ButtonStyle());
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getEditor().setSelectedUnit(UnitButton.this.unit);
            }
        });
    }

    public MapEditor getEditor() {
        return editor;
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
        if (getEditor().getBrushType() == MapEditor.TYPE_UNIT
                && getEditor().getSelectedUnit().getIndex() == unit.getIndex()) {
            batch.draw(
                    ResourceManager.getBorderLightColor(),
                    getX(), getY(), getWidth(), getHeight());
        }
        unit.setTeam(getEditor().getSelectedTeam());
        unit.setHead(getEditor().getSelectedTeam());
        UnitRenderer.drawUnit_(batch, unit, getX(), getY(), 0, ts);
        super.draw(batch, parentAlpha);
    }

}
