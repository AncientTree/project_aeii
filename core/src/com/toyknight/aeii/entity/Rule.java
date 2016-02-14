package com.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;

/**
 * @author toyknight 4/15/2015.
 */
public class Rule implements Serializable {

    private static final long serialVersionUID = 4152015L;

    public static final int POISON_DAMAGE = 10;
    public static final int HEALER_BASE_HEAL = 40;
    public static final int REFRESH_BASE_HEAL = 10;

    private final ObjectMap<String, Object> values;

    private final Array<Integer> available_units;

    public Rule() {
        values = new ObjectMap<String, Object>();
        available_units = new Array<Integer>();
    }

    public Rule(Rule rule) {
        values = new ObjectMap<String, Object>(rule.getValues());
        available_units = new Array<Integer>(rule.getAvailableUnits());
    }

    protected ObjectMap<String, Object> getValues() {
        return values;
    }

    public Array<Integer> getAvailableUnits() {
        return available_units;
    }

    public void setAvailableUnits(Array<Integer> list) {
        available_units.clear();
        available_units.addAll(list);
    }

    public void addAvailableUnit(int index) {
        available_units.add(index);
    }

    public void setValue(String entry, Object value) {
        values.put(entry, value);
    }

    public int getInteger(String entry) {
        return getInteger(entry, 0);
    }

    public int getInteger(String entry, int default_value) {
        Object value = values.get(entry, null);
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return default_value;
        }
    }

    public boolean getBoolean(String entry) {
        return getBoolean(entry, false);
    }

    public boolean getBoolean(String entry, boolean default_value) {
        Object value = values.get(entry, null);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return default_value;
        }
    }

    public String getString(String entry) {
        return getString(entry, null);
    }

    public String getString(String entry, String default_value) {
        Object value = values.get(entry, null);
        if (value instanceof String) {
            return (String) value;
        } else {
            return default_value;
        }
    }

    public static Rule createDefault() {
        Rule rule = new Rule();

        rule.setValue(Entry.CASTLE_INCOME, 100);
        rule.setValue(Entry.VILLAGE_INCOME, 50);
        rule.setValue(Entry.COMMANDER_INCOME, 50);
        rule.setValue(Entry.KILL_EXPERIENCE, 60);
        rule.setValue(Entry.ATTACK_EXPERIENCE, 30);
        rule.setValue(Entry.COUNTER_EXPERIENCE, 10);
        rule.setValue(Entry.COMMANDER_PRICE_STEP, 100);
        rule.setValue(Entry.MAX_POPULATION, 20);
        rule.setValue(Entry.ENEMY_CLEAR, true);
        rule.setValue(Entry.CASTLE_CLEAR, true);

        rule.setAvailableUnits(getDefaultUnits());

        return rule;
    }

    private static Array<Integer> getDefaultUnits() {
        int commander = UnitFactory.getCommanderIndex();
        int skeleton = UnitFactory.getSkeletonIndex();
        int crystal = UnitFactory.getCrystalIndex();
        Array<Integer> unit_list = new Array<Integer>();
        for (int index = 0; index < UnitFactory.getUnitCount(); index++) {
            if (index != commander && index != skeleton && index != crystal) {
                unit_list.add(index);
            }
        }
        //sort unit list
        for (int i = unit_list.size - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (UnitFactory.getSample(unit_list.get(j)).getPrice() >
                        UnitFactory.getSample(unit_list.get(j + 1)).getPrice()) {
                    unit_list.swap(j, j + 1);
                }
            }
        }
        unit_list.add(commander);
        return unit_list;
    }

    public class Entry {

        public static final String CASTLE_INCOME = "CASTLE_INCOME";
        public static final String VILLAGE_INCOME = "VILLAGE_INCOME";
        public static final String COMMANDER_INCOME = "COMMANDER_INCOME";
        public static final String KILL_EXPERIENCE = "KILL_EXPERIENCE";
        public static final String ATTACK_EXPERIENCE = "ATTACK_EXPERIENCE";
        public static final String COUNTER_EXPERIENCE = "COUNTER_EXPERIENCE";
        public static final String COMMANDER_PRICE_STEP = "COMMANDER_PRICE_STEP";
        public static final String MAX_POPULATION = "MAX_POPULATION";
        public static final String ENEMY_CLEAR = "ENEMY_CLEAR";
        public static final String CASTLE_CLEAR = "CASTLE_CLEAR";

    }

}
