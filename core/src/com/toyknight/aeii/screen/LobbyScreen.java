package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.net.NetworkListener;
import com.toyknight.aeii.net.task.NetworkTask;
import com.toyknight.aeii.screen.dialog.MiniMapDialog;
import com.toyknight.aeii.screen.dialog.RoomCreateDialog;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.server.entity.RoomSnapshot;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by toyknight on 8/23/2015.
 */
public class LobbyScreen extends StageScreen implements NetworkListener {

    private StringList<RoomSnapshot> room_list;

    private RoomConfig room_config;

    private RoomCreateDialog room_create_dialog;
    private MiniMapDialog map_preview_dialog;

    private TextButton btn_back;
    private TextButton btn_refresh;
    private TextButton btn_join;
    private TextButton btn_create;

    public LobbyScreen(AEIIApplication context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        room_list = new StringList(ts);
        ScrollPane sp_room_list = new ScrollPane(room_list);
        sp_room_list.setBounds(ts, ts * 2, Gdx.graphics.getWidth() - ts * 2, Gdx.graphics.getHeight() - ts * 4);
        sp_room_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_room_list.setScrollBarPositions(false, true);

        this.addActor(sp_room_list);

        int width_btn = ts * 3;
        int margin_left = (Gdx.graphics.getWidth() - width_btn * 4 - ts * 3) / 2;

        btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.setBounds(margin_left, ts / 2, width_btn, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().getNetworkManager().disconnect();
                getContext().gotoMainMenuScreen();
            }
        });
        addActor(btn_back);

        btn_refresh = new TextButton(Language.getText("LB_REFRESH"), getContext().getSkin());
        btn_refresh.setBounds(margin_left + width_btn + ts, ts / 2, width_btn, ts);
        btn_refresh.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refreshGameList();
            }
        });
        addActor(btn_refresh);

        btn_join = new TextButton(Language.getText("LB_JOIN"), getContext().getSkin());
        btn_join.setBounds(margin_left + width_btn * 2 + ts * 2, ts / 2, width_btn, ts);
        btn_join.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doJoinRoom();
            }
        });
        addActor(btn_join);

        btn_create = new TextButton(Language.getText("LB_CREATE"), getContext().getSkin());
        btn_create.setBounds(margin_left + width_btn * 3 + ts * 3, ts / 2, width_btn, ts);
        btn_create.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                room_create_dialog.updateMaps();
                showDialog("create");
            }
        });
        addActor(btn_create);

        room_create_dialog = new RoomCreateDialog(this);
        addDialog("create", room_create_dialog);

        map_preview_dialog = new MiniMapDialog(ts, getContext().getSkin());
        map_preview_dialog.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialog("create");
            }
        });
        addDialog("preview", map_preview_dialog);
    }

    @Override
    public void onDisconnect() {
        getContext().showMessage(Language.getText("MSG_ERR_DFS"), new DialogCallback() {
            @Override
            public void doCallback() {
                getContext().gotoMainMenuScreen();
            }
        });
    }

    public void showMapPreview(MapFactory.MapSnapshot snapshot) {
        try {
            Map map = MapFactory.createMap(snapshot.file);
            map_preview_dialog.setMap(map);
            map_preview_dialog.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            showDialog("preview");
        } catch (AEIIException ex) {
        }
    }

    public void refreshGameList() {
        room_list.clearItems();
        Gdx.input.setInputProcessor(null);
        btn_refresh.setText(Language.getText("LB_REFRESHING"));
        getContext().getNetworkManager().postTask(new NetworkTask() {
            @Override
            public boolean doTask() throws IOException, ClassNotFoundException {
                ArrayList<RoomSnapshot> open_rooms = getContext().getNetworkManager().requestOpenRoomList();
                Array<RoomSnapshot> list = new Array<RoomSnapshot>();
                for (RoomSnapshot room : open_rooms) {
                    list.add(room);
                }
                room_list.setItems(list);
                if (list.size > 0) {
                    room_list.setSelectedIndex(0);
                }
                return true;
            }

            @Override
            public void onFinish() {
                btn_refresh.setText(Language.getText("LB_REFRESH"));
                Gdx.input.setInputProcessor(LobbyScreen.this);
            }

            @Override
            public void onFail(String message) {
                btn_refresh.setText(Language.getText("LB_REFRESH"));
                getContext().showMessage(message, null);
            }
        });
    }

    private void doJoinRoom() {
        if (getSelectedRoom() != null) {
            Gdx.input.setInputProcessor(null);
            getContext().getNetworkManager().postTask(new NetworkTask() {
                @Override
                public boolean doTask() throws IOException, ClassNotFoundException {
                    room_config = getContext().getNetworkManager().requestJoinRoom(getSelectedRoom().getRoomNumber());
                    return room_config != null;
                }

                @Override
                public void onFinish() {
                    getContext().gotoNetGameCreateScreen(room_config);
                }

                @Override
                public void onFail(String message) {
                    Gdx.input.setInputProcessor(LobbyScreen.this);
                    getContext().showMessage(Language.getText("MSG_ERR_CNJR"), null);
                }
            });
        }
    }

    private RoomSnapshot getSelectedRoom() {
        return room_list.getSelected();
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(ResourceManager.getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(
                ResourceManager.getBorderDarkColor(), ts - ts / 12, ts * 2 - ts / 12,
                Gdx.graphics.getWidth() - ts * 2 + ts / 6, Gdx.graphics.getHeight() - ts * 4 + ts / 6);
        FontRenderer.drawTitleCenter(batch, Language.getText("LB_GAMES"), 0, Gdx.graphics.getHeight() - ts * 2, Gdx.graphics.getWidth(), ts * 2);
        batch.end();
        super.draw();
    }

    @Override
    public void show() {
        getContext().getNetworkManager().setNetworkListener(this);
        refreshGameList();
    }

}
