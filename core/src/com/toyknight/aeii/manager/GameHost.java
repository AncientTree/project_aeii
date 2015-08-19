package com.toyknight.aeii.manager;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.events.*;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by toyknight on 7/27/2015.
 */
public class GameHost {

    private static boolean is_host;
    private static GameManager manager;

    private GameHost() {
    }

    public static void setHost(boolean b) {
        GameHost.is_host = b;
    }

    public static boolean isHost() {
        return is_host;
    }

    public static void setGameManager(GameManager manager) {
        GameHost.manager = manager;
    }

    private static GameManager getManager() {
        return manager;
    }

    private static GameCore getGame() {
        return manager.getGame();
    }

    public static void doSelect(int x, int y) {
        Unit unit = getGame().getMap().getUnit(x, y);
        if (getGame().isUnitAccessible(unit)) {
            dispatchEvent(new UnitSelectEvent(x, y));
        }
    }

    public static void doMoveUnit(int dest_x, int dest_y) {
        if (isHost()) {
            if (getManager().canSelectedUnitMove(dest_x, dest_y)) {
                int start_x = getManager().getSelectedUnit().getX();
                int start_y = getManager().getSelectedUnit().getY();
                int mp_remains = getManager().getMovementPointRemains(dest_x, dest_y);
                ArrayList<Point> move_path = getManager().getMovePath(dest_x, dest_y);
                dispatchEvent(new UnitMoveEvent(start_x, start_y, dest_x, dest_y, mp_remains, move_path));
            }
        } else {
            //send operation request to host
        }
    }

    public static void doReverseMove() {
        if (isHost()) {
            Point last_position = getManager().getLastPosition();
            Unit selected_unit = getManager().getSelectedUnit();
            dispatchEvent(
                    new UnitMoveReverseEvent(selected_unit.getX(), selected_unit.getY(), last_position.x, last_position.y));
        } else {
            //send operation request to host
        }
    }

    public static void doAttack(int target_x, int target_y) {
        if (isHost()) {
            Unit attacker = getManager().getSelectedUnit();
            if (UnitToolkit.isWithinRange(attacker, target_x, target_y)) {
                Unit defender = getGame().getMap().getUnit(target_x, target_y);
                int kill_experience = getGame().getRule().getKillExperience();
                int attack_experience = getGame().getRule().getAttackExperience();
                int counter_experience = getGame().getRule().getCounterExperience();
                if (defender == null) {
                    if (attacker.hasAbility(Ability.DESTROYER) && getGame().getMap().getTile(target_x, target_y).isDestroyable()) {
                        dispatchEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), target_x, target_y, -1, attack_experience));
                        dispatchEvent(new UnitStandbyEvent(attacker.getX(), attacker.getY()));
                        dispatchEvent(new TileDestroyEvent(target_x, target_y));
                    }
                } else {
                    if (getGame().isEnemy(attacker, defender)) {
                        //attack pre-calculation
                        attacker = UnitFactory.cloneUnit(attacker);
                        defender = UnitFactory.cloneUnit(defender);
                        int attack_damage = UnitToolkit.getDamage(attacker, defender, getGame().getMap());
                        UnitToolkit.attachAttackStatus(attacker, defender);
                        defender.changeCurrentHp(-attack_damage);
                        if (defender.getCurrentHp() > 0) {
                            attacker.gainExperience(attack_experience);
                            dispatchEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), attack_damage, attack_experience));
                            if (UnitToolkit.canCounter(defender, attacker)) {
                                int counter_damage = UnitToolkit.getDamage(defender, attacker, getGame().getMap());
                                attacker.changeCurrentHp(-counter_damage);
                                if (attacker.getCurrentHp() > 0) {
                                    dispatchEvent(new UnitAttackEvent(defender.getX(), defender.getY(), attacker.getX(), attacker.getY(), counter_damage, counter_experience));
                                } else {
                                    dispatchEvent(new UnitAttackEvent(defender.getX(), defender.getY(), attacker.getX(), attacker.getY(), counter_damage, kill_experience));
                                }
                            }
                        } else {
                            dispatchEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), attack_damage, kill_experience));
                        }
                    }
                }
            }
        } else {
            //send operation request to host
        }
    }

    public static void doSummon(int target_x, int target_y) {
        if (isHost()) {
            Unit summoner = getManager().getSelectedUnit();
            if (UnitToolkit.isWithinRange(summoner, target_x, target_y) && getGame().canSummon(target_x, target_y)) {
                int experience = getGame().getRule().getAttackExperience();
                dispatchEvent(new SummonEvent(summoner.getX(), summoner.getY(), target_x, target_y, experience));
            }
        } else {
            //send operation request to host
        }
    }

    public static void doRepair() {
        if (isHost()) {
            Unit unit = getManager().getSelectedUnit();
            dispatchEvent(new RepairEvent(unit.getX(), unit.getY()));
        } else {
            //send operation request to host
        }
    }

    public static void doOccupy() {
        if (isHost()) {
            Unit unit = getManager().getSelectedUnit();
            dispatchEvent(new OccupyEvent(unit.getX(), unit.getY(), unit.getTeam()));
        } else {
            //send operation request to host
        }
    }

    public static void doBuyUnit(String package_name, int index, int x, int y) {
        if (isHost()) {
            int team = getGame().getCurrentTeam();
            dispatchEvent(new UnitBuyEvent(package_name, index, team, x, y, getGame().getUnitPrice(package_name, index, team)));
        } else {
            //send operation request to host
        }
    }

    public static void doStandbyUnit() {
        if (isHost()) {
            Unit unit = getManager().getSelectedUnit();
            if (getGame().isUnitAccessible(unit)) {
                dispatchEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
            }
        } else {
            //send operation request to host
        }
    }

    public static void doEndTurn() {
        if (isHost()) {
            dispatchEvent(new TurnEndEvent());
            //calculate hp change at turn start
            int team = getGame().getCurrentTeam();
            HashSet<Point> unit_position_set = new HashSet(getGame().getMap().getUnitPositionSet());
            HashMap<Point, Integer> hp_change_map = new HashMap();
            Set<Point> unit_position_set_copy = new HashSet(unit_position_set);
            for (Point position : unit_position_set_copy) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.getTeam() == team) {
                    int change = 0;
                    //deal with terrain heal issues
                    change += UnitToolkit.getTerrainHeal(unit, getGame().getMap().getTile(unit.getX(), unit.getY()));
                    //deal with buff issues
                    if (unit.getStatus() != null && unit.getStatus().getType() == Status.POISONED) {
                        change -= getGame().getRule().getPoisonDamage();
                    }
                    hp_change_map.put(position, change);
                } else {
                    //remove other teams' unit position
                    unit_position_set.remove(position);
                }
            }
            //the healing aura
            for (Point position : unit_position_set) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.hasAbility(Ability.HEALING_AURA)) {
                    for (int x = unit.getX() - 1; x <= unit.getX() + 1; x++) {
                        for (int y = unit.getY() - 1; y <= unit.getY() + 1; y++) {
                            //not healer himself
                            if ((x != unit.getX() || y != unit.getY()) && getGame().getMap().isWithinMap(x, y)) {
                                Point target_position = getGame().getMap().getPosition(x, y);
                                //there's a unit at the position
                                if (unit_position_set.contains(target_position)) {
                                    //see if this unit already has hp change
                                    if (hp_change_map.keySet().contains(target_position)) {
                                        int change = hp_change_map.get(target_position) + 15;
                                        hp_change_map.put(target_position, change);
                                    } else {
                                        hp_change_map.put(target_position, 15);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            dispatchEvent(new HpChangeEvent(hp_change_map));
            // pre-calculate unit that will be destroyed
            for (Point position : hp_change_map.keySet()) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.getCurrentHp() + hp_change_map.get(position) <= 0) {
                    dispatchEvent(new UnitDestroyEvent(unit.getX(), unit.getY()));
                }
            }
            dispatchEvent(new UnitStatusUpdateEvent(team));
        } else {
            //send operation request to host
        }
    }

    private static void dispatchEvent(GameEvent event) {
        getManager().submitGameEvent(event);
        //sync operations
    }

}
