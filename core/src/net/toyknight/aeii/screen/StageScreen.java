package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.network.NetworkListener;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.screen.dialog.*;
import net.toyknight.aeii.utils.Language;
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

    private Stage prompt_layer;
    private PromptDialog prompt_dialog;
    private InputDialog input_dialog;
    private ConfirmDialog confirm_dialog;
    private PlaceholderDialog placeholder;

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

        initializePrompts();
    }

    private void initializePrompts() {
        prompt_layer = new Stage();

        //initialize prompt dialog
        prompt_dialog = new PromptDialog(this);
        prompt_dialog.setVisible(false);
        prompt_layer.addActor(prompt_dialog);

        //initialize input dialog
        input_dialog = new InputDialog(this);
        input_dialog.setVisible(false);
        prompt_layer.addActor(input_dialog);

        //initialize confirm dialog
        confirm_dialog = new ConfirmDialog(this);
        confirm_dialog.setVisible(false);
        prompt_layer.addActor(confirm_dialog);

        //initialize placeholder
        placeholder = new PlaceholderDialog(this);
        placeholder.setVisible(false);
        prompt_layer.addActor(placeholder);
    }

    public void showPrompt(String message, Callable callback) {
        input_dialog.setVisible(false);
        placeholder.setVisible(false);
        if (!prompt_dialog.isVisible()) {
            prompt_dialog.display(message, callback);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void closePrompt() {
        prompt_dialog.setVisible(false);
        updateFocus();
    }

    public void showInput(String message, int max_length, boolean password, Input.TextInputListener input_listener) {
        if (!isPromptVisible()) {
            input_dialog.display(message, max_length, password, input_listener);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void closeInput() {
        input_dialog.setVisible(false);
        updateFocus();
    }

    public void showConfirm(String message, ConfirmDialog.ConfirmDialogListener listener) {
        if (!isPromptVisible()) {
            confirm_dialog.display(message, listener);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void closeConfirm() {
        confirm_dialog.setVisible(false);
        updateFocus();
    }

    public void showPlaceholder(String message) {
        if (!isPromptVisible()) {
            placeholder.setMessage(message);
            placeholder.setVisible(true);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void closePlaceholder() {
        placeholder.setVisible(false);
        updateFocus();
    }

    public final void updateFocus() {
        if (isPromptVisible()) {
            Gdx.input.setInputProcessor(prompt_layer);
            return;
        }
        if (isDialogVisible()) {
            Gdx.input.setInputProcessor(dialog_stage);
            return;
        }
        Gdx.input.setInputProcessor(this);
    }

    public boolean isPromptVisible() {
        return input_dialog.isVisible() || prompt_dialog.isVisible() || confirm_dialog.isVisible() || placeholder.isVisible();
    }

    public GameContext getContext() {
        return context;
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

    public void addDialog(String name, BasicDialog dialog) {
        dialog.setName(name);
        dialog_stage.addActor(dialog);
        dialogs.put(name, dialog);
        dialog.setVisible(false);
    }

    public void showWiki() {
        if (!dialogs.containsKey("wiki")) {
            addDialog("wiki", getContext().getWiki());
        }
        getContext().getWiki().setOwner(this);
        showDialog("wiki");
    }

    public void closeWiki() {
        closeDialog("wiki");
    }

    public void showDialog(String name) {
        BasicDialog dialog = dialogs.get(name);
        if (dialog != null && !dialog.isVisible()) {
            if (dialog_layer.size() > 0) {
                dialogs.get(dialog_layer.getFirst()).setVisible(false);
            }

            dialog_layer.addFirst(name);
            dialog.display();
            dialog.setVisible(true);

            Gdx.input.setInputProcessor(dialog_stage);
            dialog_shown = true;
        }
    }

    public void closeAllDialogs() {
        while (dialog_layer.size() > 0) {
            String dialog_name = dialog_layer.getFirst();
            closeDialog(dialog_name);
        }
    }

    public void closeTopDialog() {
        if (dialog_layer.size() > 0) {
            String top_dialog_name = dialog_layer.getFirst();
            closeDialog(top_dialog_name);
        }
    }

    public void closeDialog(String name) {
        dialog_layer.remove(name);
        dialogs.get(name).setVisible(false);

        if (dialog_layer.size() > 0) {
            String top_dialog_name = dialog_layer.getFirst();
            dialogs.get(top_dialog_name).setVisible(true);
        } else {
            Gdx.input.setInputProcessor(this);
            dialog_shown = false;
        }
    }

    public boolean isDialogVisible() {
        return dialog_shown;
    }

    public boolean dialogKeyDown(int keyCode) {
        return false;
    }

    @Override
    public void show() {
        NetworkManager.setNetworkListener(this);
        if (prompt_dialog.isVisible()) {
            Gdx.input.setInputProcessor(prompt_layer);
        } else {
            if (isDialogVisible()) {
                Gdx.input.setInputProcessor(dialog_stage);
            } else {
                Gdx.input.setInputProcessor(this);
            }
        }
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
        if (isDialogVisible()) {
            this.dialog_stage.draw();
            this.dialog_stage.act(delta);
        }
        if (isPromptVisible()) {
            prompt_layer.draw();
            prompt_layer.act();
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
        showPrompt(Language.getText("MSG_ERR_DFS"), new Callable() {
            @Override
            public void call() {
                getContext().gotoMainMenuScreen(true);
            }
        });
    }

    @Override
    public void onPlayerJoin(int id, String username) {
        getContext().getRoomManager().onPlayerJoin(id, username);
    }

    @Override
    public void onPlayerLeave(int id, String username, int host) {
        getContext().getRoomManager().onPlayerLeave(id, host);
    }

    @Override
    public void onAllocationUpdate(int[] alliance, int[] allocation, int[] types) {
        getContext().getRoomManager().onAllocationUpdate(alliance, allocation, types);
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