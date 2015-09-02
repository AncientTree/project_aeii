package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.net.task.NetworkTask;
import com.toyknight.aeii.server.entity.Server;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.utils.Language;

import java.io.IOException;

/**
 * Created by toyknight on 8/25/2015.
 */
public class ServerListDialog extends Table {

    private final int ts;
    private final AEIIApplication context;

    private StringList<Server> server_list;
    private TextButton btn_connect;
    private TextButton btn_back;

    public ServerListDialog(AEIIApplication context) {
        this.context = context;
        this.ts = getContext().getTileSize();
        int width = ts * 10;
        int height = ts * 6;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - 85 - height) / 2, width, height);
        this.initComponents();
    }

    private void initComponents() {
        server_list = new StringList(ts);
        ScrollPane sp_server_list = new ScrollPane(server_list);
        sp_server_list.setBounds(ts / 2, ts * 2, getWidth() - ts, getHeight() - ts * 2 - ts / 2);
        sp_server_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_server_list.setScrollBarPositions(false, true);

        this.addActor(sp_server_list);

        Array<Server> servers = new Array();
        servers.add(new Server("127.0.0.1", 5438, "aeii server - PC Debug"));
        servers.add(new Server("172.16.0.8", 5438, "aeii server - Android Debug"));
        servers.add(new Server("45.56.93.69", 5438, "aeii server - NA"));
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
                getContext().getMainMenuScreen().showMenu();
            }
        });
        addActor(btn_back);
    }

    private AEIIApplication getContext() {
        return context;
    }

    private Server getSelectedServer() {
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

            getContext().getNetworkManager().postTask(new NetworkTask<Void>() {
                @Override
                public Void doTask() throws IOException {
                    getContext().getNetworkManager().connect(getSelectedServer(), getContext().getUsername());
                    return null;
                }

                @Override
                public void onFinish(Void result) {
                    setEnabled(true);
                    btn_connect.setText(Language.getText("LB_CONNECT"));
                    getContext().gotoLobbyScreen();
                }

                @Override
                public void onFail(String message) {
                    setEnabled(true);
                    btn_connect.setText(Language.getText("LB_CONNECT"));
                    getContext().showMessage(Language.getText("MSG_ERR_CCS"), null);
                }
            });
        }
    }

    public void setSelectedIndex(int index) {
        server_list.setSelectedIndex(index);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
