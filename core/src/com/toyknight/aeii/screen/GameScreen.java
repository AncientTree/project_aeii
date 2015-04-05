package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.renderer.MapRenderer;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameScreen implements Screen {

    private final AEIIApplication context;

    private GameManager manager;

    private BasicGame game;

    public GameScreen(AEIIApplication context) {
        this.context = context;
        this.manager = new GameManager(context.getTileSize());
    }

    public void setGame(BasicGame game) {
        this.game = game;
        this.manager.setGame(game);
    }

    public BasicGame getGame() {
        return game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(manager);
    }

    @Override
    public void render(float delta) {
        manager.draw();
        manager.act(delta);
    }

    @Override
    public void resize(int width, int height) {
        manager.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {

    }

}
