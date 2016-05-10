package com.toyknight.aeii.record;

import com.toyknight.aeii.Serializable;
import com.toyknight.aeii.entity.GameCore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 9/22/2015.
 */
public class GameRecord implements Serializable {

    private final String V_STRING;

    private GameCore game;
    private Queue<JSONObject> event_queue;

    public GameRecord(JSONObject json) throws JSONException {
        this(json.getString("v_string"));
        setGame(new GameCore(json.getJSONObject("game")));
        JSONArray events = json.getJSONArray("events");
        event_queue = new LinkedList<JSONObject>();
        for (int i = 0; i < events.length(); i++) {
            event_queue.add(events.getJSONObject(i));
        }
    }

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

    public void setEvents(Queue<JSONObject> events) {
        this.event_queue = events;
    }

    public Queue<JSONObject> getEvents() {
        return event_queue;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("v_string", V_STRING);
        json.put("game", getGame().toJson());
        JSONArray events = new JSONArray();
        Queue<JSONObject> temp_queue = new LinkedList<JSONObject>(getEvents());
        while (temp_queue.size() > 0) {
            events.put(temp_queue.poll());
        }
        json.put("events", events);
        return json;
    }
}
