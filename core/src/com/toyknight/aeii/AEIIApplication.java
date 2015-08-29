package com.toyknight.aeii;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.net.NetworkManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.screen.*;
import com.toyknight.aeii.utils.*;

public class AEIIApplication extends Game {

    private final int TILE_SIZE;
    private final Platform PLATFORM;

    private Skin skin;

    private Screen previous_screen;

    private LogoScreen logo_screen;
    private MainMenuScreen main_menu_screen;
    private MapEditorScreen map_editor_screen;
    private LobbyScreen lobby_screen;
    private NetGameCreateScreen net_game_create_screen;
    private TestScreen test_screen;
    private GameScreen game_screen;

    private Stage dialog_layer;
    private Dialog dialog;
    private DialogCallback dialog_callback;
    private TextButton btn_ok;

    private NetworkManager network_manager;

    public AEIIApplication(Platform platform, int ts) {
        this.TILE_SIZE = ts;
        this.PLATFORM = platform;
    }

    @Override
    public void create() {
        try {
            FileProvider.setPlatform(PLATFORM);

            Language.init();
            TileFactory.loadTileData();
            UnitFactory.loadUnitData();
            ResourceManager.loadResources();
            FontRenderer.loadFonts(TILE_SIZE);
            BorderRenderer.init();

            skin = new Skin(FileProvider.getAssetsFile("skin/aeii_skin.json"));
            skin.get(TextButton.TextButtonStyle.class).font = FontRenderer.getTextFont();
            skin.get(TextField.TextFieldStyle.class).font = FontRenderer.getTextFont();
            skin.get(Label.LabelStyle.class).font = FontRenderer.getTextFont();
            skin.get(Dialog.WindowStyle.class).titleFont = FontRenderer.getTextFont();
            skin.get(List.ListStyle.class).font = FontRenderer.getTextFont();

            logo_screen = new LogoScreen(this);
            main_menu_screen = new MainMenuScreen(this);
            map_editor_screen = new MapEditorScreen(this);
            lobby_screen = new LobbyScreen(this);
            net_game_create_screen = new NetGameCreateScreen(this);
            test_screen = new TestScreen(this);
            game_screen = new GameScreen(this);
            createDialogLayer();

            network_manager = new NetworkManager();

            Animator.setTileSize(getTileSize());

            setScreen(logo_screen);
        } catch (AEIIException ex) {
            System.err.println("ERROR: " + ex.getMessage());
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

    public Skin getSkin() {
        return skin;
    }

    public boolean isDialogShown() {
        return dialog.isVisible();
    }

    public void gotoMainMenuScreen() {
        gotoScreen(main_menu_screen);
    }

    public void gotoMapEditorScreen() {
        AudioManager.stopCurrentBGM();
        gotoScreen(map_editor_screen);
    }

    public void gotoGameScreen(GameManager manager) {
        AudioManager.stopCurrentBGM();
        game_screen.prepare(manager);
        gotoScreen(game_screen);
    }

    public void gotoLobbyScreen() {
        gotoScreen(lobby_screen);
    }

    public void gotoNetGameCreateScreen(long room_number) {
        net_game_create_screen.setRoomNumber(room_number);
        gotoScreen(net_game_create_screen);
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
    }

    public void showMessage(String content, DialogCallback callback) {
        dialog_callback = callback;

        //set the message and title
        dialog.getContentTable().reset();
        dialog.getContentTable().add(new Label(content, getSkin()));
        dialog.setWidth(Math.max(TILE_SIZE * 6, FontRenderer.getTextFont().getBounds(content).width + TILE_SIZE));

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
        if (dialog_callback != null) {
            dialog_callback.doCallback();
            dialog_callback = null;
        }
        if (getScreen() instanceof Stage) {
            Gdx.input.setInputProcessor((Stage) getScreen());
        }
    }

    public MainMenuScreen getMainMenuScreen() {
        return main_menu_screen;
    }

    public NetworkManager getNetworkManager() {
        return network_manager;
    }

    public String getUsername() {
        return "default";
    }

    @Override
    public void render() {
        getNetworkManager().updateTasks();
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
        if (isDialogShown()) {
            dialog_layer.act(Gdx.graphics.getDeltaTime());
            dialog_layer.draw();
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
