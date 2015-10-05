package com.toyknight.aeii.utils;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Map;

import java.util.*;

/**
 * @author toyknight 4/3/2015.
 */
public class UnitToolkit {

    private static final Random random = new Random(System.currentTimeMillis());

    private static GameCore game;

    public static void setGame(GameCore game) {
        UnitToolkit.game = game;
    }

    public static GameCore getGame() {
        return game;
    }

    public static void attachAttackStatus(Unit attacker, Unit defender) {
        if (!defender.hasAbility(Ability.HEAVY_MACHINE)) {
            if (attacker.hasAbility(Ability.POISONER)) {
                defender.attachStatus(new Status(Status.POISONED, 2));
                return;
            }
            if (attacker.hasAbility(Ability.SLOWING_MASTER) && !defender.hasAbility(Ability.SLOWING_MASTER)) {
                defender.attachStatus(new Status(Status.SLOWED, 1));
                defender.setCurrentMovementPoint(defender.getMovementPoint());
            }
            if (attacker.hasAbility(Ability.BLINDER) && !defender.hasAbility(Ability.BLINDER)) {
                defender.attachStatus(new Status(Status.BLINDED, 1));
            }
        }
    }

    public static int getTerrainHeal(Unit unit, Tile tile) {
        int heal = 0;
        if (!unit.hasAbility(Ability.CRAWLER)) {
            if (tile.getTeam() == -1) {
                heal += tile.getHpRecovery();
            } else {
                if (!getGame().isEnemy(unit.getTeam(), tile.getTeam())) {
                    heal += tile.getHpRecovery();
                }
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
        return !(unit_a == null || unit_b == null) && unit_a.isAt(unit_b.getX(), unit_b.getY()) && unit_a.getUnitCode().equals(unit_b.getUnitCode());
    }

    public static boolean isWithinRange(Unit unit, int target_x, int target_y) {
        int range = getRange(unit.getX(), unit.getY(), target_x, target_y);
        return unit.getMinAttackRange() <= range && range <= unit.getMaxAttackRange();
    }

    public static boolean isWithinRange(Unit unit, Unit target) {
        return isWithinRange(unit, target.getX(), target.getY());
    }

    public static boolean canCounter(Unit counter, Unit attacker) {
        if (getGame().isEnemy(counter, attacker)) {
            if (counter.hasAbility(Ability.COUNTER_MADNESS)) {
                return getRange(counter.getX(), counter.getY(), attacker.getX(), attacker.getY()) <= 2;
            } else {
                return getRange(counter.getX(), counter.getY(), attacker.getX(), attacker.getY()) == 1
                        && isWithinRange(counter, attacker.getX(), attacker.getY());
            }
        } else {
            return false;
        }
    }

    private static int getRange(int unit_x, int unit_y, int target_x, int target_y) {
        return Math.abs(target_x - unit_x) + Math.abs(target_y - unit_y);
    }

    public static int getRange(Unit unit_a, Unit unit_b) {
        return getRange(unit_a.getX(), unit_a.getY(), unit_b.getX(), unit_b.getY());
    }

    public static int getTileDefenceBonus(Unit unit, int tile_index) {
        int defence_bonus = 0;
        Tile tile = TileFactory.getTile(tile_index);
        if (!unit.hasAbility(Ability.AIR_FORCE)) {
            defence_bonus += tile.getDefenceBonus();
        }
        if (unit.hasAbility(Ability.GUARDIAN)
                && getGame().getAlliance(unit.getTeam()) == getGame().getAlliance(tile.getTeam())) {
            defence_bonus += 5;
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

    public static int getPhysicalDefenceBonus(Unit attacker, Unit defender, int tile_index) {
        int defence_bonus = getTileDefenceBonus(defender, tile_index);
        if (defender.hasAbility(Ability.BLOODTHIRSTY)) {
            int enemy_count = getGame().getEnemyAroundCount(defender, 2);
            defence_bonus += enemy_count * 5;
        }
        return defence_bonus;
    }

    public static int getMagicDefenceBonus(Unit attacker, Unit defender, int tile_index) {
        int defence_bonus = getTileDefenceBonus(defender, tile_index);
        return defence_bonus;
    }

    public static int getAttackBonus(Unit attacker, Unit defender, int tile_index) {
        int attack_bonus = 0;
        Tile tile = TileFactory.getTile(tile_index);
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN) && tile.getType() == Tile.TYPE_MOUNTAIN) {
            attack_bonus += 10;
        }
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_FOREST) && tile.getType() == Tile.TYPE_FOREST) {
            attack_bonus += 10;
        }
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_SEA) && tile.getType() == Tile.TYPE_WATER) {
            attack_bonus += 10;
        }
        if (attacker.hasAbility(Ability.MARKSMAN) && defender.hasAbility(Ability.AIR_FORCE)) {
            attack_bonus += 15;
        }
        if (attacker.hasAbility(Ability.DESTROYER) && defender.hasAbility(Ability.CONQUEROR)) {
            attack_bonus += 10;
        }
        if (attacker.hasAbility(Ability.GUARDIAN)
                && getGame().getAlliance(attacker.getTeam()) == getGame().getAlliance(tile.getTeam())) {
            attack_bonus += 10;
        }
        if (attacker.hasAbility(Ability.BLOODTHIRSTY)) {
            int enemy_count = getGame().getEnemyAroundCount(attacker, 2);
            attack_bonus += enemy_count * 10;
        }
        if (attacker.hasStatus(Status.INSPIRED)) {
            attack_bonus += 10;
        }
        return attack_bonus;
    }

    public static int getDamage(Unit attacker, Unit defender, Map map) {
        int attacker_tile_index = map.getTileIndex(attacker.getX(), attacker.getY());
        int defender_tile_index = map.getTileIndex(defender.getX(), defender.getY());

        //calculate attack bonus
        int attack_bonus = getAttackBonus(attacker, defender, attacker_tile_index);
        int attack = attacker.getAttack() + attack_bonus;
        //calculate defence bonus
        int defence = attacker.getAttackType() == Unit.ATTACK_PHYSICAL
                ? defender.getPhysicalDefence() + getPhysicalDefenceBonus(attacker, defender, defender_tile_index)
                : defender.getMagicalDefence() + getMagicDefenceBonus(attacker, defender, defender_tile_index);
        //calculate base damage
        int damage = attack > defence ? attack - defence : 0;
        int attacker_hp = attacker.getCurrentHp();
        int attacker_max_hp = attacker.getMaxHp();
        //calculate random damage offset
        int offset = random.nextInt(5) - 2;
        //calculate final damage
        damage = damage * attacker_hp / attacker_max_hp;
        damage += offset;
        damage = damage > 0 ? damage : 0;
        //final damage percentage calculation
        float percentage_modifier = 1.0f;
        if (!attacker.hasAbility(Ability.LORD_OF_TERROR) || !defender.hasAbility(Ability.LORD_OF_TERROR)) {
            if (defender.hasAbility(Ability.LORD_OF_TERROR) && getRange(attacker, defender) > 1) {
                percentage_modifier -= 0.5f;
            }
            if (attacker.hasAbility(Ability.LORD_OF_TERROR) && getRange(attacker, defender) == 1) {
                percentage_modifier += 0.5f;
            }
        }
        percentage_modifier = percentage_modifier >= 0f ? percentage_modifier : 0f;
        damage = (int) (damage * percentage_modifier);
        //validate damage
        damage = damage < defender.getCurrentHp() ? damage : defender.getCurrentHp();
        return damage;
    }

    public static int getHeal(Unit unit, Unit target) {
        if (unit.hasAbility(Ability.HEALER)) {
            if (target.hasAbility(Ability.UNDEAD)) {
                return -(60 + 10 * unit.getLevel());
            } else {
                return 40 + 10 * unit.getLevel();
            }
        } else {
            return 0;
        }
    }

    public static boolean canMoveAgain(Unit unit) {
        return unit.getCurrentHp() > 0
                && unit.getCurrentMovementPoint() > 0
                && unit.hasAbility(Ability.CHARGER);
    }

}
