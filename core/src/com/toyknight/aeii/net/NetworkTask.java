package com.toyknight.aeii.net;

import java.io.IOException;

/**
 * Created by toyknight on 8/26/2015.
 */
public interface NetworkTask {

    void doTask() throws IOException;

    void onFinish();

    void onFail(String message);

}
