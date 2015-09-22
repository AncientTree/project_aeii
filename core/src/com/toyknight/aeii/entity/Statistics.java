package com.toyknight.aeii.entity;

import java.io.Serializable;

/**
 * Created by toyknight on 9/22/2015.
 */
public class Statistics implements Serializable {

    private static final long serialVersionUID = 9222015L;

    private final int[] income;
    private final int[] destroy;
    private final int[] lose;

    public Statistics() {
        income = new int[4];
        destroy = new int[4];
        lose = new int[4];
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

}
