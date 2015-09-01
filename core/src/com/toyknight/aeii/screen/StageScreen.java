package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.NetworkListener;
import com.toyknight.aeii.utils.Language;

import java.util.HashMap;

/**
 * Created by toyknight on 8/29/2015.
 */
public class StageScreen extends Stage implements Screen, NetworkListener {

    protected final int ts;
    protected final SpriteBatch batch;

    private final AEIIApplication context;

    private boolean dialog_shown;
    private final Stage dialog_stage;
    private final HashMap<String, Table> dialogs;

    public StageScreen(AEIIApplication context) {
        this.context = context;
        this.ts = context.getTileSize();
        this.batch = new SpriteBatch();
        this.dialog_stage = new Stage();
        this.dialogs = new HashMap<String, Table>();
        this.dialog_shown = false;
    }

    public AEIIApplication getContext() {
        return context;
    }

    public void addDialog(String name, Table dialog) {
        dialog_stage.addActor(dialog);
        dialogs.put(name, dialog);
        dialog.setVisible(false);
    }

    public void showDialog(String name) {
        for (Table dialog : dialogs.values()) {
            dialog.setVisible(false);
        }
        dialogs.get(name).setVisible(true);
        Gdx.input.setInputProcessor(dialog_stage);
        dialog_shown = true;
    }

    public void closeDialog(String name) {
        dialogs.get(name).setVisible(false);
        Gdx.input.setInputProcessor(this);
        dialog_shown = false;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
        if (dialog_shown) {
            this.dialog_stage.draw();
            this.dialog_stage.act(delta);
        }
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
    public void onPlayerDisconnect(String service_name, String username) {
    }

    @Override
    public void onGameStart() {
    }

    @Override
    public void onReceiveGameEvent(GameEvent event) {
        GameHost.dispatchEvent(event);
    }

}
