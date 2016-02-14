package com.toyknight.aeii.utils;

import static com.toyknight.aeii.entity.Rule.Entry.*;

import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Rule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 2/13/2016.
 */
public class Serializer {

    private Serializer() {
    }

    public static JSONObject toJson(GameSave save) {
        JSONObject json = new JSONObject();
        json.put("type", save.getType());
        json.put("game", toJson(save.getGame()));
        return json;
    }

    public static GameSave toSave(JSONObject json) throws AEIIException {
        try {
            int type = json.getInt("type");
            GameCore game = toGame(json.getJSONObject("game"));
            return new GameSave(game, type);
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

    public static JSONObject toJson(GameCore game) {
        JSONObject json = new JSONObject();
        json.put("type", game.getType());
        json.put("map", toJson(game.getMap()));
        json.put("rule", toJson(game.getRule()));
        JSONArray players = new JSONArray();
        JSONArray commanders = new JSONArray();
        JSONArray team_destroy = new JSONArray();
        for (int team = 0; team < 4; team++) {
            players.put(toJson(game.getPlayer(team)));
            team_destroy.put(game.isTeamDestroyed(team));
            commanders.put(toJson(game.getCommander(team)));
        }
        json.put("players", players);
        json.put("commanders", commanders);
        json.put("team_destroy", team_destroy);
        json.put("current_turn", game.getCurrentTurn());
        json.put("current_team", game.getCurrentTeam());
        json.put("game_over", game.isGameOver());
        json.put("statistics", toJson(game.getStatistics()));
        json.put("initialized", game.isInitialized());
        return json;
    }

    public static GameCore toGame(JSONObject json) throws AEIIException {
        try {
            Map map = toMap(json.getJSONObject("map"));
            Rule rule = toRule(json.getJSONObject("rule"));
            int type = json.getInt("type");
            GameCore game = new GameCore(map, rule, 0, type);
            game.setCurrentTurn(json.getInt("current_turn"));
            game.setCurrentTeam(json.getInt("current_team"));
            game.setGameOver(json.getBoolean("game_over"));
            game.setInitialized(json.getBoolean("initialized"));
            JSONArray players = json.getJSONArray("players");
            JSONArray commanders = json.getJSONArray("commanders");
            JSONArray team_destroy = json.getJSONArray("team_destroy");
            JSONArray income = json.getJSONObject("statistics").getJSONArray("income");
            JSONArray destroy = json.getJSONObject("statistics").getJSONArray("destroy");
            JSONArray lose = json.getJSONObject("statistics").getJSONArray("lose");
            for (int team = 0; team < 4; team++) {
                JSONObject player = players.getJSONObject(team);
                game.getPlayer(team).setType(player.getInt("type"));
                game.getPlayer(team).setGold(player.getInt("gold"));
                game.getPlayer(team).setAlliance(player.getInt("alliance"));
                game.getPlayer(team).setPopulation(player.getInt("population"));
                game.setTeamDestroyed(team, team_destroy.getBoolean(team));
                game.getStatistics().addIncome(team, income.getInt(team));
                game.getStatistics().addDestroy(team, destroy.getInt(team));
                game.getStatistics().addLose(team, lose.getInt(team));
                game.setCommander(team, toUnit(commanders.getJSONObject(team)));
            }
            return game;
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

    public static JSONObject toJson(Map map) {
        JSONObject json = new JSONObject();
        json.put("author", map.getAuthor());
        json.put("width", map.getWidth());
        json.put("height", map.getHeight());
        JSONArray map_data = new JSONArray();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                map_data.put(map.getTileIndex(x, y));
            }
        }
        json.put("map_data", map_data);
        JSONArray units = new JSONArray();
        for (Unit unit : map.getUnits()) {
            units.put(toJson(unit));
        }
        json.put("units", units);
        JSONArray tombs = new JSONArray();
        for (Tomb tomb : map.getTombs()) {
            tombs.put(toJson(tomb));
        }
        json.put("tombs", tombs);
        JSONArray team_access = new JSONArray();
        for (int team = 0; team < 4; team++) {
            team_access.put(map.hasTeamAccess(team));
        }
        json.put("team_access", team_access);
        return json;
    }

    public static Map toMap(JSONObject json) throws AEIIException {
        try {
            int width = json.getInt("width");
            int height = json.getInt("height");
            Map map = new Map(width, height);
            map.setAuthor(json.getString("author"));
            JSONArray map_data = json.getJSONArray("map_data");
            int index = 0;
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    int tile = map_data.getInt(index++);
                    map.setTile((short) tile, x, y);
                }
            }
            JSONArray units = json.getJSONArray("units");
            for (int i = 0; i < units.length(); i++) {
                map.addUnit(toUnit(units.getJSONObject(i)));
            }
            JSONArray tombs = json.getJSONArray("tombs");
            for (int i = 0; i < tombs.length(); i++) {
                map.addTomb(toTomb(tombs.getJSONObject(i)));
            }
            JSONArray team_access = json.getJSONArray("team_access");
            for (int team = 0; team < 4; team++) {
                map.setTeamAccess(team, team_access.getBoolean(team));
            }
            return map;
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

    public static JSONObject toJson(Unit unit) {
        JSONObject json = new JSONObject();
        json.put("index", unit.getIndex());
        json.put("price", unit.getPrice());
        json.put("experience", unit.getTotalExperience());
        json.put("unit_code", unit.getUnitCode());
        json.put("team", unit.getTeam());
        json.put("current_hp", unit.getCurrentHp());
        json.put("current_movement_point", unit.getCurrentMovementPoint());
        json.put("x_position", unit.getX());
        json.put("y_position", unit.getY());
        json.put("standby", unit.isStandby());
        if (unit.getStatus() != null) {
            json.put("status", toJson(unit.getStatus()));
        }
        return json;
    }

    public static Unit toUnit(JSONObject json) throws AEIIException {
        try {
            int index = json.getInt("index");
            int team = json.getInt("team");
            String unit_code = json.getString("unit_code");
            Unit unit = UnitFactory.createUnit(index, team, unit_code);
            unit.setPrice(json.getInt("price"));
            unit.gainExperience(json.getInt("experience"));
            unit.setCurrentHp(json.getInt("current_hp"));
            unit.setCurrentMovementPoint(json.getInt("current_movement_point"));
            unit.setX(json.getInt("x_position"));
            unit.setY(json.getInt("y_position"));
            unit.setStandby(json.getBoolean("standby"));
            if (json.has("status")) {
                unit.setStatus(toStatus(json.getJSONObject("status")));
            }
            return unit;
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

    public static JSONObject toJson(Rule rule) {
        JSONObject json = new JSONObject();
        json.put(CASTLE_INCOME, rule.getInteger(CASTLE_INCOME));
        json.put(VILLAGE_INCOME, rule.getInteger(VILLAGE_INCOME));
        json.put(COMMANDER_INCOME, rule.getInteger(COMMANDER_INCOME));
        json.put(KILL_EXPERIENCE, rule.getInteger(KILL_EXPERIENCE));
        json.put(ATTACK_EXPERIENCE, rule.getInteger(ATTACK_EXPERIENCE));
        json.put(COUNTER_EXPERIENCE, rule.getInteger(COUNTER_EXPERIENCE));
        json.put(COMMANDER_PRICE_STEP, rule.getInteger(COMMANDER_PRICE_STEP));
        json.put(MAX_POPULATION, rule.getInteger(MAX_POPULATION));
        json.put(ENEMY_CLEAR, rule.getBoolean(ENEMY_CLEAR));
        json.put(CASTLE_CLEAR, rule.getBoolean(CASTLE_CLEAR));
        JSONArray available_units = new JSONArray();
        for (Integer index : rule.getAvailableUnits()) {
            available_units.put(index);
        }
        json.put("available_units", available_units);
        return json;
    }

    public static Rule toRule(JSONObject json) throws AEIIException {
        try {
            Rule rule = new Rule();
            rule.setValue(CASTLE_INCOME, json.getInt(CASTLE_INCOME));
            rule.setValue(VILLAGE_INCOME, json.getInt(VILLAGE_INCOME));
            rule.setValue(COMMANDER_INCOME, json.getInt(COMMANDER_INCOME));
            rule.setValue(KILL_EXPERIENCE, json.getInt(KILL_EXPERIENCE));
            rule.setValue(ATTACK_EXPERIENCE, json.getInt(ATTACK_EXPERIENCE));
            rule.setValue(COUNTER_EXPERIENCE, json.getInt(COUNTER_EXPERIENCE));
            rule.setValue(COMMANDER_PRICE_STEP, json.getInt(COMMANDER_PRICE_STEP));
            rule.setValue(MAX_POPULATION, json.getInt(MAX_POPULATION));
            JSONArray available_units = json.getJSONArray("available_units");
            for (int i = 0; i < available_units.length(); i++) {
                rule.addAvailableUnit(available_units.getInt(i));
            }
            return rule;
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

    public static JSONObject toJson(Player player) {
        JSONObject json = new JSONObject();
        json.put("type", player.getType());
        json.put("gold", player.getGold());
        json.put("alliance", player.getAlliance());
        json.put("population", player.getPopulation());
        return json;
    }

    public static JSONObject toJson(Statistics statistics) {
        JSONObject json = new JSONObject();
        JSONArray income = new JSONArray();
        JSONArray destroy = new JSONArray();
        JSONArray lose = new JSONArray();
        for (int team = 0; team < 4; team++) {
            income.put(statistics.getIncome(team));
            destroy.put(statistics.getDestroy(team));
            lose.put(statistics.getLost(team));
        }
        json.put("income", income);
        json.put("destroy", destroy);
        json.put("lose", lose);
        return json;
    }

    public static JSONObject toJson(Tomb tomb) {
        JSONObject json = new JSONObject();
        json.put("x", tomb.x);
        json.put("y", tomb.y);
        json.put("remains", tomb.getRemains());
        return json;
    }

    public static Tomb toTomb(JSONObject json) throws AEIIException {
        try {
            int x = json.getInt("x");
            int y = json.getInt("y");
            int remains = json.getInt("remains");
            Tomb tomb = new Tomb(x, y);
            tomb.setRemains(remains);
            return tomb;
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

    public static JSONObject toJson(Status status) {
        JSONObject json = new JSONObject();
        json.put("type", status.getType());
        json.put("remaining_turn", status.getRemainingTurn());
        return json;
    }

    public static Status toStatus(JSONObject json) throws AEIIException {
        try {
            int type = json.getInt("type");
            int remaining_turn = json.getInt("remaining_turn");
            Status status = new Status(type);
            status.setRemainingTurn(remaining_turn);
            return status;
        } catch (JSONException ex) {
            throw new AEIIException(ex.getMessage(), ex);
        }
    }

}
