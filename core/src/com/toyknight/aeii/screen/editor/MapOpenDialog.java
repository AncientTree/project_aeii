package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.screen.MapEditorScreen;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;

/**
 * Created by toyknight on 7/9/2015.
 */
public class MapOpenDialog extends BasicDialog {

    private StringList<MapFactory.MapSnapshot> map_list;

    public MapOpenDialog(MapEditorScreen editor) {
        super(editor);
        initComponents();
    }

    private void initComponents() {
        this.map_list = new StringList<MapFactory.MapSnapshot>(ts);
        ScrollPane sp_map_list = new ScrollPane(map_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_map_list.setBounds(ts / 4, ts + ts / 2, ts * 8 - ts / 2, ts * 6 - ts / 4 * 3);
        sp_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_map_list.setScrollBarPositions(false, true);
        this.addActor(sp_map_list);

        TextButton btn_open = new TextButton(Language.getText("LB_OPEN"), getContext().getSkin());
        btn_open.setBounds(ts / 4, ts / 4, ts * 3, ts);
        btn_open.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryLoadMap();
            }
        });
        this.addActor(btn_open);

        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.setBounds(ts * 8 - ts / 4 - ts * 3, ts / 4, ts * 3, ts);
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("open");
            }
        });
        this.addActor(btn_cancel);
    }

    public MapEditorScreen getOwner() {
        return (MapEditorScreen) super.getOwner();
    }

    private void tryLoadMap() {
        MapFactory.MapSnapshot snapshot = map_list.getSelected();
        try {
            Map map = MapFactory.createMap(snapshot.file);
            getOwner().setMap(map, snapshot.file.nameWithoutExtension());
            getOwner().closeDialog("open");
        } catch (AEIIException ex) {
            getContext().showMessage(Language.getText("MSG_ERR_BMF"), null);
        }
    }

    public void display() {
        //update maps
        Array<MapFactory.MapSnapshot> user_maps = MapFactory.getUserMapSnapshots();
        map_list.setItems(user_maps);
    }

}
