package net.toyknight.aeii.network.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 9/27/2016.
 */
public class LeaderboardRecord implements Serializable {
    private final String campaign_code;
    private final int stage_number;
    private final int turns;
    private final String username_turns;
    private final int actions;
    private final String username_actions;

    public LeaderboardRecord(
            String campaign_code, int stage_number,
            int turns, String username_turns, int actions, String username_actions) {
        this.campaign_code = campaign_code;
        this.stage_number = stage_number;
        this.turns = turns;
        this.username_turns = username_turns;
        this.actions = actions;
        this.username_actions = username_actions;
    }

    public LeaderboardRecord(JSONObject json) throws JSONException {
        this(json.getString("campaign_code"),
                json.getInt("stage_number"),
                json.getInt("turns"),
                json.getString("username_turns"),
                json.getInt("actions"),
                json.getString("username_actions"));
    }

    public String getCampaignCode() {
        return campaign_code;
    }

    public int getStageNumber() {
        return stage_number;
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
        json.put("campaign_code", campaign_code);
        json.put("stage_number", stage_number);
        json.put("turns", turns);
        json.put("username_turns", username_actions);
        json.put("actions", actions);
        json.put("username_actions", username_actions);
        return json;
    }

}
