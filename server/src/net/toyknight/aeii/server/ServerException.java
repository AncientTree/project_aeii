package net.toyknight.aeii.server;

/**
 * @author toyknight 8/16/2016.
 */
public class ServerException extends Exception {

    private final String tag;

    public ServerException(String tag, String message) {
        super(message);
        this.tag = tag;
    }

    public ServerException(String tag, String message, Throwable cause) {
        super(message, cause);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

}
