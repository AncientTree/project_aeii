package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.MapEditorScreen;
import com.toyknight.aeii.utils.TileFactory;

/**
 * @author toyknight 7/9/2015.
 */
public class TileSelector extends Container {

    private final int ts;
    private final MapEditorScreen editor;

    public TileSelector(MapEditorScreen editor) {
        this.editor = editor;
        this.ts = getContext().getTileSize();
        this.initComponents();
    }

    private void initComponents() {
        int index = 0;
        Table tile_table = new Table();
        for (int i = 0; i < TileFactory.getTileCount(); i++) {
            if ((0 <= i && i <= 2) || (15 <= i && i <= 45) || (80 <= i && i < TileFactory.getTileCount())) {
                TileButton t_btn = new TileButton(editor, (short) i, ts);
                if (index % 3 == 2) {
                    tile_table.add(t_btn).padTop(ts / 4).row();
                } else {
                    tile_table.add(t_btn).padRight(ts / 4).padTop(ts / 4);
                }
                index++;
            }
        }
        tile_table.layout();
        ScrollPane sp_tile_table = new ScrollPane(tile_table);
        sp_tile_table.setScrollBarPositions(false, true);
        sp_tile_table.setFadeScrollBars(false);
        sp_tile_table.setBounds(0, 0, ts * 4, Gdx.graphics.getHeight() - ts);
        this.setActor(sp_tile_table);
    }

    private AEIIApplication getContext() {
        return editor.getContext();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
    }

}
