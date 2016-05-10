package com.toyknight.aeii.concurrent;

import com.badlogic.gdx.Gdx;

/**
 * @author toyknight 8/26/2015.
 */
public abstract class AsyncTask<T> implements Runnable {

    private static final String TAG = "Task";

    private String message;

    abstract public T doTask() throws Exception;

    abstract public void onFinish(T result);

    abstract public void onFail(String message);

    @Override
    public final void run() {
        try {
            final T result = doTask();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    onFinish(result);
                }
            });
        } catch (Exception e) {
            message = e.getMessage();
            Gdx.app.log(TAG, e.toString());
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    onFail(message);
                }
            });
        }
    }

}
