package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.network.NetworkListener;
import net.toyknight.aeii.network.NetworkManager;
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
    private static Label prompt_message;
    private static TextButton prompt_btn_ok;
    private static Callable prompt_callback;

    private static Dialog input_dialog;
    private static Label input_message;
    private static TextField input_field;
    private static TextButton input_btn_ok;
    private static TextButton input_btn_cancel;
    private static Input.TextInputListener input_listener;

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

        //initialize prompt dialog
        int pdw = ts * 6;
        int pdh = ts / 2 * 5;
        prompt_dialog = new Dialog("", skin);
        prompt_dialog.setBounds((Gdx.graphics.getWidth() - pdw) / 2, (Gdx.graphics.getHeight() - pdh) / 2, pdw, pdh);
        //set the message
        prompt_dialog.getContentTable().pad(ts / 4);
        prompt_message = new Label("", skin);
        prompt_message.setAlignment(Align.center);
        prompt_message.setWrap(true);
        prompt_dialog.getContentTable().add(prompt_message).width(ts * 6);
        //set the button
        prompt_btn_ok = new TextButton(Language.getText("LB_OK"), skin);
        prompt_dialog.getButtonTable().add(prompt_btn_ok).size(ts / 2 * 5, ts / 3 * 2);
        prompt_dialog.setVisible(false);
        prompt_layer.addActor(prompt_dialog);

        //initialize input dialog
        int idw = ts * 8;
        int idh = ts / 2 * 7;
        input_dialog = new Dialog("", skin);
        input_dialog.setBounds((Gdx.graphics.getWidth() - idw) / 2, (Gdx.graphics.getHeight() - idh) / 2, idw, idh);
        input_message = new Label("", skin);
        input_message.setWrap(true);
        input_dialog.getContentTable().add(input_message).width(idw - ts / 2).pad(ts / 4).padBottom(ts / 12).row();
        input_field = new TextField("", skin);
        input_field.setFocusTraversal(false);
        input_dialog.getContentTable().add(input_field).size(idw - ts / 2, ts / 2 + ts / 8);
        input_btn_ok = new TextButton(Language.getText("LB_OK"), skin);
        input_dialog.getButtonTable().add(input_btn_ok).size(ts * 3, ts / 3 * 2).pad(ts / 4).padTop(ts / 8);
        input_btn_cancel = new TextButton(Language.getText("LB_CANCEL"), skin);
        input_dialog.getButtonTable().add(input_btn_cancel).size(ts * 3, ts / 3 * 2).pad(ts / 4).padTop(ts / 8);
        input_dialog.setVisible(false);
        prompt_layer.addActor(input_dialog);
    }

    public void showPrompt(String message, Callable callback) {
        input_dialog.setVisible(false);
        if (!prompt_dialog.isVisible()) {
            prompt_callback = callback;
            prompt_message.setText(message);
            if (prompt_btn_ok.getListeners().size <= 1) {
                prompt_btn_ok.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        closePrompt();
                    }
                });
            }
            prompt_dialog.getContentTable().pack();
            prompt_dialog.pack();
            prompt_dialog.setPosition(
                    (Gdx.graphics.getWidth() - prompt_dialog.getWidth()) / 2,
                    (Gdx.graphics.getHeight() - prompt_dialog.getHeight()) / 2);
            prompt_dialog.setVisible(true);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void showInput(String message, int max_length, boolean password, Input.TextInputListener input_listener) {
        if (!isPromptVisible()) {
            StageScreen.input_listener = input_listener;
            input_message.setText(message);
            input_field.setText("");
            input_field.setMaxLength(max_length);
            input_field.setAlignment(password ? Align.center : Align.left);
            input_field.setTextFieldListener(new TextField.TextFieldListener() {
                @Override
                public void keyTyped(TextField textField, char c) {
                    if (c == '\n' || c == '\r') {
                        StageScreen.input_listener.input(input_field.getText());
                        closeInput();
                    }
                }
            });
            if (input_btn_ok.getListeners().size <= 1) {
                input_btn_ok.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        StageScreen.input_listener.input(input_field.getText());
                        closeInput();
                    }
                });
            }
            if (input_btn_cancel.getListeners().size <= 1) {
                input_btn_cancel.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        StageScreen.input_listener.canceled();
                        closeInput();
                    }
                });
            }
            input_dialog.getContentTable().pack();
            input_dialog.pack();
            input_dialog.setPosition(
                    (Gdx.graphics.getWidth() - input_dialog.getWidth()) / 2,
                    (Gdx.graphics.getHeight() - input_dialog.getHeight()) / 2);
            input_dialog.setVisible(true);
            Gdx.input.setInputProcessor(prompt_layer);
        }
    }

    public void closePrompt() {
        prompt_dialog.setVisible(false);
        updateFocus();
        if (prompt_callback != null) {
            prompt_callback.call();
            prompt_callback = null;
        }
    }

    public void closeInput() {
        input_dialog.setVisible(false);
        updateFocus();
    }

    public GameContext getContext() {
        return context;
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

    public final void updateFocus() {
        if(isPromptVisible()) {
            Gdx.input.setInputProcessor(prompt_layer);
            return;
        }
        if(isDialogVisible()) {
            Gdx.input.setInputProcessor(dialog_stage);
            return;
        }
        Gdx.input.setInputProcessor(this);
    }

    public void addDialog(String name, BasicDialog dialog) {
        dialog.setName(name);
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

    public boolean isPromptVisible() {
        return input_dialog.isVisible() || prompt_dialog.isVisible();
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