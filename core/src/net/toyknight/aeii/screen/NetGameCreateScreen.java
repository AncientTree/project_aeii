package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.AudioManager;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.concurrent.MessageSendingTask;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Player;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.manager.RoomManager;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.screen.dialog.MiniMapDialog;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.screen.widgets.PlayerAllocationButton;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.screen.widgets.Spinner;
import net.toyknight.aeii.screen.widgets.SpinnerListener;
import net.toyknight.aeii.screen.widgets.StringList;

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

    private Label lb_population;
    private Label lb_gold;

    private Table team_setting_content;

    private Table message_pane;
    private ScrollPane sp_message;

    private StringList<PlayerSnapshot> player_list;

    private MiniMapDialog map_preview;

    public NetGameCreateScreen(GameContext context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        player_list = new StringList<PlayerSnapshot>(getContext(), ts);
        ScrollPane sp_player_list = new ScrollPane(player_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        getResources().getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_player_list.getStyle().background = ResourceManager.createDrawable(getResources().getListBackground());
        sp_player_list.setScrollingDisabled(true, false);
        sp_player_list.setBounds(ts / 2, ts * 3 + ts / 2, ts * 3, Gdx.graphics.getHeight() - ts * 4);
        addActor(sp_player_list);

        btn_start = new TextButton(Language.getText("LB_START"), getContext().getSkin());
        btn_start.setBounds(ts / 2, ts / 2, ts * 3, ts);
        btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryStartGame();
            }
        });
        addActor(btn_start);

        TextButton btn_message = new TextButton(Language.getText("LB_SEND_MESSAGE"), getContext().getSkin());
        btn_message.setBounds(Gdx.graphics.getWidth() - ts * 3 - ts / 2, ts / 2, ts * 3, ts);
        btn_message.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage();
            }
        });
        addActor(btn_message);

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
        btn_preview.setBounds(ts / 2, ts * 2, ts * 3, ts);
        btn_preview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialog("preview");
            }
        });
        addActor(btn_preview);

        btn_record = new TextButton("", getContext().getSkin());
        btn_record.setBounds(Gdx.graphics.getWidth() - ts * 3 - ts / 2, ts * 2, ts * 3, ts);
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

        Table team_setting_pane = new Table();
        team_setting_pane.setBounds(
                ts * 4, ts * 3 + ts / 2, Gdx.graphics.getWidth() - ts * 4, Gdx.graphics.getHeight() - ts * 3 - ts / 2);
        addActor(team_setting_pane);

        Table team_setting_header = new Table();
        Label label_space = new Label("", getContext().getSkin());
        team_setting_header.add(label_space).width(ts * 2 + ts / 2);
        Label label_team = new Label(Language.getText("LB_TEAM"), getContext().getSkin());
        label_team.setAlignment(Align.center);
        team_setting_header.add(label_team).width(ts * 3).padLeft(ts / 2);
        Label label_type = new Label(Language.getText("LB_TYPE"), getContext().getSkin());
        label_type.setAlignment(Align.center);
        team_setting_header.add(label_type).width(ts * 4).padLeft(ts / 2);
        Label label_username = new Label(Language.getText("LB_USERNAME"), getContext().getSkin());
        label_username.setAlignment(Align.center);
        team_setting_header.add(label_username).width(Gdx.graphics.getWidth() - ts * 14 - ts / 2);

        team_setting_pane.add(team_setting_header).width(Gdx.graphics.getWidth() - ts * 4).padBottom(ts / 8).row();

        team_setting_content = new Table();
        team_setting_pane.add(team_setting_content).width(Gdx.graphics.getWidth() - ts * 4);


        btn_allocate = new TextButton[4];
        team_image = new Image[4];
        spinner_alliance = new Spinner[4];
        spinner_type = new Spinner[4];
        lb_username = new Label[4];
        for (int team = 0; team < 4; team++) {
            btn_allocate[team] = new PlayerAllocationButton(team, getContext().getSkin());
            btn_allocate[team].addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    int team = ((PlayerAllocationButton) event.getListenerActor()).getTeam();
                    allocateSelectedPlayer(team);
                }
            });

            TextureRegionDrawable team_color =
                    new TextureRegionDrawable(new TextureRegion(getResources().getTeamBackground(team)));
            team_color.setMinWidth(ts);
            team_color.setMinHeight(ts);
            team_image[team] = new Image(team_color);

            Integer[] alliance = new Integer[]{1, 2, 3, 4};
            spinner_alliance[team] = new Spinner<Integer>(getContext());
            spinner_alliance[team].setListener(state_change_listener);
            spinner_alliance[team].setItems(alliance);

            String[] type = new String[]{
                    Language.getText("LB_NONE"), Language.getText("LB_PLAYER"), Language.getText("LB_ROBOT")};
            spinner_type[team] = new Spinner<String>(getContext());
            spinner_type[team].setListener(state_change_listener);
            spinner_type[team].setContentWidth(ts * 2);
            spinner_type[team].setItems(type);

            lb_username[team] = new Label("", getContext().getSkin());
            lb_username[team].setAlignment(Align.center);
        }


        HorizontalGroup info_group = new HorizontalGroup();
        info_group.setBounds(ts * 4, ts * 2, ts * 3, ts);

        TextureRegion tr_population = getResources().getStatusHudIcon(0);
        TextureRegionDrawable trd_population = new TextureRegionDrawable(tr_population);
        trd_population.setMinWidth(ts / 24 * 11);
        trd_population.setMinHeight(ts / 24 * 11);
        Image ico_population = new Image(trd_population);
        info_group.addActor(ico_population);
        lb_population = new Label("", getContext().getSkin());
        info_group.addActor(lb_population);

        TextureRegion tr_gold = getResources().getStatusHudIcon(1);
        TextureRegionDrawable trd_gold = new TextureRegionDrawable(tr_gold);
        trd_gold.setMinWidth(ts / 24 * 11);
        trd_gold.setMinHeight(ts / 24 * 11);
        Image ico_gold = new Image(trd_gold);
        info_group.addActor(ico_gold);
        lb_gold = new Label("", getContext().getSkin());
        info_group.addActor(lb_gold);

        addActor(info_group);

        message_pane = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getListBackground(), getX(), getY(), getWidth(), getHeight());
                super.draw(batch, parentAlpha);
            }
        };
        message_pane.top();
        sp_message = new ScrollPane(message_pane, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        getResources().getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_message.setScrollingDisabled(true, false);
        sp_message.getStyle().background = ResourceManager.createDrawable(getResources().getListBackground());
        sp_message.setBounds(ts * 7 + ts / 2, ts / 2, Gdx.graphics.getWidth() - ts * 11 - ts / 2, ts * 2 + ts / 2);
        addActor(sp_message);
    }

    public RoomManager getRoomManager() {
        return getContext().getRoomManager();
    }

    public GameCore getGame() {
        return getRoomManager().getGame();
    }

    public void sendMessage() {
        if (Language.getLocale().equals("zh_CN")) {
            Gdx.input.getTextInput(message_input_listener, Language.getText("MSG_INFO_IM"), "", "");
        } else {
            showInput(Language.getText("MSG_INFO_IM"), 64, false, message_input_listener);
        }
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

    private void trySendMessage(String message) {
        if (message.length() > 0) {
            getContext().submitAsyncTask(new MessageSendingTask(message) {
                @Override
                public void onFinish(Void result) {
                }

                @Override
                public void onFail(String message) {
                }
            });
        }
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

    private void appendMessage(String username, String message) {
        String content = username == null ? ">" + message : ">" + username + ": " + message;
        Label label_message = new Label(content, getContext().getSkin());
        label_message.setWrap(true);
        message_pane.add(label_message).width(message_pane.getWidth()).padTop(ts / 12).row();
        message_pane.layout();
        sp_message.layout();
        sp_message.setScrollPercentY(1.0f);
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (keyCode == Input.Keys.ENTER) {
            sendMessage();
            return true;
        } else {
            return false;
        }
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
        batch.draw(getResources().getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getBorderRenderer().drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        super.show();

        record_on = false;

        message_pane.clearChildren();

        map_preview.setMap(getGame().getMap());
        map_preview.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        lb_population.setText(" " + getRoomManager().getMaxPopulation() + " ");
        if (getRoomManager().getStartGold() >= 0) {
            lb_gold.setText(" " + getRoomManager().getStartGold() + " ");
        } else {
            lb_gold.setText(" - ");
        }

        boolean empty = true;
        team_setting_content.clear();
        for (int team = 0; team < 4; team++) {
            if (getGame().getMap().hasTeamAccess(team)) {
                int pad_top = empty ? 0 : ts / 2;
                team_setting_content.add(btn_allocate[team])
                        .size(ts, ts)
                        .padTop(pad_top);
                team_setting_content.add(team_image[team])
                        .size(ts, ts)
                        .padTop(pad_top)
                        .padLeft(ts / 2);
                team_setting_content.add(spinner_alliance[team])
                        .size(ts * 3, ts)
                        .padTop(pad_top)
                        .padLeft(ts / 2);
                team_setting_content.add(spinner_type[team])
                        .size(ts * 4, ts)
                        .padTop(pad_top)
                        .padLeft(ts / 2);
                team_setting_content.add(lb_username[team])
                        .size(Gdx.graphics.getWidth() - ts * 14 - ts / 2, ts)
                        .padTop(pad_top)
                        .row();
                if (empty) {
                    empty = false;
                }
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
        appendMessage(Language.getText("LB_SYSTEM"), String.format(Language.getText("MSG_INFO_PJR"), username));
        AudioManager.playSE("prompt.mp3");
    }

    @Override
    public void onPlayerLeave(int id, String username, int host) {
        super.onPlayerLeave(id, username, host);
        updatePlayers();
        updateButtons();
        appendMessage(Language.getText("LB_SYSTEM"), String.format(Language.getText("MSG_INFO_PLR"), username));
        AudioManager.playSE("prompt.mp3");
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

    @Override
    public void onReceiveMessage(String username, String message) {
        appendMessage(username, message);
    }

    private final SpinnerListener state_change_listener = new SpinnerListener() {
        @Override
        public void onValueChanged(Spinner spinner) {
            onStateChange();
        }
    };

    private Input.TextInputListener message_input_listener = new Input.TextInputListener() {
        @Override
        public void input(String message) {
            trySendMessage(message);
        }

        @Override
        public void canceled() {
            //do nothing
        }
    };

}
