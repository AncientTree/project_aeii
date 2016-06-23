package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.network.NetworkListener;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.renderer.FontRenderer;
import net.toyknight.aeii.screen.dialog.BasicDialog;
import net.toyknight.aeii.screen.dialog.ConfirmDialog;
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

    private final ConfirmDialog confirm_dialog;

    private static Stage prompt_layer;
    private static Dialog prompt_dialog;
    private static Callable prompt_callback;
    private static TextButton prompt_btn_ok;

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

        this.confirm_dialog = new ConfirmDialog(this);
        this.addDialog("confirm", confirm_dialog);
    }

    public static void initializePrompt(Skin skin, int ts) {
        prompt_layer = new Stage();

        int dw = ts * 6;
        int dh = ts / 2 * 5;
        prompt_dialog = new Dialog("", skin);
        prompt_dialog.setBounds((Gdx.graphics.getWidth() - dw) / 2, (Gdx.graphics.getHeight() - dh) / 2, dw, dh);
        prompt_dialog.setVisible(false);
        prompt_layer.addActor(prompt_dialog);

        prompt_btn_ok = new TextButton(Language.getText("LB_OK"), skin);
    }

    public GameContext getContext() {
        return context;
    }

    public void addDialog(String name, BasicDialog dialog) {
        dialog_stage.addActor(dialog);
        dialogs.put(name, dialog);
        dialog.setVisible(false);
    }

    public void showConfirmDialog(String message, Callable yes_callback, Callable no_callback) {
        confirm_dialog.setMessage(message);
        confirm_dialog.setYesCallback(yes_callback);
        confirm_dialog.setNoCallable(no_callback);
        showDialog("confirm");
    }

    public void closeConfirmDialog() {
        closeDialog("confirm");
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
                dialogs.get(dialog_layer.peekFirst()).setVisible(false);
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

    public void showPrompt(String content, Callable callback) {
        if (!prompt_dialog.isVisible()) {
            prompt_callback = callback;

            //set the message and title
            prompt_dialog.getContentTable().reset();
            prompt_dialog.getContentTable().add(new Label(content, getContext().getSkin()));
            prompt_dialog.setWidth(Math.max(ts * 6, FontRenderer.getTextLayout(content).width + ts));

            //set the button
            prompt_dialog.getButtonTable().reset();
            prompt_dialog.getButtonTable().add(prompt_btn_ok).size(ts / 2 * 5, ts / 2);
            prompt_btn_ok.clearListeners();
            prompt_btn_ok.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    closePrompt();
                }
            });

            prompt_dialog.setVisible(true);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void closePrompt() {
        prompt_dialog.setVisible(false);
        if (isDialogShown()) {
            Gdx.input.setInputProcessor(dialog_stage);
        } else {
            Gdx.input.setInputProcessor(this);
        }
        if (prompt_callback != null) {
            prompt_callback.call();
            prompt_callback = null;
        }
    }

    @Override
    public void show() {
        NetworkManager.setNetworkListener(this);
        if (prompt_dialog.isVisible()) {
            Gdx.input.setInputProcessor(prompt_layer);
        } else {
            if (isDialogShown()) {
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
        if (isDialogShown()) {
            this.dialog_stage.draw();
            this.dialog_stage.act(delta);
        }
        if (prompt_dialog.isVisible()) {
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