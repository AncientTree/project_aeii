package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
import com.toyknight.aeii.screen.widgets.PlayerAllocationButton;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.SpinnerListener;
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

    private TextButton[] btn_allocate;
    private Image[] team_image;
    private Spinner<Integer>[] spinner_alliance;
    private Spinner<String>[] spinner_type;
    private Label[] lb_username;
    private ScrollPane[] sp_username;

    private Label lb_population;
    private Label lb_gold;

    private Table team_setting_table;

    private StringList<PlayerSnapshot> player_list;

    private MiniMapDialog map_preview;

    public NetGameCreateScreen(AEIIApplication context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        Label lb_team = new Label(Language.getText("LB_TEAM"), getContext().getSkin());
        lb_team.setPosition(
                ts * 7 + (ts * 3 - lb_team.getPrefWidth()) / 2,
                Gdx.graphics.getHeight() - ts / 2 - lb_team.getPrefHeight());
        addActor(lb_team);
        Label lb_type = new Label(Language.getText("LB_TYPE"), getContext().getSkin());
        lb_type.setPosition(
                ts * 10 + ts / 2 + (ts * 4 - lb_type.getPrefWidth()) / 2,
                Gdx.graphics.getHeight() - ts / 2 - lb_type.getPrefHeight());
        addActor(lb_type);

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

        int tstw = Gdx.graphics.getWidth() - ts * 4;
        int tsth = ts * 4 + ts / 2 * 4;
        team_setting_table = new Table();
        team_setting_table.setBounds(
                ts * 4, Gdx.graphics.getHeight() - ts / 2 - lb_team.getPrefHeight() - tsth, tstw, tsth);
        addActor(team_setting_table);

        btn_allocate = new TextButton[4];
        team_image = new Image[4];
        spinner_alliance = new Spinner[4];
        spinner_type = new Spinner[4];
        lb_username = new Label[4];
        sp_username = new ScrollPane[4];
        for (int team = 0; team < 4; team++) {
            btn_allocate[team] = new PlayerAllocationButton(team, getContext().getSkin());
            btn_allocate[team].addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    int team = ((PlayerAllocationButton) event.getListenerActor()).getTeam();
                    allocateSelectedPlayer(team);
                }
            });

            TextureRegionDrawable team_color =
                    new TextureRegionDrawable(new TextureRegion(ResourceManager.getTeamBackground(team)));
            team_color.setMinWidth(ts);
            team_color.setMinHeight(ts);
            team_image[team] = new Image(team_color);

            Integer[] alliance = new Integer[]{1, 2, 3, 4};
            spinner_alliance[team] = new Spinner<Integer>(ts, getContext().getSkin());
            spinner_alliance[team].setListener(alliance_listener);
            spinner_alliance[team].setItems(alliance);

            String[] type = new String[]{Language.getText("LB_NONE"), Language.getText("LB_PLAYER")};
            spinner_type[team] = new Spinner<String>(ts, getContext().getSkin());
            spinner_type[team].setListener(allocation_listener);
            spinner_type[team].setContentWidth(ts * 2);
            spinner_type[team].setItems(type);

            lb_username[team] = new Label("", getContext().getSkin());
            sp_username[team] = new ScrollPane(lb_username[team]);
            sp_username[team].setScrollBarPositions(true, false);
        }

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
        sp_player_list.setBounds(ts / 2, ts * 2, ts * 3, Gdx.graphics.getHeight() - ts * 2 - ts / 2);
        addActor(sp_player_list);

        HorizontalGroup info_group = new HorizontalGroup();
        info_group.setBounds(ts * 4, ts * 2, Gdx.graphics.getWidth() - ts * 4, ts * 11 / 24);

        TextureRegion tr_population = ResourceManager.getStatusHudIcon(0);
        TextureRegionDrawable trd_population = new TextureRegionDrawable(tr_population);
        trd_population.setMinWidth(ts / 24 * 11);
        trd_population.setMinHeight(ts / 24 * 11);
        Image ico_population = new Image(trd_population);
        info_group.addActor(ico_population);
        lb_population = new Label("", getContext().getSkin());
        info_group.addActor(lb_population);

        TextureRegion tr_gold = ResourceManager.getStatusHudIcon(1);
        TextureRegionDrawable trd_gold = new TextureRegionDrawable(tr_gold);
        trd_gold.setMinWidth(ts / 24 * 11);
        trd_gold.setMinHeight(ts / 24 * 11);
        Image ico_gold = new Image(trd_gold);
        info_group.addActor(ico_gold);
        lb_gold = new Label("", getContext().getSkin());
        info_group.addActor(lb_gold);

        addActor(info_group);
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

    public String getUsername(String service_name) {
        for (PlayerSnapshot player : players) {
            if (player.service_name.equals(service_name)) {
                return player.username;
            }
        }
        return "";
    }

    public void updateStatus() {
        for (int team = 0; team < 4; team++) {
            String player_service = getRoomConfig().team_allocation[team];
            if (room_config.map.hasTeamAccess(team)) {
                if (player_service.equals("NONE")) {
                    spinner_type[team].setSelectedIndex(0);
                    lb_username[team].setText("");
                } else {
                    spinner_type[team].setSelectedIndex(1);
                    lb_username[team].setText(getUsername(getRoomConfig().team_allocation[team]));
                }
                spinner_alliance[team].setSelectedIndex(room_config.alliance_state[team] - 1);
            }
        }
    }

    public void updateAllocation() {
        for (int team = 0; team < 4; team++) {
            if (room_config.map.hasTeamAccess(team)) {
                String selected = spinner_type[team].getSelectedItem();
                if (selected.equals(Language.getText("LB_NONE"))) {
                    room_config.team_allocation[team] = "NONE";
                }
                if (selected.equals(Language.getText("LB_PLAYER"))) {
                    if (room_config.team_allocation[team].equals("NONE")) {
                        room_config.team_allocation[team] = getContext().getNetworkManager().getServiceName();
                        room_config.player_type[team] = Player.LOCAL;
                    }
                }
            }
        }
        updateStatus();
        tryUpdateAllocation();
    }

    public void updateAlliance() {
        for (int team = 0; team < 4; team++) {
            if (room_config.map.hasTeamAccess(team)) {
                room_config.alliance_state[team] = spinner_alliance[team].getSelectedItem();
            }
        }
        updateStatus();
        tryUpdateAlliance();
    }

    public void allocateSelectedPlayer(int team) {
        String service_name = player_list.getSelected().service_name;
        room_config.team_allocation[team] = service_name;
        room_config.player_type[team] = Player.LOCAL;
        updateStatus();
        tryUpdateAllocation();
    }

    private void tryUpdateAllocation() {
        Gdx.input.setInputProcessor(null);
        getContext().getNetworkManager().postTask(new NetworkTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().requestUpdateAllocation(room_config.team_allocation, room_config.player_type);
                return null;
            }

            @Override
            public void onFinish(Void result) {
                Gdx.input.setInputProcessor(NetGameCreateScreen.this);
            }

            @Override
            public void onFail(String message) {
                getContext().showMessage(message, null);
            }
        });
    }

    private void tryUpdateAlliance() {
        Gdx.input.setInputProcessor(null);
        getContext().getNetworkManager().postTask(new NetworkTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().requestUpdateAlliance(room_config.alliance_state);
                return null;
            }

            @Override
            public void onFinish(Void result) {
                Gdx.input.setInputProcessor(NetGameCreateScreen.this);
            }

            @Override
            public void onFail(String message) {
                getContext().showMessage(message, null);
            }
        });
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
            if (room_config.map.hasTeamAccess(team)) {
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

        lb_population.setText(" " + room_config.max_population + " ");
        lb_gold.setText(" " + room_config.initial_gold + " ");

        team_setting_table.clear();
        for (int team = 0; team < 4; team++) {
            if (room_config.map.hasTeamAccess(team)) {
                AEIIApplication.setButtonEnabled(btn_allocate[team], isHost());
                spinner_alliance[team].setEnabled(isHost());
                spinner_type[team].setSelectedIndex(0);
                spinner_type[team].setEnabled(isHost());

                team_setting_table.add(btn_allocate[team]).size(ts, ts).padTop(ts / 2);
                team_setting_table.add(team_image[team]).size(ts, ts).padTop(ts / 2).padLeft(ts / 2);
                team_setting_table.add(spinner_alliance[team]).size(ts * 3, ts).padTop(ts / 2).padLeft(ts / 2);
                team_setting_table.add(spinner_type[team]).size(ts * 4, ts).padTop(ts / 2).padLeft(ts / 2);
                team_setting_table.add(sp_username[team]).size(team_setting_table.getWidth() - ts * 11 - ts / 2, ts).padTop(ts / 2).padLeft(ts / 2).row();
            }
        }
        updateStatus();
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
    public void onAllocationUpdate(String[] allocation, Integer[] types) {
        room_config.team_allocation = allocation;
        room_config.player_type = types;
        updateStatus();
    }

    @Override
    public void onAllianceUpdate(Integer[] alliance) {
        room_config.alliance_state = alliance;
        updateStatus();
    }

    @Override
    public void onGameStart() {
        createGame();
    }

    private final SpinnerListener allocation_listener = new SpinnerListener() {
        @Override
        public void onValueChanged(Spinner spinner) {
            updateAllocation();
        }
    };

    private final SpinnerListener alliance_listener = new SpinnerListener() {
        @Override
        public void onValueChanged(Spinner spinner) {
            updateAlliance();
        }
    };

}
