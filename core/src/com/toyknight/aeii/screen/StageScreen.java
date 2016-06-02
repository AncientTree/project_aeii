package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.Callable;
import com.toyknight.aeii.network.NetworkListener;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.utils.Language;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;

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
    private final LinkedList<String> dialog_layer;

    public StageScreen(GameContext context) {
        this.context = context;
        this.ts = context.getTileSize();
        this.batch = new SpriteBatch();
        this.dialog_stage = new Stage() {
            @Override
            public boolean keyDown(int keyCode) {
                boolean event_handled = super.keyDown(keyCode);
                return event_handled || dialogKeyDown(keyCode);
            }
        };
        this.dialogs = new HashMap<String, BasicDialog>();
        this.dialog_layer = new LinkedList<String>();
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
        if (dialog_layer.size() > 0) {
            dialogs.get(dialog_layer.peekFirst()).setVisible(false);
        }

        dialog_layer.addFirst(name);
        dialogs.get(name).setVisible(true);
        dialogs.get(name).display();

        Gdx.input.setInputProcessor(dialog_stage);
        dialog_shown = true;
    }

    public void closeAllDialogs() {
        while (dialog_layer.size() > 0) {
            String dialog_name = dialog_layer.peekFirst();
            closeDialog(dialog_name);
        }
    }

    public void closeTopDialog() {
        String top_dialog_name = dialog_layer.peekFirst();
        closeDialog(top_dialog_name);
    }

    public void closeDialog(String name) {
        dialog_layer.remove(name);
        dialogs.get(name).setVisible(false);

        String top_dialog_name = dialog_layer.peekFirst();
        if (top_dialog_name == null) {
            Gdx.input.setInputProcessor(this);
            dialog_shown = false;
        } else {
            dialogs.get(top_dialog_name).setVisible(true);
        }
    }

    public boolean isDialogShown() {
        return dialog_shown;
    }

    public boolean dialogKeyDown(int keyCode) {
        return false;
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
        getContext().showMessage(Language.getText("MSG_ERR_DFS"), new Callable() {
            @Override
            public void call() {
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
    public void onAllocationUpdate() {
    }

    @Override
    public void onGameStart() {
    }

    @Override
    public void onReceiveGameEvent(JSONObject event) {
    }

    @Override
    public void onReceiveMessage(String username, String message) {
    }

}