package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.manager.MapEditor;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.utils.TileFactory;

/**
 * @author toyknight 7/9/2015.
 */
public class TileSelector extends Container<ScrollPane> {

    private final int ts;
    private final MapEditor editor;
    private final ObjectMap<Short, TileButton> buttons;

    public TileSelector(MapEditor editor, int ts) {
        this.editor = editor;
        this.ts = ts;
        this.buttons = new ObjectMap<Short, TileButton>();
        this.initComponents();
    }

    private void initComponents() {
        int index = 0;
        Table tile_table = new Table();
        tile_table.padBottom(ts / 4);
        for (short i = 0; i < TileFactory.getTileCount(); i++) {
            if ((0 <= i && i <= 2) || (15 <= i && i <= 45) || (80 <= i && i < TileFactory.getTileCount())) {
                TileButton t_btn = new TileButton(getEditor(), i, ts);
                switch (index % 3) {
                    case 0:
                    case 1:
                        tile_table.add(t_btn).padLeft(ts / 4).padTop(ts / 4);
                        break;
                    case 2:
                        tile_table.add(t_btn).padLeft(ts / 4).padRight(ts / 4).padTop(ts / 4).row();
                        break;
                }
                buttons.put(i, t_btn);
                index++;
            }
        }
        tile_table.layout();
        ScrollPane sp_tile_table = new ScrollPane(tile_table);
        sp_tile_table.setScrollBarPositions(false, true);
        sp_tile_table.setFadeScrollBars(false);
        sp_tile_table.setBounds(0, 0, ts * 4, Gdx.graphics.getHeight() - ts);
        setActor(sp_tile_table);
    }

    public MapEditor getEditor() {
        return editor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
    }

}
