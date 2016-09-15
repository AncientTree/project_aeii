package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.NetworkConstants;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.ServerConfiguration;
import net.toyknight.aeii.network.entity.MapSnapshot;
import net.toyknight.aeii.screen.dialog.ConfirmDialog;
import net.toyknight.aeii.screen.dialog.MiniMapDialog;
import net.toyknight.aeii.screen.widgets.StringList;
import net.toyknight.aeii.utils.FileProvider;
import net.toyknight.aeii.utils.InputFilter;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.MapFactory;

import java.io.IOException;

/**
 * @author toyknight 6/10/2016.
 */
public class MapManagementScreen extends StageScreen {

    private final ServerConfiguration map_server_configuration =
//            new ServerConfiguration("127.0.0.1", 5438, "aeii server - NA");
            new ServerConfiguration("45.56.93.69", 5438, "aeii server - NA");

    private final MiniMapDialog map_preview_dialog;

    private final StringList<Object> server_map_list;
    private final ScrollPane sp_server_map_list;
    private final StringList<MapFactory.MapSnapshot> local_map_list;
    private final ScrollPane sp_local_map_list;

    private final TextButton btn_download;
    private final TextButton btn_preview;
    private final TextButton btn_back;
    private final TextButton btn_refresh;
    private final TextButton btn_upload;
    private final TextButton btn_rename;
    private final TextButton btn_delete;

    private boolean symmetric;
    private String current_author;
    private float last_scroll_position_server_author_list;

    public MapManagementScreen(GameContext context) {
        super(context);

        int sp_width = (Gdx.graphics.getWidth() - ts * 3 / 2) / 2;

        Label label_online_map = new Label(Language.getText("LB_ONLINE_MAPS"), getContext().getSkin());
        label_online_map.setAlignment(Align.center);
        label_online_map.setBounds(ts / 2, Gdx.graphics.getHeight() - ts, sp_width, ts);
        addActor(label_online_map);
        Label label_local_map = new Label(Language.getText("LB_LOCAL_MAPS"), getContext().getSkin());
        label_local_map.setAlignment(Align.center);
        label_local_map.setBounds(sp_width + ts, Gdx.graphics.getHeight() - ts, sp_width, ts);
        addActor(label_local_map);

        server_map_list = new StringList<Object>(getContext(), ts);
        sp_server_map_list = new ScrollPane(server_map_list, getContext().getSkin());
        sp_server_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_server_map_list.setScrollBarPositions(true, true);
        sp_server_map_list.setBounds(ts / 2, ts * 2, sp_width, Gdx.graphics.getHeight() - ts * 3);
        server_map_list.setListener(new StringList.SelectionListener() {
            @Override
            public void onSelect(int index, Object value) {
                select(value);
            }

            @Override
            public void onChange(int index, Object value) {
                local_map_list.getSelection().clear();
                update();
            }
        });
        addActor(sp_server_map_list);
        local_map_list = new StringList<MapFactory.MapSnapshot>(getContext(), ts);
        sp_local_map_list = new ScrollPane(local_map_list, getContext().getSkin());
        sp_local_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_local_map_list.setScrollBarPositions(true, true);
        sp_local_map_list.setBounds(sp_width + ts, ts * 2, sp_width, Gdx.graphics.getHeight() - ts * 3);
        local_map_list.setListener(new StringList.SelectionListener() {
            @Override
            public void onSelect(int index, Object value) {
            }

            @Override
            public void onChange(int index, Object value) {
                server_map_list.getSelection().clear();
                update();
            }
        });
        addActor(sp_local_map_list);

        Table button_bar = new Table();
        button_bar.setBounds(ts / 2, ts / 2, Gdx.graphics.getWidth() - ts, ts);
        addActor(button_bar);

        int padding = (Gdx.graphics.getWidth() - ts * 15) / 6;

        btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back();
            }
        });
        button_bar.add(btn_back).size(ts * 2, ts);

        btn_refresh = new TextButton(Language.getText("LB_REFRESH"), getContext().getSkin());
        btn_refresh.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refresh();
            }
        });
        button_bar.add(btn_refresh).size(ts * 2, ts).padLeft(padding);

        btn_preview = new TextButton(Language.getText("LB_PREVIEW"), getContext().getSkin());
        btn_preview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (local_map_list.getSelected() == null) {
                    previewSelectedServerMap();
                } else {
                    previewSelectedLocalMap();
                }
            }
        });
        button_bar.add(btn_preview).size(ts * 2, ts).padLeft(padding);

        btn_download = new TextButton(Language.getText("LB_DOWNLOAD"), getContext().getSkin());
        btn_download.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                downloadSelectedMap();
            }
        });
        button_bar.add(btn_download).size(ts * 2, ts).padLeft(padding);

        btn_upload = new TextButton(Language.getText("LB_UPLOAD"), getContext().getSkin());
        btn_upload.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                uploadSelectedMap();
            }
        });
        button_bar.add(btn_upload).size(ts * 2, ts).padLeft(padding);

        btn_rename = new TextButton(Language.getText("LB_RENAME"), getContext().getSkin());
        btn_rename.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                rename();
            }
        });
        button_bar.add(btn_rename).size(ts * 2, ts).padLeft(padding);

        btn_delete = new TextButton(Language.getText("LB_DELETE"), getContext().getSkin());
        btn_delete.setBounds(Gdx.graphics.getWidth() - ts * 7 - ts / 2, ts / 2, ts * 2, ts);
        btn_delete.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                delete();
            }
        });
        button_bar.add(btn_delete).size(ts * 2, ts).padLeft(padding);

        map_preview_dialog = new MiniMapDialog(this);
        map_preview_dialog.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog("preview");
            }
        });
        addDialog("preview", map_preview_dialog);
    }

    private void update() {
        Object server_selected = server_map_list.getSelected();
        MapFactory.MapSnapshot local_selected = local_map_list.getSelected();
        btn_preview.setVisible(local_selected != null ||
                (server_selected instanceof MapSnapshot && !((MapSnapshot) server_selected).isDirectory()));
        btn_download.setVisible(
                server_selected instanceof MapSnapshot && !((MapSnapshot) server_selected).isDirectory());
        btn_upload.setVisible(local_selected != null);
        btn_rename.setVisible(local_selected != null);
        btn_delete.setVisible(local_selected != null);
    }

    private void back() {
        if (current_author == null) {
            getContext().gotoMainMenuScreen(false);
        } else {
            current_author = null;
            refresh();
        }
    }

    private void select(Object value) {
        if (value instanceof MapSnapshot && ((MapSnapshot) value).isDirectory()) {
            last_scroll_position_server_author_list = sp_server_map_list.getScrollPercentY();
            current_author = ((MapSnapshot) value).getAuthor();
            refresh();
        }
        if (value.equals("/...")) {
            current_author = null;
            refresh();
        }
    }

    private void rename() {
        showInput(Language.getText("MSG_INFO_IF"), 20, false, new InputFilter(), new Input.TextInputListener() {
            @Override
            public void input(String text) {
                MapFactory.MapSnapshot snapshot = local_map_list.getSelected();
                FileHandle target_file = FileProvider.getUserFile("map/" + text + ".aem");
                if (target_file.exists()) {
                    showNotification(Language.getText("MSG_ERR_FAE"), new Callable() {
                        @Override
                        public void call() {
                            rename();
                        }
                    });
                } else {
                    try {
                        snapshot.file.moveTo(target_file);
                        refreshLocalMaps();
                    } catch (GdxRuntimeException ex) {
                        showNotification(Language.getText("MSG_ERR_FRM"), null);
                    }
                }
            }

            @Override
            public void canceled() {
            }
        });
    }

    private boolean connect() {
        try {
            return NetworkManager.connect(map_server_configuration);
        } catch (Exception ex) {
            return false;
        }
    }

    private void downloadSelectedMap() {
        if (checkSelectedMap()) {
            final int map_id = ((MapSnapshot) server_map_list.getSelected()).getID();
            final String filename = ((MapSnapshot) server_map_list.getSelected()).getFilename();
            showPlaceholder(Language.getText("LB_DOWNLOADING"));
            getContext().submitAsyncTask(new AsyncTask<Void>() {
                @Override
                public Void doTask() throws Exception {
                    if (connect()) {
                        Map map = NetworkManager.requestDownloadMap(map_id);
                        NetworkManager.disconnect();
                        tryWriteMap(map, filename);
                        return null;
                    } else {
                        throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                    }
                }

                @Override
                public void onFinish(Void map) {
                    closePlaceholder();
                    showNotification(Language.getText("MSG_INFO_MD"), null);
                }

                @Override
                public void onFail(String message) {
                    closePlaceholder();
                    showNotification(message, null);
                }
            });
        }
    }

    private void uploadSelectedMap() {
        showPlaceholder(Language.getText("LB_UPLOADING"));
        getContext().submitAsyncTask(new AsyncTask<Integer>() {
            @Override
            public Integer doTask() throws Exception {
                if (connect()) {
                    FileHandle map_file = local_map_list.getSelected().file;
                    Map map = MapFactory.createMap(map_file);
                    int code = NetworkManager.requestUploadMap(map, map_file.nameWithoutExtension());
                    NetworkManager.disconnect();
                    return code;
                } else {
                    throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                }
            }

            @Override
            public void onFinish(Integer code) {
                closePlaceholder();
                if (code == NetworkConstants.CODE_OK) {
                    showNotification(Language.getText("MSG_INFO_MU"), null);
                } else {
                    showNotification(Language.getText("MSG_ERR_FUM") + " Error Code [" + code + "]", null);
                }
            }

            @Override
            public void onFail(String message) {
                closePlaceholder();
                showNotification(message, null);
            }
        });
    }

    private void tryWriteMap(Map map, String filename) {
        FileHandle map_file = FileProvider.getUserFile("map/" + filename);
        if (map == null) {
            showNotification(Language.getText("MSG_ERR_FDM"), null);
        } else {
            if (map_file.exists()) {
                showNotification(Language.getText("MSG_ERR_MAE"), null);
            } else {
                try {
                    MapFactory.writeMap(map, map_file);
                    refreshLocalMaps();
                } catch (IOException e) {
                    showNotification(Language.getText("MSG_ERR_FDM"), null);
                }
            }
        }
    }

    private void previewSelectedServerMap() {
        if (checkSelectedMap()) {
            final int map_id = ((MapSnapshot) server_map_list.getSelected()).getID();
            showPlaceholder(Language.getText("LB_DOWNLOADING"));
            getContext().submitAsyncTask(new AsyncTask<Map>() {
                @Override
                public Map doTask() throws Exception {
                    if (connect()) {
                        Map map = NetworkManager.requestDownloadMap(map_id);
                        NetworkManager.disconnect();
                        return map;
                    } else {
                        throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                    }
                }

                @Override
                public void onFinish(Map map) {
                    closePlaceholder();
                    if (map == null) {
                        showNotification(Language.getText("MSG_ERR_FDM"), null);
                    } else {
                        preview(map);
                    }
                }

                @Override
                public void onFail(String message) {
                    closePlaceholder();
                    showNotification(message, null);
                }
            });
        }
    }

    private void previewSelectedLocalMap() {
        if (local_map_list.getSelected() != null) {
            FileHandle map_file = local_map_list.getSelected().file;
            try {
                Map map = MapFactory.createMap(map_file);
                preview(map);
            } catch (AEIIException e) {
                showNotification(Language.getText("MSG_ERR_BMF"), null);
            }
        }
    }

    private void preview(Map map) {
        map_preview_dialog.setMap(map);
        map_preview_dialog.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        showDialog("preview");
    }

    private void refresh() {
        showPlaceholder(Language.getText("LB_REFRESHING"));
        getContext().submitAsyncTask(new AsyncTask<Array<MapSnapshot>>() {
            @Override
            public Array<MapSnapshot> doTask() throws Exception {
                refreshLocalMaps();
                if (connect()) {
                    Array<MapSnapshot> map_list = NetworkManager.requestMapList(current_author, symmetric);
                    NetworkManager.disconnect();
                    return map_list;
                } else {
                    throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                }
            }

            @Override
            public void onFinish(Array<MapSnapshot> map_list) {
                closePlaceholder();
                if (map_list == null) {
                    showNotification(Language.getText("MSG_ERR_CCS"), null);
                } else {
                    map_list.sort();
                    updateServerMapList(map_list);
                    update();
                }
            }

            @Override
            public void onFail(String message) {
                closePlaceholder();
                showNotification(message, null);
            }
        });
    }

    private void updateServerMapList(Array<MapSnapshot> map_list) {
        if (current_author == null) {
            server_map_list.setItems(map_list);
            sp_server_map_list.layout();
            sp_server_map_list.setScrollPercentY(last_scroll_position_server_author_list);
        } else {
            Array<Object> list = new Array<Object>(map_list);
            list.insert(0, "/...");
            server_map_list.setItems(list);
            sp_server_map_list.layout();
        }
        local_map_list.getSelection().clear();
    }

    private void refreshLocalMaps() {
        local_map_list.setItems(MapFactory.getUserMapSnapshots());
        server_map_list.getSelection().clear();
        sp_local_map_list.layout();
    }

    private void delete() {
        if (local_map_list.getSelected() != null) {
            showConfirm(Language.getText("MSG_INFO_DSM"), new ConfirmDialog.ConfirmDialogListener() {
                @Override
                public void confirmed() {
                    deleteSelectedMap();
                    refreshLocalMaps();
                }

                @Override
                public void canceled() {
                }
            });
        }
    }

    private void deleteSelectedMap() {
        FileHandle map_file = local_map_list.getSelected().file;
        map_file.delete();
    }

    private boolean checkSelectedMap() {
        Object selected = server_map_list.getSelected();
        return selected instanceof MapSnapshot && !((MapSnapshot) selected).isDirectory();
    }

    @Override
    public void act(float delta) {
        map_preview_dialog.update(delta);
        super.act(delta);
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(getResources().getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getBorderRenderer().drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(
                getResources().getBorderDarkColor(),
                sp_server_map_list.getX() - ts / 24, sp_server_map_list.getY() - ts / 24,
                sp_server_map_list.getWidth() + ts / 12, sp_server_map_list.getHeight() + ts / 12);
        batch.draw(
                getResources().getBorderDarkColor(),
                sp_local_map_list.getX() - ts / 24, sp_local_map_list.getY() - ts / 24,
                sp_local_map_list.getWidth() + ts / 12, sp_local_map_list.getHeight() + ts / 12);
        batch.end();
        super.draw();
    }

    @Override
    public void show() {
        super.show();
        symmetric = false;
        current_author = null;
        local_map_list.clearItems();
        server_map_list.clearItems();
        setNetworkRelatedButtonsEnabled(true);
        last_scroll_position_server_author_list = 0f;
        refresh();
    }

    @Override
    public void onDisconnect() {
    }

    private void setNetworkRelatedButtonsEnabled(boolean enabled) {
        GameContext.setButtonEnabled(btn_upload, enabled);
        GameContext.setButtonEnabled(btn_download, enabled);
        GameContext.setButtonEnabled(btn_preview, enabled);
        GameContext.setButtonEnabled(btn_refresh, enabled);
    }

}
