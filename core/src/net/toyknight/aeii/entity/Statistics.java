package net.toyknight.aeii.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 9/22/2015.
 */
public class Statistics implements Serializable {

    private final int[] income;
    private final int[] destroy;
    private final int[] lose;
    private String save_name;

    public Statistics() {
        income = new int[4];
        destroy = new int[4];
        lose = new int[4];
        save_name = null;
    }

    public Statistics(Statistics statistics) {
        income = new int[4];
        destroy = new int[4];
        lose = new int[4];
        for (int team = 0; team < 4; team++) {
            income[team] = statistics.getIncome(team);
            destroy[team] = statistics.getDestroy(team);
            lose[team] = statistics.getLost(team);
        }
        save_name = null;
    }

    public void initialize(JSONObject json) throws JSONException {
        JSONArray income = json.getJSONArray("income");
        JSONArray destroy = json.getJSONArray("destroy");
        JSONArray lose = json.getJSONArray("lose");
        for (int team = 0; team < 4; team++) {
            addIncome(team, income.getInt(team));
            addDestroy(team, destroy.getInt(team));
            addLose(team, lose.getInt(team));
        }
        save_name = json.has("save_name") ? json.getString("save_name") : null;
    }

    public void addIncome(int team, int income) {
        this.income[team] += income;
    }

    public void addDestroy(int team, int value) {
        this.destroy[team] += value;
    }

    public void addLose(int team, int value) {
        this.lose[team] += value;
    }

    public int getIncome(int team) {
        return income[team];
    }

    public int getDestroy(int team) {
        return destroy[team];
    }

    public int getLost(int team) {
        return lose[team];
    }

    public void setSaveName(String save_name) {
        this.save_name = save_name;
    }

    public String getSaveName() {
        return save_name;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray income = new JSONArray();
        JSONArray destroy = new JSONArray();
        JSONArray lose = new JSONArray();
        for (int team = 0; team < 4; team++) {
            income.put(getIncome(team));
            destroy.put(getDestroy(team));
            lose.put(getLost(team));
        }
        json.put("income", income);
        json.put("destroy", destroy);
        json.put("lose", lose);
        if (save_name != null) {
            json.put("save_name", save_name);
        }
        return json;
    }

}
