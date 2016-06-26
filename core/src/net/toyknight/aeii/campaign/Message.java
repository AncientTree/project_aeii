package net.toyknight.aeii.campaign;

/**
 * @author toyknight 6/24/2016.
 */
public class Message {

    private final int portrait;
    private final String message;

    public Message(int portrait, String message) {
        this.portrait = portrait;
        this.message = message;
    }

    public int getPortrait() {
        return portrait;
    }

    public String getMessage() {
        return message;
    }

}
