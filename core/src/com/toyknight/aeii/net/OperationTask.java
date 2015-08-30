package com.toyknight.aeii.net;

import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.utils.Language;

import java.io.IOException;

/**
 * Created by toyknight on 8/29/2015.
 */
public class OperationTask implements NetworkTask {

    protected final int request;
    protected final Integer[] params;

    public OperationTask(int request, Integer... params) {
        this.request = request;
        this.params = params;
    }

    @Override
    public boolean doTask() throws IOException {
        GameHost.getContext().getNetworkManager().sendInteger(NetworkManager.REQUEST);
        GameHost.getContext().getNetworkManager().sendInteger(Request.OPT_REQUEST);
        GameHost.getContext().getNetworkManager().sendInteger(request);
        for (int i = 0; i < params.length; i++) {
            GameHost.getContext().getNetworkManager().sendInteger(params[i]);
        }
        return true;
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void onFail(String message) {
        GameHost.getContext().showMessage(Language.getText("MSG_ERR_AEA"), new DialogCallback() {
            @Override
            public void doCallback() {
                GameHost.getContext().getNetworkManager().disconnect();
                GameHost.getContext().gotoMainMenuScreen();
            }
        });
    }

}
