package com.toyknight.aeii;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.entity.player.Player;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.screen.LogoScreen;
import com.toyknight.aeii.screen.MainMenuScreen;
import com.toyknight.aeii.screen.TestScreen;
import com.toyknight.aeii.utils.*;

public class AEIIApplication extends Game {

    private final int TILE_SIZE;
    private final Platform PLATFORM;

    private Skin skin;

    private Screen previous_screen;

    private LogoScreen logo_screen;
    private MainMenuScreen main_menu_screen;
    private TestScreen test_screen;
    private GameScreen game_screen;

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
            skin.get(TextButton.TextButtonStyle.class).font = FontRenderer.getLabelFont();
            skin.get(List.ListStyle.class).font = FontRenderer.getLabelFont();

            logo_screen = new LogoScreen(this);
            main_menu_screen = new MainMenuScreen(this);
            test_screen = new TestScreen(this);
            game_screen = new GameScreen(this);

            Animator.setTileSize(getTileSize());

            this.setScreen(logo_screen);
        } catch (AEIIException ex) {
            System.err.println("ERROR: " + ex.getMessage());
        }
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public int getScaling() {
        return TILE_SIZE / 24;
    }

    public Platform getPlatform() {
        return PLATFORM;
    }

    public Skin getSkin() {
        return skin;
    }

    public void gotoMainMenuScreen() {
        gotoScreen(main_menu_screen);
        main_menu_screen.show();
    }

    public void gotoGameScreen(GameCore game) {
        AudioManager.stopCurrentBGM();
        game_screen.prepare(game);
        gotoScreen(game_screen);
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

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
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
