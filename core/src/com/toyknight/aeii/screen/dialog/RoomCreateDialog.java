package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.*;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.concurrent.AsyncTask;
import com.toyknight.aeii.entity.Rule;
import com.toyknight.aeii.network.NetworkManager;
import com.toyknight.aeii.network.entity.RoomSetting;
import com.toyknight.aeii.screen.LobbyScreen;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.entity.GameSave;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.GameToolkit;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;

/**
 * @author toyknight 8/31/2015.
 */
public class RoomCreateDialog extends BasicDialog {

    public static final int NEW_GAME = 0x1;
    public static final int LOAD_GAME = 0x2;

    private int mode;

    private TextButton btn_back;
    private TextButton btn_create;
    private TextButton btn_preview;

    private Label lb_initial_gold;
    private Label lb_max_population;

    private Spinner<Integer> spinner_capacity;
    private Spinner<Integer> spinner_gold;
    private Spinner<Integer> spinner_population;
    private StringList<Object> object_list;
    private TextField input_password;

    public RoomCreateDialog(LobbyScreen lobby_screen) {
        super(lobby_screen);
        int width = ts * 11;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, ts / 4, width, Gdx.graphics.getHeight() - ts / 2);
        this.initComponents();
    }

    private void initComponents() {
        object_list = new StringList<Object>(ts);
        ScrollPane sp_map_list = new ScrollPane(object_list, getContext().getSkin()) {
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
                getOwner().closeDialog("create");
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
                preview();
            }
        });
        btn_preview.setBounds(ts * 6 + ts / 2 * 3, ts / 2, ts * 3, ts);
        addActor(btn_preview);

        Table setting_pane = new Table();
        setting_pane.setBounds(ts * 7 + ts / 2, ts * 2, ts * 3, getHeight() - ts * 2 - ts / 2);
        addActor(setting_pane);

        Label lb_capacity = new Label(Language.getText("LB_CAPACITY"), getContext().getSkin());
        setting_pane.add(lb_capacity).width(ts * 3).padBottom(ts / 4).row();
        spinner_capacity = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_capacity.setItems(new Integer[]{2, 3, 4, 5, 6, 7, 8});
        setting_pane.add(spinner_capacity).size(ts * 3, ts).padBottom(ts / 4).row();

        lb_initial_gold = new Label(Language.getText("LB_START_GOLD"), getContext().getSkin());
        setting_pane.add(lb_initial_gold).width(ts * 3).padBottom(ts / 4).row();
        spinner_gold = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_gold.setItems(Rule.GOLD_PRESET);
        setting_pane.add(spinner_gold).size(ts * 3, ts).padBottom(ts / 4).row();

        lb_max_population = new Label(Language.getText("LB_MAX_POPULATION"), getContext().getSkin());
        setting_pane.add(lb_max_population).width(ts * 3).padBottom(ts / 4).row();
        spinner_population = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_population.setItems(Rule.POPULATION_PRESET);
        setting_pane.add(spinner_population).size(ts * 3, ts).padBottom(ts / 4).row();

        Label lb_password = new Label(Language.getText("LB_PASSWORD"), getContext().getSkin());
        setting_pane.add(lb_password).width(ts * 3).padBottom(ts / 4).row();
        input_password = new TextField("", getContext().getSkin());
        input_password.setMaxLength(8);
        setting_pane.add(input_password).size(ts * 3, ts / 2);
    }

    @Override
    public LobbyScreen getOwner() {
        return (LobbyScreen) super.getOwner();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setEnabled(boolean enabled) {
        GameContext.setButtonEnabled(btn_back, enabled);
        GameContext.setButtonEnabled(btn_create, enabled);
        GameContext.setButtonEnabled(btn_preview, enabled);
        object_list.setEnabled(enabled);
        spinner_capacity.setEnabled(enabled);
        spinner_gold.setEnabled(enabled);
        spinner_population.setEnabled(enabled);
    }

    private void preview() {
        if (object_list.getSelected() != null) {
            if (mode == NEW_GAME) {
                getOwner().showMapPreview((MapFactory.MapSnapshot) object_list.getSelected());
            }
            if (mode == LOAD_GAME) {
                FileHandle save_file = (FileHandle) object_list.getSelected();
                GameSave save = GameToolkit.loadGame(save_file);
                if (save == null) {
                    getOwner().showPrompt(Language.getText("MSG_ERR_BSF"), null);
                } else {
                    getOwner().showMapPreview(save.getGame().getMap());
                }
            }
        }
    }

    private void doCreateRoom() {
        if (object_list.getSelected() != null) {
            setEnabled(false);
            btn_create.setText(Language.getText("LB_CREATING"));
            getContext().submitAsyncTask(new AsyncTask<RoomSetting>() {
                @Override
                public RoomSetting doTask() throws AEIIException {
                    return tryCreateRoom();
                }

                @Override
                public void onFinish(RoomSetting setting) {
                    setEnabled(true);
                    btn_create.setText(Language.getText("LB_CREATE"));
                    if (setting == null) {
                        getOwner().showPrompt(Language.getText("MSG_ERR_CNCR"), null);
                    } else {
                        getContext().getRoomManager().initialize(setting);
                        getContext().gotoNetGameCreateScreen();
                    }
                }

                @Override
                public void onFail(String message) {
                    setEnabled(true);
                    btn_create.setText(Language.getText("LB_CREATE"));
                    getOwner().showPrompt(Language.getText("MSG_ERR_CNCR"), null);
                }
            });
        }
    }

    private RoomSetting tryCreateRoom() throws AEIIException {
        String password = input_password.getText();
        switch (getMode()) {
            case NEW_GAME:
                String map_name = object_list.getSelected().toString();
                Map map = MapFactory.createMap(((MapFactory.MapSnapshot) object_list.getSelected()).file);
                int capacity = spinner_capacity.getSelectedItem();
                int gold = spinner_gold.getSelectedItem();
                int population = spinner_population.getSelectedItem();
                return NetworkManager.requestCreateRoom(map_name, map, capacity, gold, population, password);
            case LOAD_GAME:
                FileHandle save_file = (FileHandle) object_list.getSelected();
                GameSave game_save = GameToolkit.loadGame(save_file);
                if (game_save == null) {
                    return null;
                } else {
                    capacity = spinner_capacity.getSelectedItem();
                    return NetworkManager.requestCreateRoom(game_save.getGame(), capacity, password);
                }
            default:
                return null;
        }
    }

    @Override
    public void display() {
        object_list.clearItems();
        switch (mode) {
            case NEW_GAME:
                lb_initial_gold.setVisible(true);
                spinner_gold.setVisible(true);
                lb_max_population.setVisible(true);
                spinner_population.setVisible(true);
                updateMaps();
                break;
            case LOAD_GAME:
                lb_initial_gold.setVisible(false);
                spinner_gold.setVisible(false);
                lb_max_population.setVisible(false);
                spinner_population.setVisible(false);
                updateSaveFiles();
            default:
                //do nothing
        }
    }

    public void updateMaps() {
        Array<MapFactory.MapSnapshot> maps = MapFactory.getAllMapSnapshots();
        object_list.setItems(maps);
    }

    public void updateSaveFiles() {
        Array<FileHandle> save_files = FileProvider.getSaveFiles();
        object_list.setItems(save_files);
    }

}
