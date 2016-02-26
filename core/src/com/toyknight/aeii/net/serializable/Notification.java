package com.toyknight.aeii.net.serializable;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 10/28/2015.
 */
public class Notification implements Serializable {

    public static final int PLAYER_JOINING = 0x1;

    public static final int PLAYER_LEAVING = 0x2;

    public static final int UPDATE_ALLOCATION = 0x3;

    public static final int GAME_START = 0x5;

    public static final int GAME_EVENT = 0x6;

    public static final int MESSAGE = 0x7;

    private final int type;

    private final JSONObject content;

    public Notification(JSONObject json) throws JSONException {
        this.type = json.getInt("type");
        this.content = json.getJSONObject("content");
    }

    public Notification(int type) {
        this.type = type;
        this.content = new JSONObject();
    }

    public int getType() {
        return type;
    }

    public void setParameter(String name, Object parameter) {
        content.put(name, parameter);
    }

    public Object getParameter(String name) throws JSONException {
        return content.get(name);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", getType());
        json.put("content", content);
        return json;
    }

}
