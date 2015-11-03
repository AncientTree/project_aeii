package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.Callable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.dialog.MiniMapDialog;
import com.toyknight.aeii.screen.widgets.PlayerAllocationButton;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.SpinnerListener;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.serializable.PlayerSnapshot;
import com.toyknight.aeii.serializable.RoomConfiguration;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.record.Recorder;

/**
 * @author toyknight 8/28/2015.
 */
public class NetGameCreateScreen extends StageScreen {

    private boolean record_on;
    private GameSave game_save;
    private RoomConfiguration configuration;

    private TextButton btn_start;
    private TextButton btn_leave;
    private TextButton btn_record;

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

    public NetGameCreateScreen(GameContext context) {
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

        btn_record = new TextButton("", getContext().getSkin());
        btn_record.setBounds(ts * 11, ts / 2, ts * 3, ts);
        btn_record.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                record_on = !record_on;
                updateRecordButton();
            }
        });
        addActor(btn_record);

        map_preview = new MiniMapDialog(this);
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

    public void setGameSave(GameSave game_save) {
        this.game_save = game_save;
    }

    public void setRoomConfiguration(RoomConfiguration configuration) {
        this.configuration = configuration;
        player_list.setItems(getPlayers());
        player_list.setSelectedIndex(0);
    }

    public RoomConfiguration getRoomConfiguration() {
        return configuration;
    }

    public Array<PlayerSnapshot> getPlayers() {
        return configuration.players;
    }

    public String getUsername(int id) {
        for (PlayerSnapshot player : getPlayers()) {
            if (player.id == id) {
                return player.username;
            }
        }
        return "";
    }

    public Integer[] getPlayerTypes() {
        Integer[] player_type = new Integer[4];
        for (int team = 0; team < 4; team++) {
            player_type[team] = configuration.game.getPlayer(team).getType();
        }
        return player_type;
    }

    public Integer[] getAllianceState() {
        Integer[] alliance = new Integer[4];
        for (int team = 0; team < 4; team++) {
            alliance[team] = configuration.game.getPlayer(team).getAlliance();
        }
        return alliance;
    }

    public boolean hasTeamAccess(int team) {
        return configuration.game.getMap().hasTeamAccess(team)
                && getContext().getNetworkManager().getServiceID() == configuration.team_allocation[team];
    }

    public void updateStatus() {
        for (int team = 0; team < 4; team++) {
            int player_id = getRoomConfiguration().team_allocation[team];
            if (configuration.game.getMap().hasTeamAccess(team)) {
                if (player_id == -1) {
                    spinner_type[team].setSelectedIndex(0);
                    lb_username[team].setText("");
                } else {
                    spinner_type[team].setSelectedIndex(1);
                    lb_username[team].setText(getUsername(getRoomConfiguration().team_allocation[team]));
                }
                Player player = configuration.game.getPlayer(team);
                spinner_alliance[team].setSelectedIndex(player.getAlliance() - 1);
            }
        }
    }

    public void updateAllocation() {
        for (int team = 0; team < 4; team++) {
            if (configuration.game.getMap().hasTeamAccess(team)) {
                String selected = spinner_type[team].getSelectedItem();
                if (selected.equals(Language.getText("LB_NONE"))) {
                    configuration.team_allocation[team] = -1;
                }
                if (selected.equals(Language.getText("LB_PLAYER"))) {
                    if (configuration.team_allocation[team] == -1) {
                        configuration.team_allocation[team] = getContext().getNetworkManager().getServiceID();
                        configuration.game.getPlayer(team).setType(Player.LOCAL);
                    }
                }
            }
        }
        updateStatus();
        tryUpdateAllocation();
    }

    public void updateAlliance() {
        for (int team = 0; team < 4; team++) {
            if (configuration.game.getMap().hasTeamAccess(team)) {
                int alliance = spinner_alliance[team].getSelectedItem();
                configuration.game.getPlayer(team).setAlliance(alliance);
            }
        }
        updateStatus();
        tryUpdateAlliance();
    }

    public void allocateSelectedPlayer(int team) {
        int id = player_list.getSelected().id;
        configuration.team_allocation[team] = id;
        configuration.game.getPlayer(team).setType(Player.LOCAL);
        updateStatus();
        tryUpdateAllocation();
    }

    private void tryUpdateAllocation() {
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().notifyAllocationUpdate(configuration.team_allocation, getPlayerTypes());
                return null;
            }

            @Override
            public void onFinish(Void result) {
            }

            @Override
            public void onFail(String message) {
                getContext().showMessage(message, null);
            }
        });
    }

    private void tryUpdateAlliance() {
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().notifyAllianceUpdate(getAllianceState());
                return null;
            }

            @Override
            public void onFinish(Void result) {
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
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().notifyLeaveRoom();
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
        getContext().submitAsyncTask(new AsyncTask<Boolean>() {
            @Override
            public Boolean doTask() throws Exception {
                if (game_save == null) {
                    return getContext().getNetworkManager().requestStartGame();
                } else {
                    return getContext().getNetworkManager().requestStartGame(game_save);
                }
            }

            @Override
            public void onFinish(Boolean success) {
                if (success) {
                    btn_start.setText(Language.getText("LB_START"));
                    createGame(game_save);
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

    private void createGame(GameSave game_save) {
        if (game_save == null) {
            Recorder.setRecord(record_on);
            for (int team = 0; team < 4; team++) {
                if (hasTeamAccess(team)) {
                    configuration.game.getPlayer(team).setType(Player.LOCAL);
                } else {
                    if (configuration.game.getPlayer(team).getType() != Player.NONE) {
                        configuration.game.getPlayer(team).setType(Player.REMOTE);
                    }
                }
            }
            getContext().gotoGameScreen(configuration.game, true);
//            Player[] players = new Player[4];
//            for (int team = 0; team < 4; team++) {
//                if (configuration.game.getMap().hasTeamAccess(team) && configuration.team_allocation[team] != -1) {
//                    players[team] = new Player();
//                    players[team].setAlliance(configuration.alliance_state[team]);
//                    players[team].setGold(configuration.initial_gold);
//                    if (getContext().getNetworkManager().getServiceID().equals(configuration.team_allocation[team])) {
//                        players[team].setType(configuration.player_type[team]);
//                    } else {
//                        players[team].setType(Player.REMOTE);
//                    }
//                }
//            }
//            Rule rule = Rule.getDefaultRule();
//            rule.setMaxPopulation(configuration.max_population);
//            GameCore game = new GameCore(configuration.game.getMap(), rule, GameCore.SKIRMISH, players);
//            getContext().gotoGameScreen(game);
        } else {
//            GameCore game = game_save.game;
//            for (int team = 0; team < 4; team++) {
//                if (game.getPlayer(team) != null) {
//                    if (configuration.team_allocation[team].equals("NONE")) {
//                        game.removePlayer(team);
//                    } else {
//                        if (getContext().getNetworkManager().getServiceID().equals(configuration.team_allocation[team])) {
//                            game.getPlayer(team).setType(Player.LOCAL);
//                        } else {
//                            game.getPlayer(team).setType(Player.REMOTE);
//                        }
//                    }
//                }
//            }
//            getContext().gotoGameScreen(game_save);
        }
    }

    private void updateRecordButton() {
        if (record_on) {
            btn_record.setText(Language.getText("LB_RECORD") + ":" + Language.getText("LB_ON"));
        } else {
            btn_record.setText(Language.getText("LB_RECORD") + ":" + Language.getText("LB_OFF"));
        }
    }

    private boolean isHost() {
        return getContext().getNetworkManager().getServiceID() == configuration.host;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        map_preview.update(delta);
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
        map_preview.setMap(configuration.game.getMap());
        map_preview.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        lb_population.setText(" " + configuration.max_population + " ");
        if (configuration.initial_gold >= 0) {
            lb_gold.setText(" " + configuration.initial_gold + " ");
        } else {
            lb_gold.setText(" - ");
        }

        team_setting_table.clear();
        for (int team = 0; team < 4; team++) {
            if (configuration.game.getMap().hasTeamAccess(team)) {
                GameContext.setButtonEnabled(btn_allocate[team], isHost());
                spinner_alliance[team].setEnabled(isHost() && game_save == null);
                spinner_type[team].setSelectedIndex(0);
                spinner_type[team].setEnabled(isHost());

                team_setting_table.add(btn_allocate[team]).size(ts, ts).padTop(ts / 2);
                team_setting_table.add(team_image[team]).size(ts, ts).padTop(ts / 2).padLeft(ts / 2);
                team_setting_table.add(spinner_alliance[team]).size(ts * 3, ts).padTop(ts / 2).padLeft(ts / 2);
                team_setting_table.add(spinner_type[team]).size(ts * 4, ts).padTop(ts / 2).padLeft(ts / 2);
                team_setting_table.add(sp_username[team]).size(team_setting_table.getWidth() - ts * 11 - ts / 2, ts).padTop(ts / 2).padLeft(ts / 2).row();
            }
        }
        if (game_save != null && isHost()) {
            for (int team = 0; team < 4; team++) {
                if (game_save.game.getPlayer(team) != null) {
                    int alliance = game_save.game.getPlayer(team).getAlliance();
                    configuration.game.getPlayer(team).setAlliance(alliance);
                }
            }
            tryUpdateAlliance();
        }
        updateStatus();

        record_on = false;
        updateRecordButton();
    }

    @Override
    public void onPlayerJoin(int id, String username) {
        PlayerSnapshot snapshot = new PlayerSnapshot();
        snapshot.id = id;
        snapshot.username = username;
        snapshot.is_host = false;
        getPlayers().add(snapshot);
        player_list.setItems(getPlayers());
        //show in message box
    }

    @Override
    public void onPlayerLeave(int id, String username) {
        if (configuration.host == id) {
            getContext().showMessage(Language.getText("MSG_ERR_HPD"), new Callable() {
                @Override
                public void call() {
                    tryLeaveRoom();
                }
            });
        } else {
            for (int i = 0; i < getPlayers().size; i++) {
                if (id == getPlayers().get(i).id) {
                    getPlayers().removeIndex(i);
                    break;
                }
            }
            player_list.setItems(getPlayers());
            //show in message box
        }
    }

    @Override
    public void onAllocationUpdate(Integer[] allocation, Integer[] types) {
        configuration.team_allocation = allocation;
        for (int team = 0; team < 4; team++) {
            configuration.game.getPlayer(team).setType(types[team]);
        }
        updateStatus();
    }

    @Override
    public void onAllianceUpdate(Integer[] alliance) {
        for (int team = 0; team < 4; team++) {
            configuration.game.getPlayer(team).setAlliance(alliance[team]);
        }
        updateStatus();
    }

    @Override
    public void onGameStart(GameSave game_save) {
        createGame(game_save);
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
