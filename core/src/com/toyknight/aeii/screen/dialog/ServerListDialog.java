package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.screen.MainMenuScreen;
import com.toyknight.aeii.serializable.ServerConfig;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.utils.Language;

import java.io.IOException;

/**
 * @author toyknight 8/25/2015.
 */
public class ServerListDialog extends BasicDialog {

    private StringList<ServerConfig> server_list;
    private TextButton btn_connect;
    private TextButton btn_back;

    public ServerListDialog(MainMenuScreen screen) {
        super(screen);
        int width = ts * 10;
        int height = ts * 6;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - 85 * ts / 48 - height) / 2, width, height);
        this.initComponents();
    }

    private void initComponents() {
        server_list = new StringList<ServerConfig>(ts);
        ScrollPane sp_server_list = new ScrollPane(server_list, getContext().getSkin());
        sp_server_list.setBounds(ts / 2, ts * 2, getWidth() - ts, getHeight() - ts * 2 - ts / 2);
        sp_server_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_server_list.setScrollBarPositions(false, true);

        this.addActor(sp_server_list);

        Array<ServerConfig> servers = new Array<ServerConfig>();
        servers.add(new ServerConfig("127.0.0.1", 5438, "aeii server - Local"));
        servers.add(new ServerConfig("112.74.215.26", 5438, "aeii server - China"));
        servers.add(new ServerConfig("45.56.93.69", 5438, "aeii server - NA"));
        server_list.setItems(servers);

        int width_btn = ts * 3;
        int margin_left = ((int) getWidth() - width_btn * 2 - ts) / 2;
        btn_connect = new TextButton(Language.getText("LB_CONNECT"), getContext().getSkin());
        btn_connect.setBounds(margin_left, ts / 2, width_btn, ts);
        btn_connect.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doConnect();
            }
        });
        addActor(btn_connect);

        btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.setBounds(margin_left + width_btn + ts, ts / 2, width_btn, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("server");
            }
        });
        addActor(btn_back);
    }

    private ServerConfig getSelectedServer() {
        return server_list.getSelected();
    }

    private void setEnabled(boolean enabled) {
        AEIIApplication.setButtonEnabled(btn_connect, enabled);
        AEIIApplication.setButtonEnabled(btn_back, enabled);
        server_list.setEnabled(enabled);
    }

    private void doConnect() {
        if (getSelectedServer().getAddress().length() > 0) {
            setEnabled(false);
            btn_connect.setText(Language.getText("LB_CONNECTING"));

            getContext().submitAsyncTask(new AsyncTask<Boolean>() {
                @Override
                public Boolean doTask() throws IOException {
                    return getContext().getNetworkManager().connect(
                            getSelectedServer(), getContext().getUsername(), getContext().getVerificationString());
                }

                @Override
                public void onFinish(Boolean approved) {
                    if (approved) {
                        setEnabled(true);
                        btn_connect.setText(Language.getText("LB_CONNECT"));
                        getContext().gotoLobbyScreen();
                    } else {
                        setEnabled(true);
                        btn_connect.setText(Language.getText("LB_CONNECT"));
                        getOwner().closeDialog("server");
                        getContext().getNetworkManager().disconnect();
                        getContext().showMessage(Language.getText("MSG_ERR_RBS"), null);
                    }
                }

                @Override
                public void onFail(String message) {
                    setEnabled(true);
                    btn_connect.setText(Language.getText("LB_CONNECT"));
                    getContext().showMessage(Language.getText("MSG_ERR_CCS"), new DialogCallback() {
                        @Override
                        public void doCallback() {
                            Gdx.input.setInputProcessor(getOwner().getDialogLayer());
                        }
                    });
                }
            });
        }
    }

    public MainMenuScreen getOwner() {
        return (MainMenuScreen) super.getOwner();
    }

}
