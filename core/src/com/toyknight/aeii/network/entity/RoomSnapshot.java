package com.toyknight.aeii.network.entity;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 8/27/2015.
 */
public class RoomSnapshot implements Serializable {

    public long room_number;

    public boolean open;

    public String room_name;

    public String map_name;

    public int capacity;

    public int remaining;

    public RoomSnapshot() {
    }

    public RoomSnapshot(JSONObject json) throws JSONException {
        room_number = json.getLong("room_number");
        open = json.getBoolean("open");
        room_name = json.getString("room_name");
        map_name = json.getString("map_name");
        capacity = json.getInt("capacity");
        remaining = json.getInt("remaining");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("room_number", room_number);
        json.put("open", open);
        json.put("room_name", room_name);
        json.put("map_name", map_name);
        json.put("capacity", capacity);
        json.put("remaining", remaining);
        return json;
    }

    @Override
    public String toString() {
        String str = room_name + " | Map: " + map_name + " | (" + (capacity - remaining) + "/" + capacity + ")";
        if (!open) {
            str += " - Started";
        }
        return str;
    }

}
