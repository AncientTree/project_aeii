package com.toyknight.aeii.manager;

import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.animator.MessageAnimator;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.events.*;
import com.toyknight.aeii.net.task.GameEventSendingTask;
import com.toyknight.aeii.utils.Language;
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

    private static boolean is_game_over;

    private static AEIIApplication context;
    private static GameManager manager;

    private GameHost() {
    }

    public static void setContext(AEIIApplication context) {
        GameHost.context = context;
    }

    public static void setGameOver(boolean b) {
        GameHost.is_game_over = b;
    }

    public static boolean isGameOver() {
        return is_game_over;
    }

    public static void setGameManager(GameManager manager) {
        GameHost.manager = manager;
        GameHost.is_game_over = false;
    }

    public static AEIIApplication getContext() {
        return context;
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
        if (getManager().canSelectedUnitMove(dest_x, dest_y)) {
            int start_x = getManager().getSelectedUnit().getX();
            int start_y = getManager().getSelectedUnit().getY();
            int mp_remains = getManager().getMovementPointRemains(dest_x, dest_y);
            ArrayList<Point> move_path = getManager().getMovePath(dest_x, dest_y);
            dispatchEvent(new UnitMoveEvent(start_x, start_y, dest_x, dest_y, mp_remains, move_path));
        }
    }

    public static void doReverseMove() {
        Point last_position = getManager().getLastPosition();
        Unit selected_unit = getManager().getSelectedUnit();
        dispatchEvent(
                new UnitMoveReverseEvent(selected_unit.getX(), selected_unit.getY(), last_position.x, last_position.y));

    }

    public static void doAttack(int target_x, int target_y) {
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
    }

    public static void doSummon(int target_x, int target_y) {
        Unit summoner = getManager().getSelectedUnit();
        if (UnitToolkit.isWithinRange(summoner, target_x, target_y) && getGame().canSummon(target_x, target_y)) {
            int experience = getGame().getRule().getAttackExperience();
            dispatchEvent(new SummonEvent(summoner.getX(), summoner.getY(), target_x, target_y, experience));
        }
    }

    public static void doHeal(int target_x, int target_y) {
        Unit healer = getManager().getSelectedUnit();
        if (getGame().canHeal(healer, target_x, target_y)) {
            int heal = UnitToolkit.getHeal(healer);
            int experience = getGame().getRule().getAttackExperience();
            dispatchEvent(new UnitHealEvent(healer.getX(), healer.getY(), target_x, target_y, heal, experience));
        }
    }

    public static void doRepair() {
        Unit unit = getManager().getSelectedUnit();
        dispatchEvent(new RepairEvent(unit.getX(), unit.getY()));
    }

    public static void doOccupy() {
        Unit unit = getManager().getSelectedUnit();
        dispatchEvent(new OccupyEvent(unit.getX(), unit.getY(), unit.getTeam()));
    }

    public static void doBuyUnit(int index, int x, int y) {
        int team = getGame().getCurrentTeam();
        dispatchEvent(new UnitBuyEvent(index, team, x, y, getGame().getUnitPrice(index, team)));
    }

    public static void doStandbyUnit() {
        Unit unit = getManager().getSelectedUnit();
        if (getGame().isUnitAccessible(unit)) {
            dispatchEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
        }
    }

    public static void doEndTurn() {
        int next_team = getGame().getNextTeam();
        dispatchEvent(new TurnEndEvent());
        //calculate hp change at turn start
        HashSet<Point> unit_position_set = new HashSet<Point>(getGame().getMap().getUnitPositionSet());
        HashMap<Point, Integer> hp_change_map = new HashMap<Point, Integer>();
        Set<Point> unit_position_set_copy = new HashSet<Point>(unit_position_set);
        for (Point position : unit_position_set_copy) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == next_team) {
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
            Unit healer = getGame().getMap().getUnit(position.x, position.y);
            if (healer.hasAbility(Ability.HEALING_AURA)) {
                Set<Point> attackable_positions = getManager().createAttackablePositions(healer);
                attackable_positions.add(getGame().getMap().getPosition(healer.getX(), healer.getY()));
                for (Point target_position : attackable_positions) {
                    //there's a unit at the position
                    if (unit_position_set.contains(target_position)) {
                        //see if this unit already has hp change
                        if (hp_change_map.keySet().contains(target_position)) {
                            int change = hp_change_map.get(target_position) + 15;
                            hp_change_map.put(target_position, change);
                        } else {
                            hp_change_map.put(target_position, 15);
                        }
                    } else {
//                        Unit target = getGame().getMap().getUnit(target_position.x, target_position.y);
//                        if (!getGame().isEnemy(healer, target)) {
//                            hp_change_map.put(target_position, 15);
//                        }
                    }
                }
            }
        }

        dispatchEvent(new HpChangeEvent(hp_change_map)

        );
        // pre-calculate unit that will be destroyed
        for (
                Point position
                : hp_change_map.keySet())

        {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit.getCurrentHp() + hp_change_map.get(position) <= 0) {
                dispatchEvent(new UnitDestroyEvent(unit.getX(), unit.getY()));
            }
        }

        dispatchEvent(new UnitStatusUpdateEvent(next_team)

        );
    }

    public static void updateGameStatus() {
        //default rule temporarily
        //get population
        int[] population = new int[4];
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (player != null) {
                population[team] = getGame().getMap().getUnitCount(team, true);
            }
        }

        //get castle count
        int[] castle_count = new int[4];
        for (int team = 0; team < 4; team++) {
            castle_count[team] = getGame().getMap().getCastleCount(team);
        }

        //remove failed player
        for (int team = 0; team < 4; team++) {
            if (population[team] == 0 && castle_count[team] == 0 && getGame().getPlayer(team) != null) {
                getGame().removePlayer(team);
            }
        }

        //check winning status
        int alliance = -1;
        boolean winning_flag = true;
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (player != null) {
                if (alliance == -1) {
                    alliance = player.getAlliance();
                } else {
                    winning_flag = player.getAlliance() == alliance;
                    if (!winning_flag) {
                        break;
                    }
                }
            }
        }
        if (winning_flag) {
            getManager().submitAnimation(new MessageAnimator(Language.getText("LB_TEAM") + " " + alliance + " " + Language.getText("LB_WIN") + "!", 1.5f));
            GameHost.setGameOver(true);
        }
    }

    public static void dispatchEvent(GameEvent event) {
        if (getContext().getNetworkManager().isConnected()) {
            getContext().getNetworkManager().postTask(new GameEventSendingTask(event));
        }
        getManager().queueGameEvent(event);
    }

}
