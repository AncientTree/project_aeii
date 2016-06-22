package net.toyknight.aeii.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class Player implements Serializable {

    public static final int NONE = 0x0;
    public static final int LOCAL = 0x1;
    public static final int ROBOT = 0x3;
    public static final int REMOTE = 0x2;
    public static final int RECORD = 0x4;

    private int type;
    private int gold = 0;
    private int alliance = 0;

    private int population = 0;

    public Player() {
    }

    public Player(Player player) {
        type = player.getType();
        gold = player.getGold();
        alliance = player.getAlliance();
        population = player.getPopulation();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void changeGold(int change) {
        this.gold += change;
    }

    public int getGold() {
        return gold;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getPopulation() {
        return population;
    }

    public void setAlliance(int alliance) {
        this.alliance = alliance;
    }

    public int getAlliance() {
        return alliance;
    }

    public boolean isLocalPlayer() {
        return getType() == LOCAL || getType() == ROBOT;
    }

    public static Player createPlayer(int type, int alliance, int gold, int population) {
        Player player = new Player();
        player.setType(type);
        player.setGold(gold);
        player.setAlliance(alliance);
        player.setPopulation(population);
        return player;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", getType());
        json.put("gold", getGold());
        json.put("alliance", getAlliance());
        json.put("population", getPopulation());
        return json;
    }

}
