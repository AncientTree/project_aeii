package net.toyknight.aeii;

/**
 * @author toyknight 4/3/2015.
 */
public class GameException extends Exception {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

}
