package com.toyknight.aeii.entity.player;

/**
 * Created by toyknight on 4/3/2015.
 */
public class Player {

    private int alliance = 0;
    private int gold = 0;
    private int population = 0;

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getPopulation() {
        return population;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void addGold(int addition) {
        this.gold += addition;
    }

    public void reduceGold(int reduction) {
        if (gold > reduction) {
            this.gold -= reduction;
        } else {
            this.gold = 0;
        }
    }

    public int getGold() {
        return gold;
    }

    public void setAlliance(int alliance) {
        this.alliance = alliance;
    }

    public int getAlliance() {
        return alliance;
    }

}
