package com.toyknight.aeii;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.GameRecord;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.net.NetworkManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.screen.*;
import com.toyknight.aeii.script.JavaScriptEngine;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.serializable.RoomConfig;
import com.toyknight.aeii.utils.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AEIIApplet extends Game {

    public static final Object RENDER_LOCK = new Object();

    private static final String VERSION_MODIFIER = "c";
    private static final String VERSION = "1.0.7";
    private static final String TAG = "Main";

    private final int TILE_SIZE;
    private final Platform PLATFORM;

    private ExecutorService executor;

    private JavaScriptEngine script_engine;

    private Skin skin;

    private ObjectMap<String, String> configuration;

    private Screen previous_screen;

    private MainMenuScreen main_menu_screen;
    private MapEditorScreen map_editor_screen;
    private LobbyScreen lobby_screen;
    private NetGameCreateScreen net_game_create_screen;
    private TestScreen test_screen;
    private GameScreen game_screen;
    private StatisticsScreen statistics_screen;

    private Stage dialog_layer;
    private Dialog dialog;
    private DialogCallback dialog_callback;
    private TextButton btn_ok;

    private NetworkManager network_manager;

    public AEIIApplet(Platform platform, int ts) {
        this.TILE_SIZE = ts;
        this.PLATFORM = platform;
    }

    @Override
    public void create() {
        try {
            executor = Executors.newSingleThreadExecutor();

            FileProvider.setPlatform(PLATFORM);
            Language.init();
            loadConfiguration();
            TileFactory.loadTileData();
            UnitFactory.loadUnitData();
            ResourceManager.loadResources();
            FontRenderer.loadFonts(TILE_SIZE);
            BorderRenderer.init();
            Animator.setTileSize(getTileSize());
            TileValidator.initialize();
            Gdx.input.setCatchBackKey(true);

            skin = new Skin(FileProvider.getAssetsFile("skin/aeii_skin.json"));
            skin.get(TextButton.TextButtonStyle.class).font = FontRenderer.getTextFont();
            skin.get(TextField.TextFieldStyle.class).font = FontRenderer.getTextFont();
            skin.get(Label.LabelStyle.class).font = FontRenderer.getTextFont();
            skin.get(Dialog.WindowStyle.class).titleFont = FontRenderer.getTextFont();
            skin.get(List.ListStyle.class).font = FontRenderer.getTextFont();

            LogoScreen logo_screen = new LogoScreen(this);
            main_menu_screen = new MainMenuScreen(this);
            map_editor_screen = new MapEditorScreen(this);
            lobby_screen = new LobbyScreen(this);
            net_game_create_screen = new NetGameCreateScreen(this);
            test_screen = new TestScreen(this);
            game_screen = new GameScreen(this);
            statistics_screen = new StatisticsScreen(this);
            createDialogLayer();

            network_manager = new NetworkManager();
            script_engine = new JavaScriptEngine();
            GameHost.setContext(this);

            setScreen(logo_screen);
            System.out.println(getVerificationString());
        } catch (AEIIException ex) {
            Gdx.app.log(TAG, ex.toString());
        }
    }

    private void loadConfiguration() throws AEIIException {
        FileHandle config_file = FileProvider.getUserFile("user.config");
        configuration = new ObjectMap<String, String>();
        try {
            if (config_file.exists() && !config_file.isDirectory()) {
                InputStreamReader reader = new InputStreamReader(config_file.read(), "UTF8");
                PropertiesUtils.load(configuration, reader);
            } else {
                configuration.put("username", "nobody");
                OutputStreamWriter writer = new OutputStreamWriter(config_file.write(false), "UTF8");
                PropertiesUtils.store(configuration, writer, "aeii user configure file");
            }
        } catch (IOException ex) {
            throw new AEIIException(ex.getMessage());
        }
    }

    public void updateConfiguration(String key, String value) {
        FileHandle config_file = FileProvider.getUserFile("user.config");
        try {
            configuration.put(key, value);
            OutputStreamWriter writer = new OutputStreamWriter(config_file.write(false), "UTF8");
            PropertiesUtils.store(configuration, writer, "aeii user configure file");
        } catch (IOException ex) {
            Gdx.app.log(TAG, ex.toString());
        }
    }

    private void createDialogLayer() {
        dialog_layer = new Stage();

        int dw = TILE_SIZE * 6;
        int dh = TILE_SIZE / 2 * 5;
        this.dialog = new Dialog("", getSkin());
        this.dialog.setBounds((Gdx.graphics.getWidth() - dw) / 2, (Gdx.graphics.getHeight() - dh) / 2, dw, dh);
        this.dialog.setVisible(false);
        this.dialog_layer.addActor(dialog);

        this.btn_ok = new TextButton(Language.getText("LB_OK"), getSkin());
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

    public Skin getSkin() {
        return skin;
    }

    public boolean isDialogShown() {
        return dialog.isVisible();
    }

    public void submitAsyncTask(AsyncTask task) {
        executor.submit(task);
    }

    public void gotoMainMenuScreen() {
        gotoScreen(main_menu_screen);
    }

    public void gotoMapEditorScreen() {
        AudioManager.stopCurrentBGM();
        gotoScreen(map_editor_screen);
    }

    public void gotoGameScreen(GameCore game) {
        AudioManager.stopCurrentBGM();
        game.initialize();
        game_screen.prepare(game);
        gotoScreen(game_screen);
    }

    public void gotoGameScreen(GameSave save) {
        AudioManager.stopCurrentBGM();
        game_screen.prepare(save.game);
        gotoScreen(game_screen);
    }

    public void gotoGameScreen(GameRecord record) {
        AudioManager.stopCurrentBGM();
        game_screen.prepare(record);
        gotoScreen(game_screen);
    }

    public void gotoLobbyScreen() {
        gotoScreen(lobby_screen);
    }

    public void gotoNetGameCreateScreen(RoomConfig config, GameSave game_save) {
        net_game_create_screen.setRoomConfig(config);
        net_game_create_screen.setGameSave(game_save);
        gotoScreen(net_game_create_screen);
    }

    public void gotoStatisticsScreen(GameCore game) {
        statistics_screen.setGame(game);
        gotoScreen(statistics_screen);
    }

    public void gotoTestScreen() {
        gotoScreen(test_screen);
    }

    public void gotoPreviousScreen() {
        this.gotoScreen(previous_screen);
    }

    public void gotoScreen(Screen screen) {
        this.previous_screen = getScreen();
        this.setScreen(screen);
        if (screen instanceof StageScreen) {
            ((StageScreen) screen).onFocus();
        }
        if (dialog.isVisible()) {
            Gdx.input.setInputProcessor(dialog_layer);
        }
    }

    public void showMessage(String content, DialogCallback callback) {
        dialog_callback = callback;

        //set the message and title
        dialog.getContentTable().reset();
        dialog.getContentTable().add(new Label(content, getSkin()));
        dialog.setWidth(Math.max(TILE_SIZE * 6, FontRenderer.getTextLayout(content).width + TILE_SIZE));

        //set the button
        dialog.getButtonTable().reset();
        dialog.getButtonTable().add(btn_ok).size(TILE_SIZE / 2 * 5, TILE_SIZE / 2);
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog();
            }
        });

        dialog.setVisible(true);
        Gdx.input.setInputProcessor(dialog_layer);
    }

    public void closeDialog() {
        dialog.setVisible(false);
        if (getScreen() instanceof StageScreen) {
            ((StageScreen) getScreen()).onFocus();
        }
        if (dialog_callback != null) {
            dialog_callback.doCallback();
            dialog_callback = null;
        }
    }

    public NetworkManager getNetworkManager() {
        return network_manager;
    }

    public JavaScriptEngine getScriptEngine() {
        return script_engine;
    }

    public String getUsername() {
        return getConfiguration().get("username", "nobody");
    }

    public boolean isGameHost() {
        String service_name = getNetworkManager().getServiceName();
        String host_service = getHostService();
        return service_name.equals(host_service);
    }

    public String getHostService() {
        return getRoomConfig().host;
    }

    public RoomConfig getRoomConfig() {
        return net_game_create_screen.getRoomConfig();
    }

    public String getVersion() {
        return VERSION + VERSION_MODIFIER;
    }

    public String getVerificationString() {
        String V_STRING = TileFactory.getVerificationString() + UnitFactory.getVerificationString() + VERSION;
        return new Encryptor().encryptString(V_STRING);
    }

    @Override
    public void render() {
        synchronized (RENDER_LOCK) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            super.render();
            if (isDialogShown()) {
                dialog_layer.act(Gdx.graphics.getDeltaTime());
                dialog_layer.draw();
            }
        }
    }

    @Override
    public void dispose() {
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
