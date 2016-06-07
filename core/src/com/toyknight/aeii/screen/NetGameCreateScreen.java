package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.concurrent.AsyncTask;
import com.toyknight.aeii.manager.RoomManager;
import com.toyknight.aeii.network.NetworkManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.dialog.MiniMapDialog;
import com.toyknight.aeii.screen.widgets.PlayerAllocationButton;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.SpinnerListener;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.network.entity.PlayerSnapshot;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/28/2015.
 */
public class NetGameCreateScreen extends StageScreen {

    private boolean record_on;

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

    private Table team_setting_pane;

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
                updateButtons();
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
        team_setting_pane = new Table();
        team_setting_pane.setBounds(
                ts * 4, Gdx.graphics.getHeight() - ts / 2 - lb_team.getPrefHeight() - tsth, tstw, tsth);
        addActor(team_setting_pane);

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
            spinner_alliance[team].setListener(state_change_listener);
            spinner_alliance[team].setItems(alliance);

            String[] type = new String[]{
                    Language.getText("LB_NONE"), Language.getText("LB_PLAYER"), Language.getText("LB_ROBOT")};
            spinner_type[team] = new Spinner<String>(ts, getContext().getSkin());
            spinner_type[team].setListener(state_change_listener);
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

    public RoomManager getRoomManager() {
        return getContext().getRoomManager();
    }

    public GameCore getGame() {
        return getRoomManager().getGame();
    }

    public void updateView() {
        for (int team = 0; team < 4; team++) {
            int player_id = getRoomManager().getAllocation(team);
            if (getGame().getMap().hasTeamAccess(team)) {
                if (player_id == -1) {
                    lb_username[team].setText("");
                    spinner_type[team].setSelectedIndex(0);
                } else {
                    if (getGame().getPlayer(team).getType() == Player.LOCAL) {

                        lb_username[team].setText(getRoomManager().getUsername(player_id));
                        spinner_type[team].setSelectedIndex(1);
                    }
                    if (getGame().getPlayer(team).getType() == Player.ROBOT) {
                        lb_username[team].setText("");
                        spinner_type[team].setSelectedIndex(2);
                    }
                }
                spinner_alliance[team].setSelectedIndex(getGame().getPlayer(team).getAlliance() - 1);
            }
        }
    }

    public void onStateChange() {
        for (int team = 0; team < 4; team++) {
            if (getGame().getMap().hasTeamAccess(team)) {
                String selected = spinner_type[team].getSelectedItem();
                if (selected.equals(Language.getText("LB_NONE"))) {
                    getRoomManager().updatePlayerType(team, Player.NONE);
                }
                if (selected.equals(Language.getText("LB_PLAYER"))) {
                    getRoomManager().updatePlayerType(team, Player.LOCAL);
                }
                if (selected.equals(Language.getText("LB_ROBOT"))) {
                    getRoomManager().updatePlayerType(team, Player.ROBOT);
                }
                int alliance = spinner_alliance[team].getSelectedItem();
                getRoomManager().updateAlliance(team, alliance);
            }
        }
        trySubmitUpdates();
        updateView();
    }

    public void allocateSelectedPlayer(int team) {
        int player_id = player_list.getSelected().id;
        getRoomManager().updateAllocation(team, player_id);
        trySubmitUpdates();
        updateView();
    }

    public void trySubmitUpdates() {
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                getRoomManager().trySubmitUpdates();
                return null;
            }

            @Override
            public void onFinish(Void result) {
            }

            @Override
            public void onFail(String message) {
                showPrompt(message, null);
            }
        });
    }

    public void updatePlayers() {
        player_list.setItems(getRoomManager().getPlayers());
    }

    public void updateButtons() {
        for (int team = 0; team < 4; team++) {
            if (getGame().getMap().hasTeamAccess(team)) {
                GameContext.setButtonEnabled(btn_allocate[team], getRoomManager().isHost());
                spinner_alliance[team].setEnabled(getRoomManager().isHost());
                spinner_type[team].setEnabled(getRoomManager().isHost());
            }
        }
        if (record_on) {
            btn_record.setText(Language.getText("LB_RECORD") + ":" + Language.getText("LB_ON"));
        } else {
            btn_record.setText(Language.getText("LB_RECORD") + ":" + Language.getText("LB_OFF"));
        }
    }

    private void tryLeaveRoom() {
        Gdx.input.setInputProcessor(null);
        btn_leave.setText(Language.getText("LB_LEAVING"));
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                NetworkManager.notifyLeaveRoom();
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
                showPrompt(message, null);
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
                return NetworkManager.requestStartGame();
            }

            @Override
            public void onFinish(Boolean success) {
                btn_start.setText(Language.getText("LB_START"));
                if (success) {
                    createGame();
                } else {
                    showPrompt(Language.getText("MSG_ERR_CNSG"), null);
                }
            }

            @Override
            public void onFail(String message) {
                btn_start.setText(Language.getText("LB_START"));
                showPrompt(message, null);
            }
        });
    }

    private void createGame() {
        getContext().getGameManager().getGameRecorder().setEnabled(record_on);
        getContext().gotoGameScreen(getRoomManager().getArrangedGame());
        getRoomManager().setStarted(true);
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
        super.show();

        record_on = false;

        map_preview.setMap(getGame().getMap());
        map_preview.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        lb_population.setText(" " + getRoomManager().getMaxPopulation() + " ");
        if (getRoomManager().getStartGold() >= 0) {
            lb_gold.setText(" " + getRoomManager().getStartGold() + " ");
        } else {
            lb_gold.setText(" - ");
        }

        team_setting_pane.clear();
        for (int team = 0; team < 4; team++) {
            if (getGame().getMap().hasTeamAccess(team)) {
                team_setting_pane.add(btn_allocate[team])
                        .size(ts, ts)
                        .padTop(ts / 2);
                team_setting_pane.add(team_image[team])
                        .size(ts, ts)
                        .padTop(ts / 2)
                        .padLeft(ts / 2);
                team_setting_pane.add(spinner_alliance[team])
                        .size(ts * 3, ts)
                        .padTop(ts / 2)
                        .padLeft(ts / 2);
                team_setting_pane.add(spinner_type[team])
                        .size(ts * 4, ts)
                        .padTop(ts / 2)
                        .padLeft(ts / 2);
                team_setting_pane.add(sp_username[team])
                        .size(team_setting_pane.getWidth() - ts * 11 - ts / 2, ts)
                        .padTop(ts / 2)
                        .padLeft(ts / 2)
                        .row();
            }
            spinner_type[team].setSelectedIndex(0);
        }
        player_list.setItems(getRoomManager().getPlayers());
        player_list.setSelectedIndex(0);
        updateView();
        updateButtons();
    }

    @Override
    public void onPlayerJoin(int id, String username) {
        super.onPlayerJoin(id, username);
        updatePlayers();
        //show in message box
    }

    @Override
    public void onPlayerLeave(int id, String username, int host) {
        super.onPlayerLeave(id, username, host);
        updatePlayers();
        updateButtons();
        //show in message box
    }

    @Override
    public void onAllocationUpdate(int[] alliance, int[] allocation, int[] types) {
        super.onAllocationUpdate(alliance, allocation, types);
        updateView();
    }

    @Override
    public void onGameStart() {
        createGame();
    }

    private final SpinnerListener state_change_listener = new SpinnerListener() {
        @Override
        public void onValueChanged(Spinner spinner) {
            onStateChange();
        }
    };

}
