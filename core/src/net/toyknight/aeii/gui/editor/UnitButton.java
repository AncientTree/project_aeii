package net.toyknight.aeii.gui.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.manager.MapEditor;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 7/9/2015.
 */
public class UnitButton extends Button {

    private final Unit unit;
    private final MapEditor editor;

    public UnitButton(MapEditor editor, Unit unit) {
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
        return AER.ts;
    }

    @Override
    public float getPrefHeight() {
        return AER.ts;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (getEditor().getBrushType() == MapEditor.TYPE_UNIT
                && getEditor().getSelectedUnit().getIndex() == unit.getIndex()) {
            batch.draw(
                    AER.resources.getBorderLightColor(),
                    getX(), getY(), getWidth(), getHeight());
        }
        unit.setTeam(getEditor().getSelectedTeam());
        unit.setHead(getEditor().getSelectedTeam());
        CanvasRenderer.drawUnit_(batch, unit, getX(), getY(), 0, AER.ts);
        super.draw(batch, parentAlpha);
    }

}
