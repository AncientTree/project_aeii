package com.toyknight.aeii.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.NetworkListener;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 8/29/2015.
 */
public class StageScreen extends Stage implements Screen, NetworkListener {

    protected final int ts;
    protected final SpriteBatch batch;

    private final AEIIApplication context;

    public StageScreen(AEIIApplication context) {
        this.context = context;
        this.ts = context.getTileSize();
        this.batch = new SpriteBatch();
    }

    public AEIIApplication getContext() {
        return context;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
    }

    @Override
    public void resize(int width, int height) {
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
    public void onDisconnect() {
        getContext().showMessage(Language.getText("MSG_ERR_DFS"), new DialogCallback() {
            @Override
            public void doCallback() {
                getContext().gotoMainMenuScreen();
            }
        });
    }

    @Override
    public void onPlayerDisconnect(String username, boolean is_host) {
    }

    @Override
    public void onGameStart() {
    }

    @Override
    public void onReceiveGameEvent(GameEvent event) {
        GameHost.dispatchEvent(event);
    }

}
