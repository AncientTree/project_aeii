package com.toyknight.aeii.utils;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Map;

import java.util.*;

/**
 * Created by toyknight on 4/3/2015.
 */
public class UnitToolkit {

    private static final Random random = new Random(System.currentTimeMillis());

    public static void attachAttackStatus(Unit attacker, Unit defender) {
        if (attacker.hasAbility(Ability.POISONER)) {
            defender.attachStatus(new Status(Status.POISONED, 1));
            return;
        }
        if (attacker.hasAbility(Ability.SLOWING_GAZE) && !defender.hasAbility(Ability.SLOWING_GAZE)) {
            defender.attachStatus(new Status(Status.PETRIFACTED, 1));
            defender.setCurrentMovementPoint(defender.getMovementPoint());
        }
    }

    public static int getTerrainHeal(Unit unit, Tile tile) {
        int heal = 0;
        if (tile.getTeam() == -1) {
            heal += tile.getHpRecovery();
        } else {
            if (unit.getTeam() == tile.getTeam()) {
                heal += tile.getHpRecovery();
            }
        }
        if (unit.hasAbility(Ability.SON_OF_THE_MOUNTAIN) && tile.getType() == Tile.TYPE_MOUNTAIN) {
            heal += 10;
        }
        if (unit.hasAbility(Ability.SON_OF_THE_FOREST) && tile.getType() == Tile.TYPE_FOREST) {
            heal += 10;
        }
        if (unit.hasAbility(Ability.SON_OF_THE_SEA) && tile.getType() == Tile.TYPE_WATER) {
            heal += 10;
        }
        return heal;
    }

    public static int getMovementPointCost(Unit unit, Tile tile) {
        int mp_cost = tile.getStepCost();
        int tile_type = tile.getType();
        if (unit.hasAbility(Ability.AIR_FORCE)) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.CRAWLER)
                && (tile_type == Tile.TYPE_LAND || tile_type == Tile.TYPE_FOREST || tile_type == Tile.TYPE_MOUNTAIN)) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.FIGHTER_OF_THE_SEA) && tile_type == Tile.TYPE_WATER) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.FIGHTER_OF_THE_FOREST) && tile_type == Tile.TYPE_FOREST) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN) && tile_type == Tile.TYPE_MOUNTAIN) {
            mp_cost = 1;
        }
        if (mp_cost < 1) {
            mp_cost = 1;
        }
        return mp_cost;
    }

    public static boolean isTheSameUnit(Unit unit_a, Unit unit_b) {
        if (unit_a == null || unit_b == null) {
            return false;
        } else {
            if (unit_a.isAt(unit_b.getX(), unit_b.getY())) {
                return unit_a.getUnitCode().equals(unit_b.getUnitCode());
            } else {
                return false;
            }
        }
    }

    public static boolean isWithinRange(Unit unit, int target_x, int target_y) {
        int range = getRange(unit.getX(), unit.getY(), target_x, target_y);
        return unit.getMinAttackRange() <= range && range <= unit.getMaxAttackRange();
    }

    public static boolean canCounter(Unit counter, Unit attacker) {
        return getRange(counter.getX(), counter.getY(), attacker.getX(), attacker.getY()) == 1
                && isWithinRange(counter, attacker.getX(), attacker.getY());
    }

    private static int getRange(int unit_x, int unit_y, int target_x, int target_y) {
        return Math.abs(target_x - unit_x) + Math.abs(target_y - unit_y);
    }

    public static int getRange(Unit unit_a, Unit unit_b) {
        return getRange(unit_a.getX(), unit_a.getY(), unit_b.getX(), unit_b.getY());
    }

    public static int getDefenceBonus(Unit unit, int tile_index) {
        int defence_bonus = 0;
        Tile tile = TileFactory.getTile(tile_index);
        if (!unit.hasAbility(Ability.AIR_FORCE)) {
            defence_bonus += tile.getDefenceBonus();
        }
        switch (tile.getType()) {
            case Tile.TYPE_FOREST:
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_FOREST)) {
                    defence_bonus += 10;
                }
            case Tile.TYPE_MOUNTAIN:
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN)) {
                    defence_bonus += 10;
                }
            case Tile.TYPE_WATER:
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_SEA)) {
                    defence_bonus += 10;
                }
            default:
                //do nothing
        }
        return defence_bonus;
    }

    public static int getAttackBonus(Unit attacker, Unit defender, int tile_index) {
        int bonus = 0;
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN)
                && TileFactory.getTile(tile_index).getType() == Tile.TYPE_MOUNTAIN) {
            bonus += 10;
        }
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_FOREST)
                && TileFactory.getTile(tile_index).getType() == Tile.TYPE_FOREST) {
            bonus += 10;
        }
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_SEA)
                && TileFactory.getTile(tile_index).getType() == Tile.TYPE_WATER) {
            bonus += 10;
        }
        if (attacker.hasAbility(Ability.MARKSMAN) && defender.hasAbility(Ability.AIR_FORCE)) {
            bonus += 10;
        }
        return bonus;
    }

    public static int getDamage(Unit attacker, Unit defender, Map map) {
        int attacker_tile_index = map.getTileIndex(attacker.getX(), attacker.getY());
        int defender_tile_index = map.getTileIndex(defender.getX(), defender.getY());
        //calculate attack bonus
        int attack_bonus = getAttackBonus(attacker, defender, attacker_tile_index);
        int attack = attacker.getAttack() + attack_bonus;
        //calculate defence bonus
        int defence_bonus = getDefenceBonus(defender, defender_tile_index);
        int defence = attacker.getAttackType() == Unit.ATTACK_PHYSICAL
                ? defender.getPhysicalDefence() : defender.getMagicalDefence();
        defence += defence_bonus;
        //calculate base damage
        int damage = attack > defence ? attack - defence : 0;
        int attacker_hp = attacker.getCurrentHp();
        int attacker_max_hp = attacker.getMaxHp();
        //calculate random damage offset
        int offset = random.nextInt(5) - 2;
        //calculate final damage
        damage = damage * attacker_hp / attacker_max_hp + offset;
        damage = damage > 0 ? damage : 0;
        if (defender.hasAbility(Ability.LORD_OF_TERROR) && getRange(attacker, defender) > 1) {
            damage /= 2;
        }
        if (attacker.hasAbility(Ability.LORD_OF_TERROR) && getRange(attacker, defender) == 1) {
            damage = damage * 3 / 2;
        }
        //validate damage
        damage = damage < defender.getCurrentHp() ? damage : defender.getCurrentHp();
        return damage;
    }

    public static boolean canMoveAgain(Unit unit) {
        return unit.getCurrentHp() > 0
                && unit.getCurrentMovementPoint() > 0
                && unit.hasAbility(Ability.CHARGER);
    }

}
