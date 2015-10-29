package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.NetworkListener;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.utils.Language;

import java.util.HashMap;

/**
 * @author toyknight 8/29/2015.
 */
public class StageScreen extends Stage implements Screen, NetworkListener {

    protected final int ts;
    protected final SpriteBatch batch;

    private final GameContext context;

    private boolean dialog_shown;
    private final Stage dialog_stage;
    private final HashMap<String, BasicDialog> dialogs;

    public StageScreen(GameContext context) {
        this.context = context;
        this.ts = context.getTileSize();
        this.batch = new SpriteBatch();
        this.dialog_stage = new Stage();
        this.dialogs = new HashMap<String, BasicDialog>();
        this.dialog_shown = false;
    }

    public GameContext getContext() {
        return context;
    }

    public void onFocus() {
        if (isDialogShown()) {
            Gdx.input.setInputProcessor(dialog_stage);
        } else {
            Gdx.input.setInputProcessor(this);
        }
    }

    public void addDialog(String name, BasicDialog dialog) {
        dialog_stage.addActor(dialog);
        dialogs.put(name, dialog);
        dialog.setVisible(false);
    }

    public void showDialog(String name) {
        closeAllDialogs();
        dialogs.get(name).setVisible(true);
        dialogs.get(name).display();
        Gdx.input.setInputProcessor(dialog_stage);
        dialog_shown = true;
    }

    public void closeAllDialogs() {
        for (BasicDialog dialog : dialogs.values()) {
            dialog.setVisible(false);
        }
        dialog_shown = false;
        Gdx.input.setInputProcessor(this);
    }

    public void closeDialog(String name) {
        dialogs.get(name).setVisible(false);
        Gdx.input.setInputProcessor(this);
        dialog_shown = false;
    }

    public boolean isDialogShown() {
        return dialog_shown;
    }

    public Stage getDialogLayer() {
        return dialog_stage;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
        if (isDialogShown()) {
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
    public void onPlayerJoin(int id, String username) {
    }

    @Override
    public void onPlayerLeave(int id, String username) {
    }

    @Override
    public void onAllocationUpdate(Integer[] allocation, Integer[] types) {
    }

    @Override
    public void onAllianceUpdate(Integer[] alliance) {
    }

    @Override
    public void onGameStart(GameSave game_save) {
    }

    @Override
    public void onReceiveGameEvent(GameEvent event) {
    }

    @Override
    public void onReceiveMessage(String username, String message) {
    }

}
