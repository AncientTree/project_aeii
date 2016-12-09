package net.toyknight.aeii.manager;

import net.toyknight.aeii.GameException;

/**
 * @author toyknight 6/23/2016.
 */
public class CheatingException extends GameException {

    private final int team;

    public CheatingException(String message, int team) {
        super(message);
        this.team = team;
    }

    public int getTeam() {
        return team;
    }

}
