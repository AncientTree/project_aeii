package com.toyknight.aeii.network.entity;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 9/1/2015.
 */
public class PlayerSnapshot implements Serializable {

    public int id;

    public String username;

    public boolean is_host;

    public PlayerSnapshot(int id, String username) {
        this(id, username, false);
    }

    public PlayerSnapshot(int id, String username, boolean is_host) {
        this.id = id;
        this.username = username;
        this.is_host = is_host;
    }

    public PlayerSnapshot(JSONObject json) throws JSONException {
        id = json.getInt("id");
        username = json.getString("username");
        is_host = json.getBoolean("is_host");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("username", username);
        json.put("is_host", is_host);
        return json;
    }

    @Override
    public String toString() {
        if (is_host) {
            return username + " *";
        } else {
            return username;
        }
    }

}
