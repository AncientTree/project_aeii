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
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.net.task.NetworkTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.screen.dialog.MiniMapDialog;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.server.entity.PlayerSnapshot;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 8/28/2015.
 */
public class NetGameCreateScreen extends StageScreen {

    private RoomConfig room_config;
    private Array<PlayerSnapshot> players = new Array<PlayerSnapshot>();

    private TextButton btn_start;
    private TextButton btn_leave;

    private StringList<PlayerSnapshot> player_list;

    private MiniMapDialog map_preview;

    public NetGameCreateScreen(AEIIApplication context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        btn_start = new TextButton(Language.getText("LB_START"), getContext().getSkin());
        btn_start.setBounds(ts / 2, ts / 2, ts * 3, ts);
        btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryStartGame();
            }
        });
        addActor(btn_start);

        btn_leave = new TextButton(Language.getText("LB_LEAVE"), getContext().getSkin());
        btn_leave.setBounds(ts * 4, ts / 2, ts * 3, ts);
        btn_leave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryLeaveRoom();
            }
        });
        addActor(btn_leave);

        TextButton btn_preview = new TextButton(Language.getText("LB_PREVIEW"), getContext().getSkin());
        btn_preview.setBounds(ts * 6 + ts / 2 * 3, ts / 2, ts * 3, ts);
        btn_preview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialog("preview");
            }
        });
        addActor(btn_preview);

        map_preview = new MiniMapDialog(ts, getContext().getSkin());
        map_preview.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog("preview");
            }
        });
        addDialog("preview", map_preview);

        player_list = new StringList<PlayerSnapshot>(ts);
        ScrollPane sp_player_list = new ScrollPane(player_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_player_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_player_list.setScrollBarPositions(false, true);
        sp_player_list.setBounds(ts / 2, ts * 2, ts * 5 + ts / 2, Gdx.graphics.getHeight() - ts * 2 - ts / 2);
        addActor(sp_player_list);
    }

    public void setRoomConfig(RoomConfig config) {
        this.room_config = config;
        players.clear();
        for (PlayerSnapshot player : config.players) {
            players.add(player);
        }
        player_list.setItems(players);
        player_list.setSelectedIndex(0);
    }

    public RoomConfig getRoomConfig() {
        return room_config;
    }

    private void tryLeaveRoom() {
        Gdx.input.setInputProcessor(null);
        btn_leave.setText(Language.getText("LB_LEAVING"));
        getContext().getNetworkManager().postTask(new NetworkTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().requestLeaveRoom();
                return null;
            }

            @Override
            public void onFinish(Void result) {
                btn_leave.setText(Language.getText("LB_LEAVE"));
                getContext().gotoLobbyScreen();
            }

            @Override
            public void onFail(String message) {
                btn_leave.setText(Language.getText("LB_LEAVE"));
                getContext().showMessage(message, null);
            }
        });
    }

    private void tryStartGame() {
        //local check
        Gdx.input.setInputProcessor(null);
        btn_start.setText(Language.getText("LB_STARTING"));
        getContext().getNetworkManager().postTask(new NetworkTask<Boolean>() {
            @Override
            public Boolean doTask() throws Exception {
                return getContext().getNetworkManager().requestStartGame();
            }

            @Override
            public void onFinish(Boolean success) {
                if (success) {
                    btn_start.setText(Language.getText("LB_START"));
                    createGame();
                } else {
                    btn_start.setText(Language.getText("LB_START"));
                    getContext().showMessage(Language.getText("MSG_ERR_CNSG"), null);
                }
            }

            @Override
            public void onFail(String message) {
                btn_start.setText(Language.getText("LB_START"));
                getContext().showMessage(message, null);
            }
        });
    }

    private void createGame() {
        GameHost.setHost(isHost());
        Player[] players = new Player[4];
        for (int team = 0; team < 4; team++) {
            if (room_config.map.getTeamAccess(team)) {
                players[team] = new Player();
                players[team].setAlliance(room_config.alliance_state[team]);
                players[team].setGold(room_config.initial_gold);
                if (getContext().getNetworkManager().getServiceName().equals(room_config.team_allocation[team])) {
                    players[team].setType(room_config.player_type[team]);
                } else {
                    players[team].setType(Player.REMOTE);
                }
            }
        }
        Rule rule = Rule.getDefaultRule();
        rule.setMaxPopulation(room_config.max_population);
        GameCore game = new GameCore(room_config.map, rule, players);
        getContext().gotoGameScreen(game);
    }

    private boolean isHost() {
        return getContext().getNetworkManager().getServiceName().equals(room_config.host);
    }

    @Override
    public void draw() {
        batch.begin();
        drawBackground();
        batch.end();
        super.draw();
    }

    private void drawBackground() {
        batch.draw(ResourceManager.getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        getContext().getNetworkManager().setNetworkListener(this);
        map_preview.setMap(room_config.map);
        map_preview.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void onPlayerJoin(String service_name, String username) {
        PlayerSnapshot snapshot = new PlayerSnapshot();
        snapshot.service_name = service_name;
        snapshot.username = username;
        players.add(snapshot);
        player_list.setItems(players);
        //show in message box
    }

    @Override
    public void onPlayerLeave(String service_name, String username) {
        if (service_name.equals(room_config.host)) {
            getContext().showMessage(Language.getText("MSG_ERR_HPD"), new DialogCallback() {
                @Override
                public void doCallback() {
                    tryLeaveRoom();
                }
            });
        } else {
            for (int i = 0; i < players.size; i++) {
                if (service_name.equals(players.get(i).service_name)) {
                    players.removeIndex(i);
                    break;
                }
            }
            player_list.setItems(players);
            //show in message box
        }
    }

    @Override
    public void onGameStart() {
        createGame();
    }

}
