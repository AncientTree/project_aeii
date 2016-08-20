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
import net.toyknight.aeii.record.GameRecordPlayer;
import net.toyknight.aeii.renderer.BorderRenderer;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.renderer.FontRenderer;
import net.toyknight.aeii.screen.*;
import net.toyknight.aeii.screen.wiki.Wiki;
import net.toyknight.aeii.utils.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameContext extends Game implements GameManagerListener {

    public static final Object RENDER_LOCK = new Object();

    public static final String INTERNAL_VERSION = "20";
    public static final String EXTERNAL_VERSION = "1.1.8";
    private static final String TAG = "Main";

    private final int TILE_SIZE;
    private final Platform PLATFORM;

    private boolean initialized = false;

    private ExecutorService executor;

    private Skin skin;

    private ObjectMap<String, String> configuration;

    private ResourceManager resource_manager;

    private GameManager game_manager;

    private GameRecordPlayer record_player;

    private CampaignContext campaign_context;

    private RoomManager room_manager;

    private Wiki wiki;

    private FontRenderer font_renderer;

    private CanvasRenderer canvas_renderer;

    private BorderRenderer border_renderer;

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
        this.TILE_SIZE = ts;
        this.PLATFORM = platform;
    }

    @Override
    public void create() {
        try {
            executor = Executors.newSingleThreadExecutor();
            FileProvider.setPlatform(PLATFORM);
            Language.initialize();
            TileFactory.loadTileData();
            UnitFactory.loadUnitData();
            resource_manager = new ResourceManager();
            resource_manager.prepare(TILE_SIZE);

            LoadingScreen loading_screen = new LoadingScreen(this);
            Gdx.input.setCatchBackKey(true);
            setScreen(loading_screen);
        } catch (AEIIException ex) {
            Gdx.app.log(TAG, ex.toString() + "; Cause: " + ex.getCause().toString());
        }
    }

    public void initialize() {
        if (!initialized) {
            try {
                loadConfiguration();
                AudioManager.setSEVolume(getSEVolume());
                AudioManager.setMusicVolume(getMusicVolume());
                resource_manager.initialize();
                TileValidator.initialize();

                skin = getResources().getSkin();
                skin.get(TextButton.TextButtonStyle.class).font = getResources().getTextFont();
                skin.get(TextField.TextFieldStyle.class).font = getResources().getTextFont();
                skin.get(Label.LabelStyle.class).font = getResources().getTextFont();
                skin.get(Dialog.WindowStyle.class).titleFont = getResources().getTextFont();
                skin.get(List.ListStyle.class).font = getResources().getTextFont();

                font_renderer = new FontRenderer(this);
                canvas_renderer = new CanvasRenderer(this);
                border_renderer = new BorderRenderer(this);

                game_manager = new GameManager(this, new AnimationManager(this));
                game_manager.getGameEventExecutor().setCheckEventValue(true);
                game_manager.setListener(this);

                room_manager = new RoomManager();

                StageScreen.initializePrompt(getSkin(), TILE_SIZE);
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
            } catch (AEIIException ex) {
                Gdx.app.log(TAG, ex.toString() + "; Cause: " + ex.getCause().toString());
            }
        }
    }


    public boolean initialized() {
        return initialized;
    }

    public ResourceManager getResources() {
        return resource_manager;
    }

    public FontRenderer getFontRenderer() {
        return font_renderer;
    }

    public CanvasRenderer getCanvasRenderer() {
        return canvas_renderer;
    }

    public BorderRenderer getBorderRenderer() {
        return border_renderer;
    }

    public Wiki getWiki() {
        return wiki;
    }

    private void loadConfiguration() throws AEIIException {
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
            throw new AEIIException(ex.getMessage());
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
        return TILE_SIZE;
    }

    public Platform getPlatform() {
        return PLATFORM;
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
        if (campaign_code.equals("C_CH")) {
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

    public String getVerificationString() {
        String V_STRING = TileFactory.getVerificationString() + UnitFactory.getVerificationString() + INTERNAL_VERSION;
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
        if (restart_bgm) {
            AudioManager.loopMainTheme();
        }
        gotoScreen(main_menu_screen);
    }

    public void gotoMapEditorScreen() {
        AudioManager.stopCurrentBGM();
        gotoScreen(map_editor_screen);
    }

    public void gotoGameScreen(GameCore game) {
        AudioManager.playRandomBGM("bg_good.mp3");
        NetworkManager.resetEventQueue();
        if (game.initialized()) {
            getGameManager().setGame(game);
        } else {
            game.initialize();
            getGameManager().setGame(game);
            int income = game.gainIncome(game.getCurrentTeam());
            if (game.getType() == GameCore.SKIRMISH) {
                getGameManager().getAnimationDispatcher().submitMessageAnimation(
                        Language.getText("LB_CURRENT_TURN") + ": " + game.getCurrentTurn(),
                        Language.getText("LB_INCOME") + ": " + income,
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
        } catch (AEIIException ex) {
            if (getScreen() instanceof StageScreen) {
                ((StageScreen) getScreen()).showPrompt(Language.getText("MSG_ERR_BMF"), null);
            }
        }
    }

    public void gotoLobbyScreen() {
        gotoScreen(lobby_screen);
    }

    public void gotoNetGameCreateScreen() {
        AudioManager.stopCurrentBGM();
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
            getCanvasRenderer().setCanvas((MapCanvas) screen);
        }
        this.setScreen(screen);
    }

    public void submitAsyncTask(AsyncTask task) {
        executor.submit(task);
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
            if (getCampaignContext().getCurrentCampaign().getCurrentStage().isCleared()) {
                boolean has_next_stage = getCampaignContext().getCurrentCampaign().nextStage();
                if (has_next_stage) {
                    String campaign_code = getCampaignContext().getCurrentCampaign().getCode();
                    int stage_number = getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber();

                    if (stage_number > getCampaignProgress(campaign_code)) {
                        updateConfiguration(campaign_code, Integer.toString(stage_number));
                        saveConfiguration();
                    }
                    gotoGameScreen(campaign_code, stage_number);
                } else {
                    gotoCampaignScreen();
                }
            } else {
                gotoCampaignScreen();
            }
            AudioManager.loopMainTheme();
        }
        if (getGame().getType() == GameCore.SKIRMISH) {
            gotoStatisticsScreen(getGame());
        }
    }

    @Override
    public void render() {
        synchronized (RENDER_LOCK) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            if (getScreen() instanceof StageScreen) {
                ((StageScreen) getScreen()).updateFocus();
            }
            super.render();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        getResources().dispose();
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
