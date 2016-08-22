package net.toyknight.aeii.utils;

import com.badlogic.gdx.utils.Json;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.entity.Unit;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;

/**
 * @author toyknight 4/3/2015.
 */
public class UnitFactory {

    private static Unit[] default_units;
    private static long current_code;

    private static UnitConfiguration unit_config;

    private UnitFactory() {
    }

    public static void loadUnitData() throws AEIIException {
        Json json = new Json();
        InputStreamReader configure_reader = new InputStreamReader(
                UnitFactory.class.getResourceAsStream("/data/units/unit_config.json"));
        unit_config = json.fromJson(UnitConfiguration.class, configure_reader);
        default_units = new Unit[unit_config.unit_count];
        for (int index = 0; index < default_units.length; index++) {
            InputStreamReader unit_reader = new InputStreamReader(
                    UnitFactory.class.getResourceAsStream("/data/units/unit_" + index + ".json"));
            Unit.UnitDefinition definition = json.fromJson(Unit.UnitDefinition.class, unit_reader);
            default_units[index] = new Unit(definition, index);
        }
    }

    public static int getCommanderIndex() {
        return unit_config.commander_index;
    }

    public static int getSkeletonIndex() {
        return unit_config.skeleton_index;
    }

    public static int getCrystalIndex() {
        return unit_config.crystal_index;
    }

    public static int getUnitCount() {
        return default_units.length;
    }

    public static Unit getSample(int index) {
        return default_units[index];
    }

    public static Unit createUnit(JSONObject json) throws JSONException {
        int index = json.getInt("index");
        int team = json.getInt("team");
        String unit_code = json.getString("unit_code");
        Unit unit = createUnit(index, team, unit_code);
        unit.setPrice(json.getInt("price"));
        unit.gainExperience(json.getInt("experience"));
        unit.setCurrentHp(json.getInt("current_hp"));
        unit.setCurrentMovementPoint(json.getInt("current_movement_point"));
        unit.setX(json.getInt("x_position"));
        unit.setY(json.getInt("y_position"));
        unit.setStandby(json.getBoolean("standby"));
        unit.setStatic(json.has("static") && json.getBoolean("static"));
        unit.setHead(json.has("head") ? json.getInt("head") : 0);
        if (json.has("status")) {
            unit.setStatus(new Status(json.getJSONObject("status")));
        }
        return unit;
    }

    public static Unit createUnit(int index, int team) {
        String unit_code = "#" + Long.toString(current_code++);
        return createUnit(index, team, unit_code);
    }

    public static Unit createUnit(int index, int team, String unit_code) {
        Unit unit = new Unit(default_units[index], unit_code);
        unit.setTeam(team);
        unit.setHead(team);
        unit.setStandby(false);
        unit.resetMovementPoint();
        unit.setCurrentHp(unit.getMaxHp());
        return unit;
    }

    public static Unit createCommander(int team) {
        return createUnit(getCommanderIndex(), team);
    }

    public static Unit cloneUnit(Unit unit) {
        if (unit == null) {
            return null;
        } else {
            return new Unit(unit, unit.getUnitCode());
        }
    }

    public static String getVerificationString() {
        String str = "";
        for (Unit unit : default_units) {
            str += unit.getVerification();
        }
        return str;
    }

    private static class UnitConfiguration {

        public int unit_count;

        public int commander_index;

        public int skeleton_index;

        public int crystal_index;

    }

}
