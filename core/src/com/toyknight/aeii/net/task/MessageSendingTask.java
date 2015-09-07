package com.toyknight.aeii.net.task;

/**
 * Created by toyknight on 9/7/2015.
 */
public abstract class MessageSendingTask extends NetworkTask<Void> {

    protected final String message;

    public MessageSendingTask(String message) {
        this.message = message;
    }

}
