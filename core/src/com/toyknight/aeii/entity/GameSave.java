package com.toyknight.aeii.entity;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 9/17/2015.
 */
public class GameSave implements Serializable {

    private final int type;

    private final GameCore game;

    public GameSave(JSONObject json) throws JSONException {
        this(new GameCore(json.getJSONObject("game")), json.getInt("type"));
    }

    public GameSave(GameCore game, int type) {
        this.type = type;
        this.game = game;
    }

    public int getType() {
        return type;
    }

    public GameCore getGame() {
        return game;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", getType());
        json.put("game", getGame().toJson());
        return json;
    }

}
