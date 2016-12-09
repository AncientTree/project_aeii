package net.toyknight.aeii.gui.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.manager.MapEditor;
import net.toyknight.aeii.gui.widgets.AEIIButton;

/**
 * @author toyknight 7/9/2015.
 */
public class TileButton extends AEIIButton {

    private final short index;
    private final MapEditor editor;

    public TileButton(GameContext context, MapEditor editor, short index) {
        super(context);
        this.index = index;
        this.editor = editor;
        setStyle(new ButtonStyle());
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getEditor().setSelectedTileIndex(TileButton.this.index);
            }
        });
    }

    public short getIndex() {
        return index;
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
        if (getEditor().getBrushType() == MapEditor.TYPE_TILE && getEditor().getSelectedTileIndex() == index) {
            batch.draw(
                    getResources().getMovePathColor(),
                    getX() - ts / 24, getY() - ts / 24,
                    getWidth() + ts / 12, getHeight() + ts / 12);
        }
        batch.draw(
                getResources().getTileTexture(index),
                getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

}
