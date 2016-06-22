package net.toyknight.aeii.concurrent;

import net.toyknight.aeii.network.NetworkManager;

/**
 * @author toyknight 9/7/2015.
 */
public abstract class MessageSendingTask extends AsyncTask<Void> {

    private final String message;

    public MessageSendingTask(String message) {
        this.message = message;
    }

    public final String getMessage() {
        return message;
    }

    @Override
    public final Void doTask() {
        NetworkManager.sendMessage(getMessage());
        return null;
    }

}
