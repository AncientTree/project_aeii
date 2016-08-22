package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.concurrent.MessageSendingTask;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/19/2016.
 */
public class ChatRoomDialog extends BasicDialog {

    private final TextButton btn_refresh;

    private final Table message_pane;
    private final ScrollPane sp_message;

    private final Table player_pane;
    private final ScrollPane sp_player;

    public ChatRoomDialog(StageScreen owner) {
        super(owner);
        int width = ts * 14 + ts / 2;
        int height = Gdx.graphics.getHeight() - ts;
        setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - height) / 2, width, height);

        Table sp_bar = new Table();
        add(sp_bar).size(width - ts, height - ts * 2).row();

        message_pane = new Table();
        message_pane.top();
        sp_message = new ScrollPane(message_pane, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        getResources().getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_bar.add(sp_message).size(width - ts * 4 - ts / 2, height - ts * 2 - ts / 2).padTop(ts / 2);

        Table player_container = new Table();
        sp_bar.add(player_container).size(ts * 3, height - ts * 2 - ts / 2).padTop(ts / 2).padLeft(ts / 2);

        Label label_idle_player = new Label(Language.getText("LB_IDLE_PLAYERS"), getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                ResourceManager.setBatchAlpha(batch, 1.0f);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                ResourceManager.setBatchAlpha(batch, parentAlpha);
                super.draw(batch, parentAlpha);
            }
        };
        label_idle_player.setAlignment(Align.center);
        player_container.add(label_idle_player).size(ts * 3, ts).row();

        player_pane = new Table();
        player_pane.top();

        sp_player = new ScrollPane(player_pane);
        player_container.add(sp_player).size(ts * 3, height - ts * 3 - ts / 2);

        Table button_bar = new Table();
        button_bar.pad(ts / 2);

        TextButton btn_message = new TextButton(Language.getText("LB_SEND_MESSAGE"), getContext().getSkin());
        btn_message.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage();
            }
        });
        button_bar.add(btn_message).size(ts * 3, ts).padRight(ts / 2);

        TextButton btn_clear = new TextButton(Language.getText("LB_CLEAR"), getContext().getSkin());
        btn_clear.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearMessage();
            }
        });
        button_bar.add(btn_clear).size(ts * 3, ts).padRight(ts / 2);

        btn_refresh = new TextButton(Language.getText("LB_REFRESH"), getContext().getSkin());
        btn_refresh.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryRefreshIdlePlayerList();
            }
        });
        button_bar.add(btn_refresh).size(ts * 3, ts).padRight(ts / 2);

        TextButton btn_close = new TextButton(Language.getText("LB_CLOSE"), getContext().getSkin());
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        button_bar.add(btn_close).size(ts * 3, ts);

        add(button_bar).size(width - ts, ts * 2);
    }

    public void appendMessage(String username, String message) {
        String content = username == null ? ">" + message : ">" + username + ": " + message;
        Label label_message = new Label(content, getContext().getSkin());
        label_message.setWrap(true);
        message_pane.add(label_message).width(message_pane.getWidth()).padTop(ts / 12).row();
        message_pane.layout();
        sp_message.layout();
        sp_message.setScrollPercentY(1.0f);
    }

    public void clearMessage() {
        message_pane.clearChildren();
        message_pane.layout();
        sp_message.layout();
    }

    public void sendMessage() {
        if (Language.getLocale().equals("zh_CN")) {
            Gdx.input.getTextInput(message_input_listener, Language.getText("MSG_INFO_IM"), "", "");
        } else {
            getOwner().showInput(Language.getText("MSG_INFO_IM"), 256, false, message_input_listener);
        }
    }

    private void trySendMessage(String message) {
        if (message.length() > 0) {
            getContext().submitAsyncTask(new MessageSendingTask(message) {
                @Override
                public void onFinish(Void result) {
                }

                @Override
                public void onFail(String message) {
                }
            });
        }
    }

    private void updateIdlePlayerList(Array<PlayerSnapshot> list) {
        player_pane.clearChildren();
        for (PlayerSnapshot snapshot : list) {
            Label label_player = new Label(snapshot.username, getContext().getSkin());
            label_player.setWrap(true);
            label_player.setColor(snapshot.id == NetworkManager.getServiceID() ? Color.RED : Color.WHITE);
            player_pane.add(label_player).width(ts * 3 - ts / 6).pad(ts / 12).row();
        }
        player_pane.layout();
        sp_player.layout();
        sp_player.setScrollPercentY(0f);
    }

    private void tryRefreshIdlePlayerList() {
        Gdx.input.setInputProcessor(null);
        getOwner().showPlaceholder(Language.getText("LB_REFRESHING"));
        getContext().submitAsyncTask(new AsyncTask<Array<PlayerSnapshot>>() {
            @Override
            public Array<PlayerSnapshot> doTask() throws Exception {
                return NetworkManager.requestIdlePlayerList();
            }

            @Override
            public void onFinish(Array<PlayerSnapshot> result) {
                if (result == null) {
                    getOwner().showNotification(Language.getText("MSG_ERR_AEA"), null);
                } else {
                    updateIdlePlayerList(result);
                }
                getOwner().closePlaceholder();
            }

            @Override
            public void onFail(String message) {
                getOwner().closePlaceholder();
                getOwner().showNotification(Language.getText("MSG_ERR_AEA"), null);
            }
        });
    }

    @Override
    public void display() {
        tryRefreshIdlePlayerList();
    }

    private Input.TextInputListener message_input_listener = new Input.TextInputListener() {
        @Override
        public void input(String message) {
            trySendMessage(message);
        }

        @Override
        public void canceled() {
            //do nothing
        }
    };

}
