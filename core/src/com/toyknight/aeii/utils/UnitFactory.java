package com.toyknight.aeii.utils;

import com.badlogic.gdx.utils.Json;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.entity.Unit;

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
                UnitFactory.class.getResourceAsStream("/com/toyknight/aeii/data/units/unit_config.json"));
        unit_config = json.fromJson(UnitConfiguration.class, configure_reader);
        default_units = new Unit[unit_config.unit_count];
        for (int index = 0; index < default_units.length; index++) {
            InputStreamReader unit_reader = new InputStreamReader(
                    UnitFactory.class.getResourceAsStream("/com/toyknight/aeii/data/units/unit_" + index + ".json"));
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
        return cloneUnit(default_units[index]);
    }

    public static Unit createUnit(int index, int team) {
        String unit_code = "#" + Long.toString(current_code++);
        return createUnit(index, team, unit_code);
    }

    public static Unit cloneUnit(Unit unit) {
        String unit_code = unit.getUnitCode();
        return new Unit(unit, unit_code);
    }

    public static Unit createUnit(int index, int team, String unit_code) {
        Unit unit = new Unit(default_units[index], unit_code);
        unit.setTeam(team);
        unit.setStandby(false);
        unit.setCurrentHp(unit.getMaxHp());
        unit.setCurrentMovementPoint(unit.getMovementPoint());
        return unit;
    }

    public static String getVerificationString() {
        String str = "";
        for (Unit unit : default_units) {
            str += unit.getVerificationString();
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
