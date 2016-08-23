package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.NetworkListener;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.screen.dialog.BasicDialog;
import net.toyknight.aeii.screen.dialog.ChatRoomDialog;
import net.toyknight.aeii.screen.dialog.MiniMapDialog;
import net.toyknight.aeii.screen.dialog.RoomCreateDialog;
import net.toyknight.aeii.network.entity.RoomSetting;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import net.toyknight.aeii.screen.widgets.StringList;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.MapFactory;

/**
 * @author toyknight 8/23/2015.
 */
public class LobbyScreen extends StageScreen implements NetworkListener {

    private StringList<RoomSnapshot> room_list;
    private Array<RoomSnapshot> all_rooms;

    private ChatRoomDialog chat_room_dialog;
    private RoomCreateDialog room_create_dialog;
    private MiniMapDialog map_preview_dialog;

    private TextButton btn_refresh;
    private TextButton btn_join;

    private TextField input_search;

    public LobbyScreen(GameContext context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        room_list = new StringList<RoomSnapshot>(getContext(), ts);
        ScrollPane sp_room_list = new ScrollPane(room_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        getResources().getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_room_list.setBounds(ts / 2, ts * 2, Gdx.graphics.getWidth() - ts, Gdx.graphics.getHeight() - ts * 3);
        sp_room_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_room_list.setScrollBarPositions(false, true);

        this.addActor(sp_room_list);

        int width_btn = ts * 3;
        float padding = (Gdx.graphics.getWidth() - width_btn * 5 - ts) / 4f;

        TextButton btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.setBounds(ts / 2, ts / 2, width_btn, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMainMenuScreen(false);
                NetworkManager.disconnect();
            }
        });
        addActor(btn_back);

        btn_refresh = new TextButton(Language.getText("LB_REFRESH"), getContext().getSkin());
        btn_refresh.setBounds(width_btn + ts / 2 + padding, ts / 2, width_btn, ts);
        btn_refresh.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refreshGameList();
            }
        });
        addActor(btn_refresh);

        btn_join = new TextButton(Language.getText("LB_JOIN"), getContext().getSkin());
        btn_join.setBounds(width_btn * 2 + ts / 2 + padding * 2, ts / 2, width_btn, ts);
        btn_join.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doJoinRoom();
            }
        });
        addActor(btn_join);

        TextButton btn_create = new TextButton(Language.getText("LB_CREATE"), getContext().getSkin());
        btn_create.setBounds(width_btn * 3 + ts / 2 + padding * 3, ts / 2, width_btn, ts);
        btn_create.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialog("mode");
            }
        });
        addActor(btn_create);

        TextButton btn_chat_room = new TextButton(Language.getText("LB_CHAT_ROOM"), getContext().getSkin());
        btn_chat_room.setBounds(width_btn * 4 + ts / 2 + padding * 4, ts / 2, width_btn, ts);
        btn_chat_room.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialog("chatting");
            }
        });
        addActor(btn_chat_room);

        ModeSelectDialog mode_selected_dialog = new ModeSelectDialog(this);
        addDialog("mode", mode_selected_dialog);

        chat_room_dialog = new ChatRoomDialog(this);
        addDialog("chatting", chat_room_dialog);

        room_create_dialog = new RoomCreateDialog(this);
        addDialog("create", room_create_dialog);

        map_preview_dialog = new MiniMapDialog(this);
        map_preview_dialog.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog("preview");
            }
        });
        addDialog("preview", map_preview_dialog);

        Table search_bar = new Table();
        search_bar.setBounds(Gdx.graphics.getWidth() - ts * 7 + ts / 2, Gdx.graphics.getHeight() - ts, ts * 6, ts);
        addActor(search_bar);

        Label label_search = new Label(Language.getText("LB_SEARCH"), getContext().getSkin());
        label_search.setAlignment(Align.right);
        search_bar.add(label_search).width(ts * 2);

        input_search = new TextField("", getContext().getSkin());
        input_search.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applySearch();
            }
        });
        search_bar.add(input_search).width(ts * 4).padLeft(ts / 8);
    }

    @Override
    public void onDisconnect() {
        showNotification(Language.getText("MSG_ERR_DFS"), new Callable() {
            @Override
            public void call() {
                getContext().gotoMainMenuScreen(false);
            }
        });
    }

    public void showMapPreview(MapFactory.MapSnapshot snapshot) {
        try {
            Map map = MapFactory.createMap(snapshot.file);
            showMapPreview(map);
        } catch (AEIIException ex) {
            showNotification(Language.getText("MSG_ERR_BMF"), new Callable() {
                @Override
                public void call() {
                    showDialog("create");
                }
            });
        }
    }

    public void showMapPreview(Map map) {
        map_preview_dialog.setMap(map);
        map_preview_dialog.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        showDialog("preview");
    }

    public void refreshGameList() {
        room_list.clearItems();
        Gdx.input.setInputProcessor(null);
        btn_refresh.setText(Language.getText("LB_REFRESHING"));
        getContext().submitAsyncTask(new AsyncTask<Array<RoomSnapshot>>() {
            @Override
            public Array<RoomSnapshot> doTask() throws Exception {
                return NetworkManager.requestRoomList();
            }

            @Override
            public void onFinish(Array<RoomSnapshot> result) {
                if (result == null) {
                    showNotification(Language.getText("MSG_ERR_AEA"), null);
                } else {
                    Gdx.input.setInputProcessor(LobbyScreen.this);
                    all_rooms = result;
                    applySearch();
                }
                btn_refresh.setText(Language.getText("LB_REFRESH"));
            }

            @Override
            public void onFail(String message) {
                btn_refresh.setText(Language.getText("LB_REFRESH"));
                showNotification(Language.getText("MSG_ERR_AEA"), null);
            }
        });
    }

    private void applySearch() {
        if (input_search.getText().length() > 0) {
            Array<RoomSnapshot> matched_rooms = new Array<RoomSnapshot>();
            for (RoomSnapshot snapshot : all_rooms) {
                if (snapshot.toString().contains(input_search.getText())) {
                    matched_rooms.add(snapshot);
                }
            }
            room_list.setItems(matched_rooms);
        } else {
            room_list.setItems(all_rooms);
        }
    }

    private void doJoinRoom() {
        final RoomSnapshot room = getSelectedRoom();
        if (room != null) {
            if (room.requires_password) {
                showInput(Language.getText("MSG_INFO_PIP"), 8, true, new Input.TextInputListener() {
                    @Override
                    public void input(String password) {
                        tryJoinRoom(room.room_number, password);
                    }

                    @Override
                    public void canceled() {
                    }
                });
            } else {
                tryJoinRoom(room.room_number, "");
            }
        }
    }

    private void tryJoinRoom(final long room_number, final String password) {
        if (getSelectedRoom() != null) {
            Gdx.input.setInputProcessor(null);
            btn_join.setText(Language.getText("LB_JOINING"));
            getContext().submitAsyncTask(new AsyncTask<RoomSetting>() {
                @Override
                public RoomSetting doTask() {
                    return NetworkManager.requestJoinRoom(room_number, password);
                }

                @Override
                public void onFinish(RoomSetting setting) {
                    btn_join.setText(Language.getText("LB_JOIN"));
                    if (setting == null) {
                        showNotification(Language.getText("MSG_ERR_CNJR"), null);
                    } else {
                        getContext().getRoomManager().initialize(setting);
                        if (setting.started) {
                            getContext().gotoGameScreen(getContext().getRoomManager().getArrangedGame());
                            getContext().getGameManager().syncState(
                                    setting.manager_state, setting.selected_unit_x, setting.selected_unit_y);
                        } else {
                            getContext().gotoNetGameCreateScreen();
                        }
                    }
                }

                @Override
                public void onFail(String message) {
                    btn_join.setText(Language.getText("LB_JOIN"));
                    showNotification(message, null);
                }
            });
        }
    }

    private RoomSnapshot getSelectedRoom() {
        return room_list.getSelected();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        map_preview_dialog.update(delta);
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(getResources().getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getBorderRenderer().drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getFontRenderer().drawTitleCenter(
                batch, Language.getText("LB_GAMES"), ts / 2, Gdx.graphics.getHeight() - ts, ts * 3, ts);
        batch.end();
        super.draw();
    }

    @Override
    public void show() {
        super.show();
        chat_room_dialog.clearMessage();
        input_search.setText("");
        closeAllDialogs();
        refreshGameList();
    }

    @Override
    public void onReceiveMessage(String username, String message) {
        chat_room_dialog.appendMessage(username, message);
    }

    private class ModeSelectDialog extends BasicDialog {

        private final TextButton btn_new_game;
        private final TextButton btn_load_game;

        public ModeSelectDialog(StageScreen screen) {
            super(screen);
            int width = ts * 4;
            int height = ts * 2 + ts / 2 * 3;
            this.setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - height) / 2, width, height);

            this.btn_new_game = new TextButton(Language.getText("LB_NEW_GAME"), getContext().getSkin());
            this.btn_new_game.setBounds(ts / 2, ts * 2, ts * 3, ts);
            this.btn_new_game.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    room_create_dialog.setMode(RoomCreateDialog.NEW_GAME);
                    showDialog("create");
                    closeDialog("mode");
                }
            });
            this.addActor(btn_new_game);

            this.btn_load_game = new TextButton(Language.getText("LB_LOAD_GAME"), getContext().getSkin());
            this.btn_load_game.setBounds(ts / 2, ts / 2, ts * 3, ts);
            this.btn_load_game.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    room_create_dialog.setMode(RoomCreateDialog.LOAD_GAME);
                    showDialog("create");
                    closeDialog("mode");
                }
            });
            this.addActor(btn_load_game);
        }

    }

}
