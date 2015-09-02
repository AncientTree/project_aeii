package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.net.task.NetworkTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.LobbyScreen;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;

import java.io.IOException;

/**
 * Created by toyknight on 8/31/2015.
 */
public class RoomCreateDialog extends Table {

    private final int ts;
    private final LobbyScreen lobby_screen;

    private TextButton btn_back;
    private TextButton btn_create;
    private TextButton btn_preview;

    private Label lb_capacity;
    private Spinner<Integer> spinner_capacity;

    private Label lb_initial_gold;
    private Spinner<Integer> spinner_gold;

    private Label lb_max_population;
    private Spinner<Integer> spinner_population;

    private StringList<MapFactory.MapSnapshot> map_list;

    public RoomCreateDialog(LobbyScreen lobby_screen) {
        this.lobby_screen = lobby_screen;
        this.ts = getContext().getTileSize();
        int width = ts * 11;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, ts, width, Gdx.graphics.getHeight() - ts * 2);
        this.initComponents();
    }

    private void initComponents() {
        map_list = new StringList<MapFactory.MapSnapshot>(ts);
        ScrollPane sp_map_list = new ScrollPane(map_list) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_map_list.setScrollBarPositions(false, true);
        sp_map_list.setBounds(ts / 2, ts * 2, ts * 6 + ts / 2, getHeight() - ts * 2 - ts / 2);
        addActor(sp_map_list);

        btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                lobby_screen.closeDialog("create");
            }
        });
        btn_back.setBounds(ts / 2, ts / 2, ts * 3, ts);
        addActor(btn_back);
        btn_create = new TextButton(Language.getText("LB_CREATE"), getContext().getSkin());
        btn_create.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doCreateRoom();
            }
        });
        btn_create.setBounds(ts * 4, ts / 2, ts * 3, ts);
        addActor(btn_create);
        btn_preview = new TextButton(Language.getText("LB_PREVIEW"), getContext().getSkin());
        btn_preview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                lobby_screen.showMapPreview(map_list.getSelected());
            }
        });
        btn_preview.setBounds(ts * 6 + ts / 2 * 3, ts / 2, ts * 3, ts);
        addActor(btn_preview);

        lb_capacity = new Label(Language.getText("LB_CAPACITY"), getContext().getSkin());
        lb_capacity.setBounds(ts * 6 + ts / 2 * 3, getHeight() - ts - ts / 2, ts * 3, ts);
        addActor(lb_capacity);
        spinner_capacity = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_capacity.setPosition(ts * 6 + ts / 2 * 3, getHeight() - ts * 2 - ts / 2);
        spinner_capacity.setItems(new Integer[]{2, 3, 4, 5, 6, 7, 8});
        addActor(spinner_capacity);

        lb_initial_gold = new Label(Language.getText("LB_INITIAL_GOLD"), getContext().getSkin());
        lb_initial_gold.setBounds(ts * 6 + ts / 2 * 3, getHeight() - ts * 3 - ts / 2, ts * 3, ts);
        addActor(lb_initial_gold);
        spinner_gold = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_gold.setPosition(ts * 6 + ts / 2 * 3, getHeight() - ts * 4 - ts / 2);
        spinner_gold.setItems(new Integer[]{500, 1000, 1500, 2000});
        addActor(spinner_gold);

        lb_max_population = new Label(Language.getText("LB_MAX_POPULATION"), getContext().getSkin());
        lb_max_population.setBounds(ts * 6 + ts / 2 * 3, getHeight() - ts * 5 - ts / 2, ts * 3, ts);
        addActor(lb_max_population);
        spinner_population = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_population.setPosition(ts * 6 + ts / 2 * 3, getHeight() - ts * 6 - ts / 2);
        spinner_population.setItems(new Integer[]{10, 15, 20, 25});
        addActor(spinner_population);
    }

    private AEIIApplication getContext() {
        return lobby_screen.getContext();
    }

    public void setEnabled(boolean enabled) {
        AEIIApplication.setButtonEnabled(btn_back, enabled);
        AEIIApplication.setButtonEnabled(btn_create, enabled);
        AEIIApplication.setButtonEnabled(btn_preview, enabled);
        map_list.setEnabled(enabled);
        spinner_capacity.setEnabled(enabled);
        spinner_gold.setEnabled(enabled);
        spinner_population.setEnabled(enabled);
    }

    private void doCreateRoom() {
        setEnabled(false);
        btn_create.setText(Language.getText("LB_CREATING"));
        getContext().getNetworkManager().postTask(new NetworkTask<RoomConfig>() {
            @Override
            public RoomConfig doTask() throws AEIIException, IOException, ClassNotFoundException {
                String map_name = map_list.getSelected().file.name();
                Map map = MapFactory.createMap(map_list.getSelected().file);
                int capacity = spinner_capacity.getSelectedItem();
                int gold = spinner_gold.getSelectedItem();
                int population = spinner_population.getSelectedItem();
                return getContext().getNetworkManager().requestCreateRoom(map_name, map, capacity, gold, population);
            }

            @Override
            public void onFinish(RoomConfig config) {
                setEnabled(true);
                btn_create.setText(Language.getText("LB_CREATE"));
                getContext().gotoNetGameCreateScreen(config);
            }

            @Override
            public void onFail(String message) {
                setEnabled(true);
                btn_create.setText(Language.getText("LB_CREATE"));
                lobby_screen.closeDialog("create");
                getContext().showMessage(message, null);
            }
        });
    }

    public void updateMaps() {
        FileHandle[] available_maps = MapFactory.getAvailableMaps();
        Array<MapFactory.MapSnapshot> maps = new Array<MapFactory.MapSnapshot>();
        for (FileHandle map_file : available_maps) {
            MapFactory.MapSnapshot snapshot = MapFactory.createMapSnapshot(map_file);
            maps.add(snapshot);
        }
        map_list.setItems(maps);
        map_list.setSelectedIndex(0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

}
