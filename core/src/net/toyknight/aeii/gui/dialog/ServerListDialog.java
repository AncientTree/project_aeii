package net.toyknight.aeii.gui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.ServerConfiguration;
import net.toyknight.aeii.gui.MainMenuScreen;
import net.toyknight.aeii.gui.widgets.StringList;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 8/25/2015.
 */
public class ServerListDialog extends BasicDialog {

    private StringList<ServerConfiguration> server_list;
    private TextButton btn_connect;
    private TextButton btn_back;

    public ServerListDialog(MainMenuScreen screen) {
        super(screen);
        int width = ts * 7 + ts / 2;
        int height = ts * 6;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - 85 * ts / 48 - height) / 2, width, height);
        this.initComponents();
    }

    private void initComponents() {
        server_list = new StringList<ServerConfiguration>(getContext(), ts);
        ScrollPane sp_server_list = new ScrollPane(server_list, getContext().getSkin());
        sp_server_list.setBounds(ts / 2, ts * 2, getWidth() - ts, getHeight() - ts * 2 - ts / 2);
        sp_server_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_server_list.setScrollBarPositions(false, true);

        this.addActor(sp_server_list);

        Array<ServerConfiguration> servers = new Array<ServerConfiguration>();
//        servers.add(new ServerConfiguration("127.0.0.1", 5438, "aeii server - Local"));
        servers.add(new ServerConfiguration("112.74.215.26", 5438, "aeii server - China"));
        servers.add(new ServerConfiguration("45.56.93.69", 5438, "aeii server - NA"));
        server_list.setItems(servers);

        int width_btn = ts * 3;
        btn_connect = new TextButton(AER.lang.getText("LB_CONNECT"), getContext().getSkin());
        btn_connect.setBounds(ts / 2, ts / 2, width_btn, ts);
        btn_connect.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doConnect();
            }
        });
        addActor(btn_connect);

        btn_back = new TextButton(AER.lang.getText("LB_BACK"), getContext().getSkin());
        btn_back.setBounds(width_btn + ts, ts / 2, width_btn, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("server");
            }
        });
        addActor(btn_back);
    }

    private ServerConfiguration getSelectedServer() {
        return server_list.getSelected();
    }

    private void setEnabled(boolean enabled) {
        GameContext.setButtonEnabled(btn_connect, enabled);
        GameContext.setButtonEnabled(btn_back, enabled);
        server_list.setEnabled(enabled);
    }

    private void doConnect() {
        if (getSelectedServer().getAddress().length() > 0) {
            setEnabled(false);
            btn_connect.setText(AER.lang.getText("LB_CONNECTING"));

            getContext().submitAsyncTask(new AsyncTask<Boolean>() {
                @Override
                public Boolean doTask() throws Exception {
                    return NetworkManager.connect(
                            getSelectedServer(), getContext().getUsername(), getContext().getVerificationString());
                }

                @Override
                public void onFinish(Boolean approved) {
                    if (approved) {
                        setEnabled(true);
                        btn_connect.setText(AER.lang.getText("LB_CONNECT"));
                        getContext().gotoLobbyScreen();
                    } else {
                        setEnabled(true);
                        btn_connect.setText(AER.lang.getText("LB_CONNECT"));
                        getOwner().closeDialog("server");
                        NetworkManager.disconnect();
                        getOwner().showNotification(AER.lang.getText("MSG_ERR_RBS"), null);
                    }
                }

                @Override
                public void onFail(String message) {
                    setEnabled(true);
                    btn_connect.setText(AER.lang.getText("LB_CONNECT"));
                    getOwner().showNotification(AER.lang.getText("MSG_ERR_CCS"), null);
                }
            });
        }
    }

    public MainMenuScreen getOwner() {
        return (MainMenuScreen) super.getOwner();
    }

}
