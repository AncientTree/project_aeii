package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.net.NetworkListener;
import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.screen.dialog.MiniMapDialog;
import com.toyknight.aeii.screen.dialog.RoomCreateDialog;
import com.toyknight.aeii.serializable.RoomConfig;
import com.toyknight.aeii.serializable.RoomSnapshot;
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

    private RoomCreateDialog room_create_dialog;
    private MiniMapDialog map_preview_dialog;

    private TextButton btn_refresh;
    private TextButton btn_join;

    public LobbyScreen(AEIIApplication context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        room_list = new StringList<RoomSnapshot>(ts);
        ScrollPane sp_room_list = new ScrollPane(room_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_room_list.setBounds(ts, ts * 2, Gdx.graphics.getWidth() - ts * 2, Gdx.graphics.getHeight() - ts * 3);
        sp_room_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_room_list.setScrollBarPositions(false, true);

        this.addActor(sp_room_list);

        int width_btn = ts * 3;
        int margin_left = (Gdx.graphics.getWidth() - width_btn * 4 - ts * 3) / 2;

        TextButton btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
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
                tryJoinRoom();
            }
        });
        addActor(btn_join);

        TextButton btn_create = new TextButton(Language.getText("LB_CREATE"), getContext().getSkin());
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

        map_preview_dialog = new MiniMapDialog(this);
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
        getContext().submitAsyncTask(new AsyncTask<ArrayList<RoomSnapshot>>() {
            @Override
            public ArrayList<RoomSnapshot> doTask() throws IOException, ClassNotFoundException {
                return getContext().getNetworkManager().requestOpenRoomList();
            }

            @Override
            public void onFinish(ArrayList<RoomSnapshot> result) {
                Array<RoomSnapshot> list = new Array<RoomSnapshot>();
                for (RoomSnapshot room : result) {
                    list.add(room);
                }
                room_list.setItems(list);
                if (list.size > 0) {
                    room_list.setSelectedIndex(0);
                }
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

    private void tryJoinRoom() {
        if (getSelectedRoom() != null) {
            Gdx.input.setInputProcessor(null);
            btn_join.setText(Language.getText("LB_JOINING"));
            getContext().submitAsyncTask(new AsyncTask<Object>() {
                @Override
                public Object doTask() throws IOException, ClassNotFoundException {
                    return getContext().getNetworkManager().requestJoinRoom(getSelectedRoom().getRoomNumber());
                }

                @Override
                public void onFinish(Object result) {
                    btn_join.setText(Language.getText("LB_JOIN"));
                    if (result == null) {
                        getContext().showMessage(Language.getText("MSG_ERR_CNJR"), null);
                    } else {
                        if (result instanceof RoomConfig) {
                            getContext().gotoNetGameCreateScreen((RoomConfig) result);
                        }
                        if (result instanceof GameCore) {
                            GameCore game = (GameCore) result;
                            for (int team = 0; team < 4; team++) {
                                if (game.getPlayer(team) != null) {
                                    game.getPlayer(team).setType(Player.REMOTE);
                                }
                            }
                            getContext().gotoGameScreen((GameCore) result);
                        }
                    }
                }

                @Override
                public void onFail(String message) {
                    btn_join.setText(Language.getText("LB_JOIN"));
                    getContext().showMessage(message, null);
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
        FontRenderer.drawTitleCenter(batch, Language.getText("LB_GAMES"), 0, Gdx.graphics.getHeight() - ts, Gdx.graphics.getWidth(), ts);
        batch.end();
        super.draw();
    }

    @Override
    public void show() {
        getContext().getNetworkManager().setNetworkListener(this);
        closeAllDialogs();
        refreshGameList();
    }

}
