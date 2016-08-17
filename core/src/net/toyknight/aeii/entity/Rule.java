package net.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.Serializable;
import net.toyknight.aeii.utils.UnitFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static net.toyknight.aeii.entity.Rule.Entry.*;
import static net.toyknight.aeii.entity.Rule.Entry.CASTLE_CLEAR;

/**
 * @author toyknight 4/15/2015.
 */
public class Rule implements Serializable {

    public static final Integer[] GOLD_PRESET = new Integer[]{200, 250, 300, 450, 500, 550, 700, 850, 1000, 1500, 2000};
    public static final Integer[] POPULATION_PRESET = new Integer[]{15, 20, 25, 30, 35, 40};

    public static final int POISON_DAMAGE = 10;
    public static final int HEALER_BASE_HEAL = 40;
    public static final int REFRESH_BASE_HEAL = 10;

    private final ObjectMap<String, Object> values;

    private final Array<Integer> available_units;

    public Rule() {
        values = new ObjectMap<String, Object>();
        available_units = new Array<Integer>();
    }

    public Rule(JSONObject json) throws JSONException {
        this();
        setValue(CASTLE_INCOME, json.getInt(CASTLE_INCOME));
        setValue(VILLAGE_INCOME, json.getInt(VILLAGE_INCOME));
        setValue(COMMANDER_INCOME, json.getInt(COMMANDER_INCOME));
        setValue(KILL_EXPERIENCE, json.getInt(KILL_EXPERIENCE));
        setValue(ATTACK_EXPERIENCE, json.getInt(ATTACK_EXPERIENCE));
        setValue(COUNTER_EXPERIENCE, json.getInt(COUNTER_EXPERIENCE));
        setValue(COMMANDER_PRICE_STEP, json.getInt(COMMANDER_PRICE_STEP));
        setValue(UNIT_CAPACITY, json.getInt(UNIT_CAPACITY));
        setValue(ENEMY_CLEAR, json.getBoolean(ENEMY_CLEAR));
        setValue(CASTLE_CLEAR, json.getBoolean(CASTLE_CLEAR));
        JSONArray available_units = json.getJSONArray("available_units");
        for (int i = 0; i < available_units.length(); i++) {
            addAvailableUnit(available_units.getInt(i));
        }
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
        rule.setValue(Entry.UNIT_CAPACITY, POPULATION_PRESET[0]);
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
            if (index != commander && index != skeleton && index != crystal && index != 16) {
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

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put(CASTLE_INCOME, getInteger(CASTLE_INCOME));
        json.put(VILLAGE_INCOME, getInteger(VILLAGE_INCOME));
        json.put(COMMANDER_INCOME, getInteger(COMMANDER_INCOME));
        json.put(KILL_EXPERIENCE, getInteger(KILL_EXPERIENCE));
        json.put(ATTACK_EXPERIENCE, getInteger(ATTACK_EXPERIENCE));
        json.put(COUNTER_EXPERIENCE, getInteger(COUNTER_EXPERIENCE));
        json.put(COMMANDER_PRICE_STEP, getInteger(COMMANDER_PRICE_STEP));
        json.put(UNIT_CAPACITY, getInteger(UNIT_CAPACITY));
        json.put(ENEMY_CLEAR, getBoolean(ENEMY_CLEAR));
        json.put(CASTLE_CLEAR, getBoolean(CASTLE_CLEAR));
        JSONArray available_units = new JSONArray();
        for (Integer index : getAvailableUnits()) {
            available_units.put(index);
        }
        json.put("available_units", available_units);
        return json;
    }

    public class Entry {

        public static final String CASTLE_INCOME = "CASTLE_INCOME";
        public static final String VILLAGE_INCOME = "VILLAGE_INCOME";
        public static final String COMMANDER_INCOME = "COMMANDER_INCOME";
        public static final String KILL_EXPERIENCE = "KILL_EXPERIENCE";
        public static final String ATTACK_EXPERIENCE = "ATTACK_EXPERIENCE";
        public static final String COUNTER_EXPERIENCE = "COUNTER_EXPERIENCE";
        public static final String COMMANDER_PRICE_STEP = "COMMANDER_PRICE_STEP";
        public static final String UNIT_CAPACITY = "UNIT_CAPACITY";
        public static final String ENEMY_CLEAR = "ENEMY_CLEAR";
        public static final String CASTLE_CLEAR = "CASTLE_CLEAR";

    }

}
