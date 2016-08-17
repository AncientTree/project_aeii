package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.ServerConfiguration;
import net.toyknight.aeii.network.entity.MapSnapshot;
import net.toyknight.aeii.screen.dialog.MessageDialog;
import net.toyknight.aeii.screen.dialog.MiniMapDialog;
import net.toyknight.aeii.screen.widgets.StringList;
import net.toyknight.aeii.utils.FileProvider;
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

    private final MessageDialog message_dialog;
    private final MiniMapDialog map_preview_dialog;

    private final StringList<Object> server_map_list;
    private final ScrollPane sp_server_map_list;
    private final StringList<MapFactory.MapSnapshot> local_map_list;
    private final ScrollPane sp_local_map_list;

    private final TextButton btn_download;
    private final TextButton btn_preview_server;
    private final TextButton btn_back;
    private final TextButton btn_refresh;
    private final TextButton btn_upload;
    private final TextButton btn_preview_local;
    private final TextButton btn_delete;

    private String current_author;
    private float last_scroll_position_server_author_list;

    public MapManagementScreen(GameContext context) {
        super(context);
        Label label_online_map = new Label(Language.getText("LB_ONLINE_MAPS"), getContext().getSkin());
        label_online_map.setAlignment(Align.center);
        label_online_map.setBounds(ts / 2, Gdx.graphics.getHeight() - ts, ts * 8, ts);
        addActor(label_online_map);
        Label label_local_map = new Label(Language.getText("LB_LOCAL_MAPS"), getContext().getSkin());
        label_local_map.setAlignment(Align.center);
        label_local_map.setBounds(ts * 9, Gdx.graphics.getHeight() - ts, ts * 8, ts);
        addActor(label_local_map);

        server_map_list = new StringList<Object>(getContext(), ts);
        sp_server_map_list = new ScrollPane(server_map_list, getContext().getSkin());
        sp_server_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_server_map_list.setScrollBarPositions(true, true);
        sp_server_map_list.setBounds(ts / 2, ts * 2, ts * 8, Gdx.graphics.getHeight() - ts * 3);
        server_map_list.setListener(new StringList.SelectionListener() {
            @Override
            public void onSelect(int index, Object value) {
                select(value);
            }

            @Override
            public void onChange(int index, Object value) {
            }
        });
        addActor(sp_server_map_list);
        local_map_list = new StringList<MapFactory.MapSnapshot>(getContext(), ts);
        sp_local_map_list = new ScrollPane(local_map_list, getContext().getSkin());
        sp_local_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_local_map_list.setScrollBarPositions(true, true);
        sp_local_map_list.setBounds(
                Gdx.graphics.getWidth() - ts * 8 - ts / 2, ts * 2, ts * 8, Gdx.graphics.getHeight() - ts * 3);
        addActor(sp_local_map_list);

        btn_download = new TextButton(Language.getText("LB_DOWNLOAD"), getContext().getSkin());
        btn_download.setBounds(ts / 2, ts / 2, ts * 2, ts);
        btn_download.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                downloadSelectedMap();
            }
        });
        addActor(btn_download);
        btn_preview_server = new TextButton(Language.getText("LB_PREVIEW"), getContext().getSkin());
        btn_preview_server.setBounds(ts * 3, ts / 2, ts * 2, ts);
        btn_preview_server.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                previewSelectedServerMap();
            }
        });
        addActor(btn_preview_server);
        btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.setBounds(ts * 5 + ts / 2, ts / 2, ts * 2, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back();
            }
        });
        addActor(btn_back);
        btn_refresh = new TextButton(Language.getText("LB_REFRESH"), getContext().getSkin());
        btn_refresh.setBounds((Gdx.graphics.getWidth() - ts * 2) / 2, ts / 2, ts * 2, ts);
        btn_refresh.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refresh();
            }
        });
        addActor(btn_refresh);
        btn_upload = new TextButton(Language.getText("LB_UPLOAD"), getContext().getSkin());
        btn_upload.setBounds(Gdx.graphics.getWidth() - ts * 2 - ts / 2, ts / 2, ts * 2, ts);
        btn_upload.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                uploadSelectedMap();
            }
        });
        addActor(btn_upload);
        btn_preview_local = new TextButton(Language.getText("LB_PREVIEW"), getContext().getSkin());
        btn_preview_local.setBounds(Gdx.graphics.getWidth() - ts * 5, ts / 2, ts * 2, ts);
        btn_preview_local.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                previewSelectedLocalMap();
            }
        });
        addActor(btn_preview_local);
        btn_delete = new TextButton(Language.getText("LB_DELETE"), getContext().getSkin());
        btn_delete.setBounds(Gdx.graphics.getWidth() - ts * 7 - ts / 2, ts / 2, ts * 2, ts);
        btn_delete.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                delete();
            }
        });
        addActor(btn_delete);

        message_dialog = new MessageDialog(this);
        addDialog("message", message_dialog);

        map_preview_dialog = new MiniMapDialog(this);
        map_preview_dialog.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog("preview");
            }
        });
        addDialog("preview", map_preview_dialog);
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

    private boolean connect() {
        try {
            return NetworkManager.connect(map_server_configuration);
        } catch (Exception ex) {
            return false;
        }
    }

    private void downloadSelectedMap() {
        if (checkSelectedMap()) {
            final String filename = ((MapSnapshot) server_map_list.getSelected()).getFilename();
            message_dialog.setMessage(Language.getText("LB_DOWNLOADING"));
            showDialog("message");
            getContext().submitAsyncTask(new AsyncTask<Map>() {
                @Override
                public Map doTask() throws Exception {
                    if (connect()) {
                        Map map = NetworkManager.requestDownloadMap(filename);
                        NetworkManager.disconnect();
                        return map;
                    } else {
                        throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                    }
                }

                @Override
                public void onFinish(Map map) {
                    closeDialog("message");
                    if (map == null) {
                        showPrompt(Language.getText("MSG_ERR_FDM"), null);
                    } else {
                        tryWriteMap(map, filename);
                    }
                }

                @Override
                public void onFail(String message) {
                    closeDialog("message");
                    showPrompt(message, null);
                }
            });
        }
    }

    private void uploadSelectedMap() {
        message_dialog.setMessage(Language.getText("LB_UPLOADING"));
        showDialog("message");
        getContext().submitAsyncTask(new AsyncTask<Boolean>() {
            @Override
            public Boolean doTask() throws Exception {
                if (connect()) {
                    FileHandle map_file = local_map_list.getSelected().file;
                    Map map = MapFactory.createMap(map_file);
                    boolean success = NetworkManager.requestUploadMap(map, map_file.nameWithoutExtension());
                    NetworkManager.disconnect();
                    return success;
                } else {
                    throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                }
            }

            @Override
            public void onFinish(Boolean success) {
                closeDialog("message");
                if (success) {
                    showPrompt(Language.getText("MSG_INFO_MU"), null);
                } else {
                    showPrompt(Language.getText("MSG_ERR_FUM"), null);
                }
            }

            @Override
            public void onFail(String message) {
                closeDialog("message");
                showPrompt(message, null);
            }
        });
    }

    private void tryWriteMap(Map map, String filename) {
        FileHandle map_file = FileProvider.getUserFile("map/" + filename);
        if (map_file.exists()) {
            showPrompt(Language.getText("MSG_ERR_MAE"), null);
        } else {
            try {
                MapFactory.writeMap(map, map_file);
                refreshLocalMaps();
                showPrompt(Language.getText("MSG_INFO_MD"), null);
            } catch (IOException e) {
                showPrompt(Language.getText("MSG_ERR_FDM"), null);
            }
        }
    }

    private void previewSelectedServerMap() {
        if (checkSelectedMap()) {
            final String filename = ((MapSnapshot) server_map_list.getSelected()).getFilename();
            message_dialog.setMessage(Language.getText("LB_DOWNLOADING"));
            showDialog("message");
            getContext().submitAsyncTask(new AsyncTask<Map>() {
                @Override
                public Map doTask() throws Exception {
                    if (connect()) {
                        Map map = NetworkManager.requestDownloadMap(filename);
                        NetworkManager.disconnect();
                        return map;
                    } else {
                        throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                    }
                }

                @Override
                public void onFinish(Map map) {
                    closeDialog("message");
                    if (map == null) {
                        showPrompt(Language.getText("MSG_ERR_FDM"), null);
                    } else {
                        preview(map);
                    }
                }

                @Override
                public void onFail(String message) {
                    closeDialog("message");
                    showPrompt(message, null);
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
                showPrompt(Language.getText("MSG_ERR_BMF"), null);
            }
        }
    }

    private void preview(Map map) {
        map_preview_dialog.setMap(map);
        map_preview_dialog.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        showDialog("preview");
    }

    private void refresh() {
        message_dialog.setMessage(Language.getText("LB_REFRESHING"));
        showDialog("message");
        getContext().submitAsyncTask(new AsyncTask<Array<MapSnapshot>>() {
            @Override
            public Array<MapSnapshot> doTask() throws Exception {
                refreshLocalMaps();
                if (connect()) {
                    Array<MapSnapshot> map_list = NetworkManager.requestMapList(current_author);
                    NetworkManager.disconnect();
                    return map_list;
                } else {
                    throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                }
            }

            @Override
            public void onFinish(Array<MapSnapshot> map_list) {
                closeDialog("message");
                map_list.sort();
                updateServerMapList(map_list);
            }

            @Override
            public void onFail(String message) {
                closeDialog("message");
                showPrompt(message, null);
            }
        });
    }

    private void updateServerMapList(Array<MapSnapshot> map_list) {
        if (current_author == null) {
            server_map_list.setItems(map_list);
            server_map_list.layout();
            sp_server_map_list.layout();
            sp_server_map_list.setScrollPercentY(last_scroll_position_server_author_list);
        } else {
            Array<Object> list = new Array<Object>(map_list);
            list.insert(0, "/...");
            server_map_list.setItems(list);
        }
    }

    private void refreshLocalMaps() {
        local_map_list.setItems(MapFactory.getUserMapSnapshots());
    }

    private void delete() {
        if (local_map_list.getSelected() != null) {
            showConfirmDialog(Language.getText("MSG_INFO_DSM"), delete_map_yes_callback, delete_map_no_callback);
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
        GameContext.setButtonEnabled(btn_preview_server, enabled);
        GameContext.setButtonEnabled(btn_refresh, enabled);
    }

    private final Callable delete_map_yes_callback = new Callable() {
        @Override
        public void call() {
            closeConfirmDialog();
            deleteSelectedMap();
            refreshLocalMaps();
        }
    };

    private final Callable delete_map_no_callback = new Callable() {
        @Override
        public void call() {
            closeConfirmDialog();
        }
    };

}
