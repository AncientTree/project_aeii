package com.toyknight.aeii.net.task;

/**
 * Created by toyknight on 8/26/2015.
 */
public interface NetworkTask {

    boolean doTask() throws Exception;

    void onFinish();

    void onFail(String message);

}
