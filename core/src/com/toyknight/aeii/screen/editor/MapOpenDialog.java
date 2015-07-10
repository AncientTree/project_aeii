package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.MapEditorScreen;
import com.toyknight.aeii.screen.widgets.MapList;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 7/9/2015.
 */
public class MapOpenDialog extends Table {

    private final int ts;
    private final MapEditorScreen editor;

    private MapList map_list;

    public MapOpenDialog(MapEditorScreen editor) {
        this.editor = editor;
        this.ts = getContext().getTileSize();
        this.initComponents();
    }

    private void initComponents() {
        this.map_list = new MapList(ts);
        ScrollPane sp_map_list = new ScrollPane(map_list);
        sp_map_list.setBounds(ts / 4, ts, ts * 6 - ts / 2, ts * 7 - ts / 4);
        sp_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_map_list.setScrollBarPositions(false, true);
        sp_map_list.setFadeScrollBars(false);
        this.addActor(sp_map_list);

        TextButton btn_open = new TextButton(Language.getText("LB_OPEN"), getContext().getSkin());
        btn_open.setBounds(ts / 4, ts / 4, ts * 2, ts / 2);
        btn_open.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editor.setMap(map_list.getSelectedMap(), map_list.getSelectedMapName());
                setVisible(false);
            }
        });
        this.addActor(btn_open);

        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.setBounds(ts * 6 - ts / 4 - ts * 2, ts / 4, ts * 2, ts / 2);
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        this.addActor(btn_cancel);
    }

    private AEIIApplication getContext() {
        return editor.getContext();
    }

    public void display() {
        map_list.setMaps(FileProvider.getUserDir("map/").list());
        this.setVisible(true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
    }

}
