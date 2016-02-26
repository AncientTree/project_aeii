package com.toyknight.aeii.network.entity;

import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.Serializable;
import com.toyknight.aeii.entity.GameCore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 8/31/2015.
 */
public class RoomSetting implements Serializable {

    public long room_number;

    public int host;

    public boolean started;

    public Array<PlayerSnapshot> players;

    public GameCore game;

    public Integer[] allocation;

    public int start_gold;

    public int max_population;

    public RoomSetting() {
    }

    public RoomSetting(JSONObject json) throws JSONException {
        room_number = json.getLong("room_number");
        host = json.getInt("host");
        started = json.getBoolean("started");
        players = new Array<PlayerSnapshot>();
        for (int i = 0; i < json.getJSONArray("players").length(); i++) {
            players.add(new PlayerSnapshot(json.getJSONArray("players").getJSONObject(i)));
        }
        game = new GameCore(json.getJSONObject("game"));
        allocation = new Integer[4];
        for (int team = 0; team < 4; team++) {
            allocation[team] = json.getJSONArray("allocation").getInt(team);
        }
        start_gold = json.getInt("start_gold");
        max_population = json.getInt("max_population");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("room_number", room_number);
        json.put("host", host);
        json.put("started", started);
        JSONArray players = new JSONArray();
        for (PlayerSnapshot player : this.players) {
            players.put(player.toJson());
        }
        json.put("players", players);
        json.put("game", game.toJson());
        JSONArray allocation = new JSONArray();
        for (int team = 0; team < 4; team++) {
            allocation.put(this.allocation[team]);
        }
        json.put("allocation", allocation);
        json.put("start_gold", start_gold);
        json.put("max_population", max_population);
        return json;
    }

}
