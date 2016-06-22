package net.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;

/**
 * @author toyknight 4/3/2015.
 */
public class Ability {

    public static final int CONQUEROR = 0;
    public static final int FIGHTER_OF_THE_SEA = 1;
    public static final int FIGHTER_OF_THE_FOREST = 2;
    public static final int FIGHTER_OF_THE_MOUNTAIN = 3;
    public static final int DESTROYER = 4;
    public static final int AIR_FORCE = 5;
    public static final int NECROMANCER = 6;
    public static final int HEALER = 7;
    public static final int CHARGER = 8;
    public static final int POISONER = 9;
    public static final int REPAIRER = 10;
    public static final int UNDEAD = 11;
    public static final int MARKSMAN = 12;
    public static final int SON_OF_THE_SEA = 13;
    public static final int SON_OF_THE_FOREST = 14;
    public static final int SON_OF_THE_MOUNTAIN = 15;
    public static final int CRAWLER = 16;
    public static final int SLOWING_AURA = 17;
    public static final int COMMANDER = 18;
    public static final int HEAVY_MACHINE = 19;
    public static final int ATTACK_AURA = 20;
    public static final int BLOODTHIRSTY = 21;
    public static final int GUARDIAN = 22;
    public static final int REFRESH_AURA = 23;
    public static final int LORD_OF_TERROR = 24;
    public static final int COUNTER_MADNESS = 25;
    public static final int BLINDER = 26;
    public static final int REHABILITATION = 27;
    public static final int AMBUSH = 28;

    public static Array<Integer> getAllAbilities() {
        Array<Integer> abilities = new Array<Integer>();
        for (int i = 0; i < 29; i++) {
            abilities.add(i);
        }
        return abilities;
    }

}
