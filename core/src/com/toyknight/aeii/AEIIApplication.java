package com.toyknight.aeii;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
import com.toyknight.aeii.utils.*;

public class AEIIApplication extends Game {

    private final int TILE_SIZE;
    private final Platform PLATFORM;

    private Skin skin;

    private LogoScreen logo_screen;
    private MainMenuScreen main_menu_screen;
    private GameScreen game_screen;

    public AEIIApplication(Platform platform, int ts) {
        this.TILE_SIZE = ts;
        this.PLATFORM = platform;
    }

    @Override
    public void create() {
        try {
            FileProvider.setPlatform(PLATFORM);

            skin = new Skin(FileProvider.getAssetsFile("skin/aeii_skin.json"));
            TileFactory.loadTileData();
            UnitFactory.loadUnitData();
            ResourceManager.loadResources();
            FontRenderer.loadFonts(TILE_SIZE);
            BorderRenderer.init();

            logo_screen = new LogoScreen(this);
            main_menu_screen = new MainMenuScreen();
            game_screen = new GameScreen(this);

            Animator.setTileSize(getTileSize());

            //for test only
            Map map = MapFactory.createMap(FileProvider.getAssetsFile("map/test.aem"));
            Player[] players = new Player[4];
            for (int i = 0; i < 4; i++) {
                players[i] = new LocalPlayer();
                players[i].setAlliance(i);
                players[i].setGold(1000);
            }
            GameCore game = new GameCore(map, Rule.getDefaultRule(), players);
            game_screen.setGame(game);
            this.setScreen(game_screen);

//            this.setScreen(logo_screen);
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

}
