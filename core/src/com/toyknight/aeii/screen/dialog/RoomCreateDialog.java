package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.*;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.net.Response;
import com.toyknight.aeii.screen.LobbyScreen;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.serializable.RoomConfiguration;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.GameFactory;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;

import java.io.File;
import java.io.FileFilter;

/**
 * @author toyknight 8/31/2015.
 */
public class RoomCreateDialog extends BasicDialog {

    private final SaveFileFilter filter = new SaveFileFilter();

    private final Integer[] gold_preset = new Integer[]{200, 250, 300, 450, 500, 550, 700, 850, 1000, 1500, 2000};

    private int mode;
    private GameSave game_save;

    private TextButton btn_back;
    private TextButton btn_create;
    private TextButton btn_preview;

    private Label lb_initial_gold;
    private Label lb_max_population;

    private Spinner<Integer> spinner_capacity;
    private Spinner<Integer> spinner_gold;
    private Spinner<Integer> spinner_population;
    private StringList<Object> object_list;

    public RoomCreateDialog(LobbyScreen lobby_screen) {
        super(lobby_screen);
        int width = ts * 11;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, ts / 2, width, Gdx.graphics.getHeight() - ts);
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

        Label lb_capacity = new Label(Language.getText("LB_CAPACITY"), getContext().getSkin());
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
        spinner_gold.setItems(gold_preset);
        addActor(spinner_gold);

        lb_max_population = new Label(Language.getText("LB_MAX_POPULATION"), getContext().getSkin());
        lb_max_population.setBounds(ts * 6 + ts / 2 * 3, getHeight() - ts * 5 - ts / 2, ts * 3, ts);
        addActor(lb_max_population);
        spinner_population = new Spinner<Integer>(ts, getContext().getSkin());
        spinner_population.setPosition(ts * 6 + ts / 2 * 3, getHeight() - ts * 6 - ts / 2);
        spinner_population.setItems(new Integer[]{10, 15, 20, 25});
        addActor(spinner_population);
    }

    @Override
    public LobbyScreen getOwner() {
        return (LobbyScreen) super.getOwner();
    }

    public void setMode(int mode) {
        this.mode = mode;
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
            if (mode == LobbyScreen.NEW_GAME) {
                getOwner().showMapPreview((MapFactory.MapSnapshot) object_list.getSelected());
            }
            if (mode == LobbyScreen.LOAD_GAME) {
                String filename = (String) object_list.getSelected();
                FileHandle save_file = FileProvider.getSaveFile(filename);
                GameSave save = GameFactory.loadGame(save_file);
                if (save == null) {
                    getContext().showMessage(Language.getText("MSG_ERR_BSF"), new DialogCallback() {
                        @Override
                        public void doCallback() {
                            Gdx.input.setInputProcessor(getOwner().getDialogLayer());
                        }
                    });
                } else {
                    getOwner().showMapPreview(save.game.getMap());
                }
            }
        }
    }

    private void doCreateRoom() {
        if (object_list.getSelected() != null) {
            setEnabled(false);
            btn_create.setText(Language.getText("LB_CREATING"));
            getContext().submitAsyncTask(new AsyncTask<RoomConfiguration>() {
                @Override
                public RoomConfiguration doTask() throws AEIIException {
                    String map_name;
                    Map map;
                    int capacity;
                    int gold;
                    int population;
                    if (mode == LobbyScreen.NEW_GAME) {
                        map_name = ((MapFactory.MapSnapshot) object_list.getSelected()).file.name();
                        map = MapFactory.createMap(((MapFactory.MapSnapshot) object_list.getSelected()).file);
                        capacity = spinner_capacity.getSelectedItem();
                        gold = spinner_gold.getSelectedItem();
                        population = spinner_population.getSelectedItem();
                        return getContext().getNetworkManager().requestCreateRoom(map_name, map, capacity, gold, population);
                    }
                    if (mode == LobbyScreen.LOAD_GAME) {
                        String filename = (String) object_list.getSelected();
                        FileHandle save_file = FileProvider.getSaveFile(filename);
                        game_save = GameFactory.loadGame(save_file);
                        if (game_save == null) {
                            return null;
                        } else {
                            map_name = "unknown map";
                            map = game_save.game.getMap();
                            capacity = spinner_capacity.getSelectedItem();
                            population = game_save.game.getRule().getMaxPopulation();
                            return getContext().getNetworkManager().requestCreateRoom(map_name, map, capacity, -1, population);
                        }
                    }
                    return null;
                }

                @Override
                public void onFinish(RoomConfiguration config) {
                    setEnabled(true);
                    btn_create.setText(Language.getText("LB_CREATE"));
                    getContext().gotoNetGameCreateScreen(config, game_save);
                }

                @Override
                public void onFail(String message) {
                    setEnabled(true);
                    btn_create.setText(Language.getText("LB_CREATE"));
                    getOwner().closeDialog("create");
                    getContext().showMessage(message, null);
                }
            });
        }
    }

    @Override
    public void display() {
        game_save = null;
        object_list.clearItems();
        if (mode == LobbyScreen.NEW_GAME) {
            lb_initial_gold.setVisible(true);
            spinner_gold.setVisible(true);
            lb_max_population.setVisible(true);
            spinner_population.setVisible(true);
            updateMaps();
        }
        if (mode == LobbyScreen.LOAD_GAME) {
            lb_initial_gold.setVisible(false);
            spinner_gold.setVisible(false);
            lb_max_population.setVisible(false);
            spinner_population.setVisible(false);

            FileHandle save_dir = FileProvider.getUserDir("save");
            FileHandle[] save_files = save_dir.list(filter);
            Array<Object> list = new Array<Object>();
            for (FileHandle file : save_files) {
                list.add(file.name());
            }
            object_list.setItems(list);
        }
    }

    public void updateMaps() {
        Array<Object> maps = new Array<Object>(MapFactory.getAllMapSnapshots());
        object_list.setItems(maps);
    }

    private class SaveFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.exists() && !file.isDirectory()) {
                String filename = file.getName();
                return filename.endsWith(".sav");
            } else {
                return false;
            }

        }

    }

}
