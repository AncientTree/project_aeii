package com.toyknight.aeii.robot;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * @author toyknight 2/3/2016.
 */
public class BattleData extends ObjectMap<Integer, Integer> {

    public static final int ENEMY_AVERAGE_POPULATION = 0x1;
    public static final int ENEMY_AIR_FORCE_COUNT = 0x2;
    public static final int ENEMY_DEBUFF_GIVER_COUNT = 0x3;
    public static final int ENEMY_AVERAGE_PHYSICAL_ATTACK = 0x4;
    public static final int ENEMY_AVERAGE_PHYSICAL_DEFENCE = 0x5;
    public static final int ENEMY_AVERAGE_MAGIC_ATTACK = 0x6;
    public static final int ENEMY_AVERAGE_MAGIC_DEFENCE = 0x7;
    public static final int ENEMY_AVERAGE_REACH_RANGE = 0x8;
    public static final int ENEMY_AVERAGE_TOTAL_VALUE = 0x9;
    public static final int UNIT_TOTAL_VALUE = 0x10;

    public void setValue(Integer key, int value) {
        put(key, value);
    }

    public int getValue(Integer key) {
        return get(key);
    }

}
