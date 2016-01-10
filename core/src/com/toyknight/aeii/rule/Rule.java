package com.toyknight.aeii.rule;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;

/**
 * @author toyknight 4/15/2015.
 */
public class Rule implements Serializable {

    private static final long serialVersionUID = 4152015L;

    public static int POISON_DAMAGE = 10;
    public static int HEALER_BASE_HEAL = 40;
    public static int REFRESH_BASE_HEAL = 10;

    private final ObjectMap<Integer, Object> values;

    private final Array<Integer> available_units;

    public Rule() {
        values = new ObjectMap<Integer, Object>();
        available_units = new Array<Integer>();
    }

    public Rule(Rule rule) {
        values = new ObjectMap<Integer, Object>(rule.getValues());
        available_units = new Array<Integer>(rule.getAvailableUnits());
    }

    protected ObjectMap<Integer, Object> getValues() {
        return values;
    }

    public Array<Integer> getAvailableUnits() {
        return available_units;
    }

    public void setValue(Integer entry, Object value) {
        values.put(entry, value);
    }

    public int getInteger(Integer entry) {
        return getInteger(entry, 0);
    }

    public int getInteger(Integer entry, int default_value) {
        Object value = values.get(entry, null);
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return default_value;
        }
    }

    public boolean getBoolean(Integer entry) {
        return getBoolean(entry, false);
    }

    public boolean getBoolean(Integer entry, boolean default_value) {
        Object value = values.get(entry, null);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return default_value;
        }
    }

    public String getString(Integer entry) {
        return getString(entry, null);
    }

    public String getString(Integer entry, String default_value) {
        Object value = values.get(entry, null);
        if (value instanceof String) {
            return (String) value;
        } else {
            return default_value;
        }
    }

    public void setAvailableUnits(Array<Integer> list) {
        available_units.clear();
        available_units.addAll(list);
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

        public static final int CASTLE_INCOME = 0x1;
        public static final int VILLAGE_INCOME = 0x2;
        public static final int COMMANDER_INCOME = 0x3;
        public static final int KILL_EXPERIENCE = 0x4;
        public static final int ATTACK_EXPERIENCE = 0x5;
        public static final int COUNTER_EXPERIENCE = 0x6;
        public static final int COMMANDER_PRICE_STEP = 0x7;
        public static final int MAX_POPULATION = 0x8;
        public static final int ENEMY_CLEAR = 0x9;
        public static final int CASTLE_CLEAR = 0x10;

    }

}
