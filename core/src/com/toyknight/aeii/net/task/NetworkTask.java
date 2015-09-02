package com.toyknight.aeii.net.task;

import com.badlogic.gdx.Gdx;

/**
 * Created by toyknight on 8/26/2015.
 */
public abstract class NetworkTask<T> implements Runnable {

    private T result;

    abstract public T doTask() throws Exception;

    abstract public void onFinish(T result);

    abstract public void onFail(String message);

    @Override
    public final void run() {
        try {
            result = doTask();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    onFinish(result);
                }
            });
        } catch (Exception e) {
            onFail(e.getMessage());
        }
    }

}
