package com.toyknight.aeii.net;

/**
 * Created by toyknight on 8/26/2015.
 */
public class NetworkTaskFutureEvent implements Runnable {

    public static final int SUCCESS = 0x1;
    public static final int FAILURE = 0x2;

    private final NetworkTask task;
    private final String message;
    private final int result;

    public NetworkTaskFutureEvent(NetworkTask task, String message, int result) {
        this.task = task;
        this.message = message;
        this.result = result;
    }

    @Override
    public void run() {
        switch (result) {
            case SUCCESS:
                task.onFinish();
                break;
            case FAILURE:
                task.onFail(message);
                break;
            default:
                //do nothing
        }
    }

}
