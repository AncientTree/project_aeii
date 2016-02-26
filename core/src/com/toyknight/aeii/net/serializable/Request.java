
package com.toyknight.aeii.net.serializable;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight
 */
public class Request implements Serializable {

    public static final int AUTHENTICATION = 0x0;

    public static final int LIST_ROOMS = 0x1;

    public static final int JOIN_ROOM = 0x2;

    public static final int START_GAME = 0x3;

    public static final int CREATE_ROOM = 0x4;

    public static final int CREATE_ROOM_SAVED = 0x5;

    private final int type;

    private final long id;

    private final JSONObject content;

    public Request(JSONObject json) throws JSONException {
        this.id = json.getInt("id");
        this.type = json.getInt("type");
        this.content = json.getJSONObject("content");
    }

    public Request(int type, long id) {
        this.type = type;
        this.id = id;
        this.content = new JSONObject();
    }

    public int getType() {
        return type;
    }

    public long getID() {
        return id;
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
        json.put("id", getID());
        json.put("type", getType());
        json.put("content", content);
        return json;
    }

    public static Request getInstance(int type) {
        return new Request(type, System.currentTimeMillis());
    }

}
