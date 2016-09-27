package net.toyknight.aeii.network.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 9/27/2016.
 */
public class LeaderboardRecord implements Serializable {

    private final int turns;
    private final String username_turns;
    private final int actions;
    private final String username_actions;

    public LeaderboardRecord(int turns, String username_turns, int actions, String username_actions) {
        this.turns = turns;
        this.username_turns = username_turns;
        this.actions = actions;
        this.username_actions = username_actions;
    }

    public LeaderboardRecord(JSONObject json) throws JSONException {
        this(json.getInt("turns"), json.getString("username_turns"),
                json.getInt("actions"), json.getString("username_actions"));
    }

    public int getTurns() {
        return turns;
    }

    public String getUsernameTurns() {
        return username_turns;
    }

    public int getActions() {
        return actions;
    }

    public String getUsernameActions() {
        return username_actions;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("turns", turns);
        json.put("username_turns", username_turns);
        json.put("actions", actions);
        json.put("username_actions", username_actions);
        return json;
    }

}
