package net.toyknight.aeii;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import net.toyknight.aeii.animation.AnimationManager;
import net.toyknight.aeii.campaign.CampaignContext;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.manager.GameManagerListener;
import net.toyknight.aeii.manager.RoomManager;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.ServerConfiguration;
import net.toyknight.aeii.record.GameRecordPlayer;
import net.toyknight.aeii.renderer.BorderRenderer;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.gui.*;
import net.toyknight.aeii.gui.wiki.Wiki;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.*;

public class GameContext extends Game implements GameManagerListener {

    public static final Object RENDER_LOCK = new Object();

    public static final String INTERNAL_VERSION = "26";
    public static final String EXTERNAL_VERSION = "1.3.0.1";
    public static final ServerConfiguration MAIN_SERVER = new ServerConfiguration("45.56.93.69", 5438, "main server");
    public static final ServerConfiguration TEST_SERVER = new ServerConfiguration("45.56.93.69", 5439, "test server");
    public static final ServerConfiguration MAP_SERVER = MAIN_SERVER;
    public static final ServerConfiguration CAMPAIGN_SERVER = MAIN_SERVER;
    private static final String TAG = "Main";

    private boolean initialized = false;

    private ThreadPoolExecutor executor;

    private Skin skin;

    private ObjectMap<String, String> configuration;

    private GameManager game_manager;

    private GameRecordPlayer record_player;

    private CampaignContext campaign_context;

    private RoomManager room_manager;

    private Wiki wiki;

    private MainMenuScreen main_menu_screen;
    private MapEditorScreen map_editor_screen;
    private LobbyScreen lobby_screen;
    private NetGameCreateScreen net_game_create_screen;
    private SkirmishGameCreateScreen skirmish_game_create_screen;
    private GameScreen game_screen;
    private StatisticsScreen statistics_screen;
    private MapManagementScreen map_management_screen;
    private CampaignScreen campaign_screen;

    public GameContext(Platform platform, int ts) {
        AER.ts = ts;
        AER.platform = platform;
    }

    @Override
    public void create() {
        try {
            executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            AER.initialize();

            LoadingScreen loading_screen = new LoadingScreen(this);
            Gdx.input.setCatchBackKey(true);
            setScreen(loading_screen);
        } catch (GameException ex) {
            Gdx.app.log(TAG, ex.toString() + "; Cause: " + ex.getCause().toString());
        }
    }

    public void initialize() {
        if (!initialized) {
            try {
                loadConfiguration();
                AER.audio.setSEVolume(getSEVolume());
                AER.audio.setMusicVolume(getMusicVolume());
                AER.resources.initialize();
                AER.font.initialize();

                CanvasRenderer.initialize();
                BorderRenderer.initialize();

                TileValidator.initialize();

                skin = AER.resources.getSkin();
                skin.get(TextButton.TextButtonStyle.class).font = AER.resources.getTextFont();
                skin.get(TextField.TextFieldStyle.class).font = AER.resources.getTextFont();
                skin.get(Label.LabelStyle.class).font = AER.resources.getTextFont();
                skin.get(Dialog.WindowStyle.class).titleFont = AER.resources.getTextFont();
                skin.get(List.ListStyle.class).font = AER.resources.getTextFont();

                game_manager = new GameManager(this, new AnimationManager());
                game_manager.getGameEventExecutor().setCheckEventValue(true);
                game_manager.setListener(this);

                room_manager = new RoomManager();

                main_menu_screen = new MainMenuScreen(this);
                map_editor_screen = new MapEditorScreen(this);
                lobby_screen = new LobbyScreen(this);
                net_game_create_screen = new NetGameCreateScreen(this);
                skirmish_game_create_screen = new SkirmishGameCreateScreen(this);
                game_screen = new GameScreen(this);
                statistics_screen = new StatisticsScreen(this);
                map_management_screen = new MapManagementScreen(this);
                campaign_screen = new CampaignScreen(this);
                wiki = new Wiki(main_menu_screen);

                record_player = new GameRecordPlayer(this);
                record_player.setListener(game_screen);

                campaign_context = new CampaignContext(this);

                initialized = true;
            } catch (GameException ex) {
                Gdx.app.log(TAG, ex.toString() + "; Cause: " + ex.getCause().toString());
            }
        }
    }

    public boolean initialized() {
        return initialized;
    }

    public Wiki getWiki() {
        return wiki;
    }

    private void loadConfiguration() throws GameException {
        FileHandle config_file = FileProvider.getUserFile("user.config");
        configuration = new ObjectMap<String, String>();
        try {
            if (config_file.exists() && !config_file.isDirectory()) {
                InputStreamReader reader = new InputStreamReader(config_file.read(), "UTF8");
                PropertiesUtils.load(configuration, reader);
            } else {
                configuration.put("username", "undefined");
                configuration.put("se_volume", "0.5");
                configuration.put("music_volume", "0.5");
                OutputStreamWriter writer = new OutputStreamWriter(config_file.write(false), "UTF8");
                PropertiesUtils.store(configuration, writer, "aeii user configuration file");
            }
        } catch (IOException ex) {
            throw new GameException(ex.getMessage());
        }
    }

    public void updateConfiguration(String key, String value) {
        configuration.put(key, value);
    }

    public void saveConfiguration() {
        FileHandle config_file = FileProvider.getUserFile("user.config");
        try {
            OutputStreamWriter writer = new OutputStreamWriter(config_file.write(false), "UTF8");
            PropertiesUtils.store(configuration, writer, "aeii user configure file");
        } catch (IOException ex) {
            Gdx.app.log(TAG, ex.toString());
        }
    }

    public int getTileSize() {
        return AER.ts;
    }

    public ObjectMap<String, String> getConfiguration() {
        return configuration;
    }

    public String getUsername() {
        return getConfiguration().get("username", "undefined");
    }

    public float getSEVolume() {
        return Float.parseFloat(configuration.get("se_volume", "0.5"));
    }

    public float getMusicVolume() {
        return Float.parseFloat(configuration.get("music_volume", "0.5"));
    }

    public int getCampaignProgress(String campaign_code) {
        if (getCampaignContext().getCampaign(campaign_code).isOpen()) {
            return getCampaignContext().getCampaign(campaign_code).getStages().size - 1;
        } else {
            if (getConfiguration().containsKey(campaign_code)) {
                return Integer.parseInt(getConfiguration().get(campaign_code));
            } else {
                updateConfiguration(campaign_code, Integer.toString(0));
                saveConfiguration();
                return 0;
            }
        }
    }

    public int getCampaignTurnRecord(String campaign_code, int stage_number) {
        try {
            String turn_key = campaign_code + "_" + stage_number + "_TURN";
            return getConfiguration().containsKey(turn_key) ? Integer.parseInt(getConfiguration().get(turn_key)) : -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public int getCampaignActionRecord(String campaign_code, int stage_number) {
        try {
            String action_key = campaign_code + "_" + stage_number + "_ACTION";
            return getConfiguration().containsKey(action_key) ? Integer.parseInt(getConfiguration().get(action_key)) : -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public void updateCampaignLocalRecord(String campaign_code, int stage_number, int turns, int actions) {
        String turn_key = campaign_code + "_" + stage_number + "_TURN";
        String action_key = campaign_code + "_" + stage_number + "_ACTION";
        boolean updated = false;
        int turn_record = getCampaignTurnRecord(campaign_code, stage_number);
        if (turn_record < 0 || turns < turn_record) {
            updateConfiguration(turn_key, Integer.toString(turns));
            updated = true;
        }
        int action_record = getCampaignActionRecord(campaign_code, stage_number);
        if (action_record < 0 || actions < action_record) {
            updateConfiguration(action_key, Integer.toString(actions));
            updated = true;
        }
        if (updated) {
            saveConfiguration();
        }
    }

    public String getVerificationString() {
        String V_STRING = AER.tiles.getVerificationString() + AER.units.getVerificationString() + INTERNAL_VERSION;
        return new MD5Converter().toMD5(V_STRING);
    }

    public Skin getSkin() {
        return skin;
    }

    public GameManager getGameManager() {
        return game_manager;
    }

    public GameRecordPlayer getRecordPlayer() {
        return record_player;
    }

    public CampaignContext getCampaignContext() {
        return campaign_context;
    }

    public RoomManager getRoomManager() {
        return room_manager;
    }

    public GameCore getGame() {
        return getGameManager().getGame();
    }

    public void gotoMainMenuScreen(boolean restart_bgm) {
        gotoMainMenuScreen(restart_bgm, false);
    }

    public void gotoMainMenuScreen(boolean restart_bgm, boolean show_announcement) {
        if (restart_bgm) {
            AER.audio.loopMainTheme();
        }
        gotoScreen(main_menu_screen);
        if (show_announcement) {
            main_menu_screen.showNotification("亲爱的玩家，请尽可能使用谷歌玩的官方版本。" +
                    "若有困难，欢迎加入官方交流群获取最新更新动态。\nQQ群号：7850187", null);
        }
    }

    public void gotoMapEditorScreen() {
        AER.audio.stopCurrentBGM();
        gotoScreen(map_editor_screen);
    }

    public void gotoGameScreen(GameCore game) {
        AER.audio.playRandomBGM("bg_good.mp3");
        NetworkManager.resetEventQueue();
        if (game.initialized()) {
            getGameManager().setGame(game);
        } else {
            game.initialize();
            getGameManager().setGame(game);
            int income = game.gainIncome(game.getCurrentTeam());
            if (game.getType() == GameCore.SKIRMISH) {
                getGameManager().getAnimationDispatcher().submitMessageAnimation(
                        AER.lang.getText("LB_CURRENT_TURN") + ": " + game.getCurrentTurn(),
                        AER.lang.getText("LB_INCOME") + ": " + income,
                        0.8f);
            }
        }
        gotoScreen(game_screen);
    }

    public void gotoGameScreen(String campaign_code, int stage) {
        try {
            getCampaignContext().setCurrentCampaign(campaign_code);
            getCampaignContext().setCurrentStage(stage);
            GameCore game = GameToolkit.createCampaignGame(getCampaignContext().getCurrentCampaign().getCurrentStage());
            gotoGameScreen(game);
            String stage_name = getCampaignContext().getCurrentCampaign().getCurrentStage().getStageName();
            getGameManager().getAnimationDispatcher().submitMessageAnimation(stage_name, 1f);
            getCampaignContext().onGameStart();
            getCampaignContext().getCurrentCampaign().getCurrentStage().getContext().show_objectives();
        } catch (GameException ex) {
            if (getScreen() instanceof StageScreen) {
                ((StageScreen) getScreen()).showNotification(AER.lang.getText("MSG_ERR_BMF"), null);
            }
        }
    }

    public void gotoLobbyScreen() {
        gotoScreen(lobby_screen);
    }

    public void gotoNetGameCreateScreen() {
        AER.audio.stopCurrentBGM();
        gotoScreen(net_game_create_screen);
    }

    public void gotoStatisticsScreen(GameCore game) {
        getRecordPlayer().reset();
        getGameManager().getGameRecorder().save();
        statistics_screen.setGame(game);
        gotoScreen(statistics_screen);
    }

    public void gotoSkirmishGameCreateScreen() {
        gotoScreen(skirmish_game_create_screen);
    }

    public void gotoMapManagementScreen() {
        gotoScreen(map_management_screen);
    }

    public void gotoCampaignScreen() {
        gotoScreen(campaign_screen);
    }

    public void gotoScreen(Screen screen) {
        if (screen instanceof MapCanvas) {
            CanvasRenderer.setCanvas((MapCanvas) screen);
        }
        this.setScreen(screen);
    }

    public void submitAsyncTask(AsyncTask task) {
        executor.submit(task);
    }

    public void clearAsyncTasks() {
        executor.getQueue().clear();
    }

    public void doSaveGame() throws GameException {
        GameCore game = getGame();
        switch (game.getType()) {
            case GameCore.SKIRMISH:
                GameToolkit.saveSkirmish(game);
                break;
            case GameCore.CAMPAIGN:
                String code = getCampaignContext().getCurrentCampaign().getCode();
                int stage = getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber();
                ObjectMap<String, Integer> attributes = getCampaignContext().getCurrentCampaign().getAttributes();
                GameToolkit.saveCampaign(game, code, stage, attributes);
                break;
        }
    }

    @Override
    public void onMapFocusRequired(int map_x, int map_y, boolean focus_viewport) {
        game_screen.focus(map_x, map_y, focus_viewport);
    }

    @Override
    public void onGameManagerStateChanged() {
        game_screen.update();
    }

    @Override
    public void onCampaignMessageSubmitted() {
        game_screen.showCampaignMessage();
    }

    @Override
    public void onCampaignObjectiveRequested() {
        game_screen.showObjectives();
    }

    @Override
    public void onGameOver() {
        if (getGame().getType() == GameCore.CAMPAIGN) {
            onCampaignOver();
        }
        if (getGame().getType() == GameCore.SKIRMISH) {
            gotoStatisticsScreen(getGame());
        }
    }

    public boolean onCampaignNextStage() {
        boolean has_next_stage = getCampaignContext().getCurrentCampaign().nextStage();
        if (has_next_stage) {
            String campaign_code = getCampaignContext().getCurrentCampaign().getCode();
            int stage_number = getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber();

            if (stage_number > getCampaignProgress(campaign_code)) {
                updateConfiguration(campaign_code, Integer.toString(stage_number));
                saveConfiguration();
            }
            return true;
        } else {
            return false;
        }
    }

    private void onCampaignOver() {
        if (getCampaignContext().getCurrentCampaign().getCurrentStage().isCleared()) {
            if (getGameManager().isRanking()) {
                updateCampaignLocalRecord(
                        getCampaignContext().getCurrentCampaign().getCode(),
                        getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber(),
                        getGame().getCurrentTurn(), getGame().getStatistics().getActions());
                game_screen.showRankingClear();
            } else {
                if (onCampaignNextStage()) {
                    gotoGameScreen(
                            getCampaignContext().getCurrentCampaign().getCode(),
                            getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber());
                }
            }
        } else {
            gotoCampaignScreen();
            AER.audio.loopMainTheme();
        }
    }

    @Override
    public void render() {
        synchronized (RENDER_LOCK) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            super.render();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AER.resources.dispose();
    }

    public static void setButtonEnabled(Button button, boolean enabled) {
        button.setDisabled(!enabled);
        if (enabled) {
            button.setTouchable(Touchable.enabled);
        } else {
            button.setTouchable(Touchable.disabled);
        }
    }

}
