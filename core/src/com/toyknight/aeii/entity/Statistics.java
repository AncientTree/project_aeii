package com.toyknight.aeii.entity;

import com.toyknight.aeii.Serializable;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author toyknight 9/22/2015.
 */
public class Statistics implements Serializable {

    private final int[] income;
    private final int[] destroy;
    private final int[] lose;

    public Statistics() {
        income = new int[4];
        destroy = new int[4];
        lose = new int[4];
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
        return json;
    }

}
