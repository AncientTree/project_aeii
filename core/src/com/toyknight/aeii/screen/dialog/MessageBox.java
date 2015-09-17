package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.net.task.MessageSendingTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/7/2015.
 */
public class MessageBox extends BasicDialog {

    private DialogCallback callback;

    private TextButton btn_greetings;
    private TextButton btn_gg;
    private TextButton btn_thinking;
    private TextButton btn_sorry;
    private TextButton btn_oops;
    private TextButton btn_irritate;
    private TextButton btn_close;

    public MessageBox(StageScreen owner) {
        super(owner);
        this.initComponents();
    }

    private void initComponents() {
        btn_greetings = new TextButton(Language.getText("LB_GREETINGS"), getContext().getSkin());
        btn_greetings.setBounds(ts / 2, ts / 2, ts * 3, ts);
        btn_greetings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getContext().hasTeamAccess()) {
                    sendMessage("MSG_GREETINGS_1");
                } else {
                    sendMessage("MSG_GREETINGS_2");
                }
            }
        });
        addActor(btn_greetings);

        btn_gg = new TextButton(Language.getText("LB_GG"), getContext().getSkin());
        btn_gg.setBounds(ts / 2 + ts * 4, ts / 2, ts * 3, ts);
        btn_gg.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage("MSG_GG");
            }
        });
        addActor(btn_gg);

        btn_thinking = new TextButton(Language.getText("LB_THINKING"), getContext().getSkin());
        btn_thinking.setBounds(ts / 2, ts * 2, ts * 7, ts);
        btn_thinking.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage("MSG_THINKING");
            }
        });
        addActor(btn_thinking);

        btn_sorry = new TextButton(Language.getText("LB_SORRY"), getContext().getSkin());
        btn_sorry.setBounds(ts / 2, ts * 2 + ts / 2 * 3, ts * 7, ts);
        btn_sorry.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getContext().hasTeamAccess()) {
                    sendMessage("MSG_SORRY_1");
                } else {
                    sendMessage("MSG_SORRY_2");
                }
            }
        });
        addActor(btn_sorry);

        btn_oops = new TextButton(Language.getText("LB_OOPS"), getContext().getSkin());
        btn_oops.setBounds(ts / 2, ts * 5, ts * 7, ts);
        btn_oops.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage("MSG_OOPS");
            }
        });
        addActor(btn_oops);

        btn_irritate = new TextButton(Language.getText("LB_IRRITATE"), getContext().getSkin());
        btn_irritate.setBounds(ts / 2, ts * 4 + ts / 2 * 5, ts * 4 + ts / 2 * 3, ts);
        btn_irritate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getContext().hasTeamAccess()) {
                    sendMessage("MSG_IRRITATE_1");
                } else {
                    sendMessage("MSG_IRRITATE_2");
                }
            }
        });
        addActor(btn_irritate);

        btn_close = new TextButton("x", getContext().getSkin());
        btn_close.setBounds(ts * 6 + ts / 2, ts * 4 + ts / 2 * 5, ts, ts);
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.doCallback();
            }
        });
        addActor(btn_close);
    }

    public void sendMessage(String message) {
        getContext().submitAsyncTask(new MessageSendingTask(message) {
            @Override
            public Void doTask() throws Exception {
                getContext().getNetworkManager().requestSubmitMessage(message);
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
        callback.doCallback();
    }

    public void setCallback(DialogCallback callback) {
        this.callback = callback;
    }

}
