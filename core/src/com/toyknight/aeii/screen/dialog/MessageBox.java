package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.Callable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.net.task.MessageSendingTask;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.widgets.PlayerList;
import com.toyknight.aeii.serializable.PlayerSnapshot;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/7/2015.
 */
public class MessageBox extends BasicDialog {

    private Callable callback;

    private PlayerList player_list;
    private TextField tf_message;

    public MessageBox(StageScreen owner) {
        super(owner);
        this.initComponents();
    }

    private void initComponents() {
        player_list = new PlayerList(ts * 3 / 2, ts);
        ScrollPane sp_player_list = new ScrollPane(player_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_player_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_player_list.setScrollBarPositions(false, true);
        sp_player_list.setFadeScrollBars(false);
        add(sp_player_list).size(ts * 6 + ts / 2, ts * 5).pad(ts / 2).row();

        tf_message = new TextField("", getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        tf_message.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char key) {
                if ((key == '\r' || key == '\n')) {
                    sendMessage();
                }
            }
        });
        tf_message.setFocusTraversal(false);
        add(tf_message).width(ts * 6 + ts / 2).padLeft(ts / 2).padRight(ts / 2).row();

        Table button_bar = new Table();
        TextButton btn_send = new TextButton(Language.getText("LB_SEND"), getContext().getSkin());
        btn_send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage();
            }
        });
        button_bar.add(btn_send).size(ts * 3, ts);
        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("message");
            }
        });
        button_bar.add(btn_cancel).size(ts * 3, ts).padLeft(ts / 2);
        add(button_bar).size(ts * 6 + ts / 2, ts).pad(ts / 2);

        pack();
    }

    @Override
    public void display() {
        tf_message.setText("");
    }

    public void setPlayers(Array<PlayerSnapshot> players, Integer[] allocation) {
        player_list.setItems(players, allocation);
    }

    public void removePlayer(Integer id) {
        player_list.removePlayer(id);
    }

    public void addPlayer(Integer id, String username) {
        player_list.addPlayer(id, username);
    }

    public void addPlayer(Integer id, String username, Integer[] teams) {
        player_list.addPlayer(id, username, teams);
    }

    public void sendMessage() {
        sendMessage(tf_message.getText());
        getOwner().closeDialog("message");
        getOwner().setKeyboardFocus(null);
    }

    public void sendMessage(String message) {
        getContext().submitAsyncTask(new MessageSendingTask(message) {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().sendMessage(message);
                return null;
            }

            @Override
            public void onFinish(Void result) {
            }

            @Override
            public void onFail(String message) {
                getContext().showMessage(message, null);
            }
        });
        callback.call();
    }

    public void setCallback(Callable callback) {
        this.callback = callback;
    }

}
