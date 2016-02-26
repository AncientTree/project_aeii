package com.toyknight.aeii.net.serializable;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 10/27/2015.
 */
public class Response implements Serializable {

    private final long id;

    private final JSONObject content;

    public Response(JSONObject json) throws JSONException {
        this.id = json.getInt("id");
        this.content = json.getJSONObject("content");
    }

    public Response(long id) {
        this.id = id;
        this.content = new JSONObject();
    }

    public long getRequestID() {
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
        json.put("id", getRequestID());
        json.put("content", content);
        return json;
    }
}
