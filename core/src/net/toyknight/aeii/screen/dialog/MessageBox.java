package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.network.CommandExecutor;
import net.toyknight.aeii.concurrent.MessageSendingTask;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.screen.widgets.PlayerList;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/7/2015.
 */
public class MessageBox extends BasicDialog {

    private final CommandExecutor command_executor;

    private PlayerList player_list;

    public MessageBox(StageScreen owner) {
        super(owner);
        this.initComponents();
        this.command_executor = new CommandExecutor(getContext());
    }

    private void initComponents() {
        player_list = new PlayerList(getContext(), ts * 3 / 2, ts);
        ScrollPane sp_player_list = new ScrollPane(player_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        getResources().getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_player_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_player_list.setScrollBarPositions(false, true);
        sp_player_list.setFadeScrollBars(false);
        add(sp_player_list).size(ts * 6 + ts / 2, ts * 6).padTop(ts / 2).padLeft(ts / 2).padRight(ts / 2).row();

        Table button_bar = new Table();
        TextButton btn_send = new TextButton(Language.getText("LB_SEND_MESSAGE"), getContext().getSkin());
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

    public CommandExecutor getCommandExecutor() {
        return command_executor;
    }

    public void setPlayers(Array<PlayerSnapshot> players) {
        player_list.setItems(players);
    }

    public void sendMessage() {
        Gdx.input.getTextInput(input_listener, Language.getText("MSG_INFO_IM"), "", "");
    }

    public void sendMessage(String message) {
        if (message.length() > 0) {
            if (message.startsWith("/")) {
                PlayerSnapshot selected_player = player_list.getSelected();
                getCommandExecutor().execute(message, selected_player.id);
            } else {
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
    }

    private Input.TextInputListener input_listener = new Input.TextInputListener() {
        @Override
        public void input(String message) {
            sendMessage(message);
        }

        @Override
        public void canceled() {
            //do nothing
        }
    };

}
