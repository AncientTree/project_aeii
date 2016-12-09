package net.toyknight.aeii.system;

import com.badlogic.gdx.utils.Json;
import net.toyknight.aeii.GameException;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.entity.Unit;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;

/**
 * @author toyknight 12/9/2016.
 */
public class Units {

    private Configuration configuration;

    private Unit[] units;
    private long code;

    protected Units() {
    }

    public void initialize() throws GameException {
        //read configuration
        Json json = new Json();
        InputStreamReader configure_reader =
                new InputStreamReader(Units.class.getResourceAsStream("/data/units/unit_config.json"));
        configuration = json.fromJson(Configuration.class, configure_reader);

        //read unit data
        units = new Unit[configuration.unit_count];
        for (int index = 0; index < units.length; index++) {
            InputStreamReader unit_reader = new InputStreamReader(
                    Units.class.getResourceAsStream("/data/units/unit_" + index + ".json"));
            Unit.Definition definition = json.fromJson(Unit.Definition.class, unit_reader);
            units[index] = new Unit(definition, index);
        }
    }

    public int getCommanderIndex() {
        return configuration.commander_index;
    }

    public boolean isCommander(int index) {
        return index == getCommanderIndex();
    }

    public int getSkeletonIndex() {
        return configuration.skeleton_index;
    }

    public boolean isSkeleton(int index) {
        return index == getSkeletonIndex();
    }

    public int getCrystalIndex() {
        return configuration.crystal_index;
    }

    public boolean isCrystal(int index) {
        return index == getCrystalIndex();
    }

    public int getUnitCount() {
        return units.length;
    }

    public Unit getSample(int index) {
        return units[index];
    }

    public Unit createUnit(JSONObject json) throws JSONException {
        int index = json.getInt("index");
        int team = json.getInt("team");
        String unit_code = json.getString("unit_code");
        Unit unit = createUnit(index, team, unit_code);
        unit.changePrice(json.getInt("price_increment"));
        unit.gainExperience(json.getInt("experience"));
        unit.setCurrentHP(json.getInt("current_hp"));
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

    public Unit createUnit(int index, int team) {
        String unit_code = "#" + Long.toString(code++);
        return createUnit(index, team, unit_code);
    }

    public Unit createUnit(int index, int team, String unit_code) {
        Unit unit = new Unit(units[index], unit_code);
        unit.setTeam(team);
        unit.setHead(team);
        unit.setStandby(false);
        unit.resetMovementPoint();
        unit.setCurrentHP(unit.getMaxHP());
        return unit;
    }

    public Unit createCommander(int team) {
        return createUnit(configuration.commander_index, team);
    }

    public Unit cloneUnit(Unit unit) {
        if (unit == null) {
            return null;
        } else {
            return new Unit(unit, unit.getUnitCode());
        }
    }

    public String getVerificationString() {
        String str = "";
        for (Unit unit : units) {
            str += unit.getVerification();
        }
        return str;
    }

    private static class Configuration {

        public int unit_count;

        public int commander_index;

        public int skeleton_index;

        public int crystal_index;

    }

}
