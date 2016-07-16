package net.toyknight.aeii.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * @author toyknight 9/17/2015.
 */
public class GameSave implements Serializable {

    private final int type;

    private final GameCore game;

    private final JSONObject attributes;

    public GameSave(JSONObject json) throws JSONException {
        this.type = json.getInt("type");
        this.game = new GameCore(json.getJSONObject("game"));
        this.attributes = json.getJSONObject("attributes");
    }

    public GameSave(GameCore game, int type) {
        this.type = type;
        this.game = game;
        this.attributes = new JSONObject();
    }

    public int getType() {
        return type;
    }

    public GameCore getGame() {
        return game;
    }

    public void putInteger(String key, int integer) {
        try {
            attributes.put(key, integer);
        } catch (JSONException ignored) {
        }
    }

    public int getInteger(String key, int default_value) {
        try {
            return attributes.getInt(key);
        } catch (JSONException ex) {
            return default_value;
        }
    }

    public void putString(String key, String str) {
        try {
            attributes.put(key, str);
        } catch (JSONException ignored) {
        }
    }

    public String getString(String key, String default_value) {
        try {
            return attributes.getString(key);
        } catch (JSONException ex) {
            return default_value;
        }
    }

    public Iterator<String> keys() {
        return attributes.keys();
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", getType());
        json.put("game", getGame().toJson());
        json.put("attributes", attributes);
        return json;
    }

}
