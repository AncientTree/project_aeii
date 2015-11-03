package com.toyknight.aeii.record;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.manager.GameEvent;

import java.io.Serializable;
import java.util.Queue;

/**
 * @author toyknight 9/22/2015.
 */
public class GameRecord implements Serializable {

    private static final long serialVersionUID = 11032015L;

    private final String V_STRING;

    private GameCore game;
    private Queue<GameEvent> event_queue;

    public GameRecord(String V_STRING) {
        this.V_STRING = V_STRING;
    }

    public String getVerificationString() {
        return V_STRING;
    }

    public void setGame(GameCore game) {
        this.game = game;
    }

    public GameCore getGame() {
        return game;
    }

    public void setEvents(Queue<GameEvent> events) {
        this.event_queue = events;
    }

    public Queue<GameEvent> getEvents() {
        return event_queue;
    }

}
