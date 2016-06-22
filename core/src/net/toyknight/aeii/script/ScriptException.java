package net.toyknight.aeii.script;

/**
 * @author toyknight 10/20/2015.
 */
public class ScriptException extends Exception {

    public ScriptException(String message) {
        this(message, null);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }

}
