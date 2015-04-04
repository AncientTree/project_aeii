package com.toyknight.aeii;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.screen.LogoScreen;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitFactory;

public class AEIIApplication extends Game {

    private final int TILE_SIZE;

    private LogoScreen logo_screen;

    private SpriteBatch batch;

    public AEIIApplication() {
        this(48);
    }

    public AEIIApplication(int ts) {
        this.TILE_SIZE = ts;
    }

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();
            FontRenderer.loadFonts();
            TextureManager.loadTextures();

            TileFactory.loadTileData();
            UnitFactory.loadUnitData();

            logo_screen = new LogoScreen(this);
            logo_screen.create();

            this.setScreen(logo_screen);
        } catch (AEIIException ex) {
            System.err.println("ERROR: " + ex.getMessage());
        }
    }

    public void gotoMainMenuScreen() {

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }

}
