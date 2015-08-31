package com.toyknight.aeii.manager;

import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.events.*;
import com.toyknight.aeii.net.GameEventSendingTask;
import com.toyknight.aeii.net.NetworkTask;
import com.toyknight.aeii.net.OperationTask;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by toyknight on 7/27/2015.
 */
public class GameHost {

    public static final int OPT_ATTACK = 0x1;
    public static final int OPT_BUY = 0x2;
    public static final int OPT_END_TURN = 0x3;
    public static final int OPT_MOVE_UNIT = 0x4;
    public static final int OPT_OCCUPY = 0x5;
    public static final int OPT_REPAIR = 0x6;
    public static final int OPT_REVERSE_MOVE = 0x7;
    public static final int OPT_SELECT = 0x8;
    public static final int OPT_STANDBY = 0x9;
    public static final int OPT_SUMMON = 0x10;

    private static boolean is_host;
    private static boolean processing;
    private static boolean is_game_over;

    private static AEIIApplication context;
    private static GameManager manager;

    private GameHost() {
    }

    public static void setContext(AEIIApplication context) {
        GameHost.context = context;
    }

    public static void setHost(boolean b) {
        GameHost.is_host = b;
    }

    public static boolean isHost() {
        return is_host;
    }

    public static boolean isProcessing() {
        return processing;
    }

    public static void setGameOver(boolean b) {
        GameHost.is_game_over = b;
    }

    public static boolean isGameOver() {
        return is_game_over;
    }

    public static void setGameManager(GameManager manager) {
        GameHost.manager = manager;
        GameHost.processing = false;
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
        if (isHost()) {
            Unit unit = getGame().getMap().getUnit(x, y);
            if (getGame().isUnitAccessible(unit)) {
                dispatchEvent(new UnitSelectEvent(x, y));
            }
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_SELECT, x, y));
        }
    }

    public static void doMoveUnit(int dest_x, int dest_y) {
        if (isHost()) {
            if (getManager().getMovablePositions() == null) {
                getManager().createMovablePositions();
            }
            if (getManager().canSelectedUnitMove(dest_x, dest_y)) {
                int start_x = getManager().getSelectedUnit().getX();
                int start_y = getManager().getSelectedUnit().getY();
                int mp_remains = getManager().getMovementPointRemains(dest_x, dest_y);
                ArrayList<Point> move_path = getManager().getMovePath(dest_x, dest_y);
                dispatchEvent(new UnitMoveEvent(start_x, start_y, dest_x, dest_y, mp_remains, move_path));
            }
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_MOVE_UNIT, dest_x, dest_y));
        }
    }

    public static void doReverseMove() {
        if (isHost()) {
            Point last_position = getManager().getLastPosition();
            Unit selected_unit = getManager().getSelectedUnit();
            dispatchEvent(
                    new UnitMoveReverseEvent(selected_unit.getX(), selected_unit.getY(), last_position.x, last_position.y));
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_REVERSE_MOVE));
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
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_ATTACK, target_x, target_y));
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
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_SUMMON, target_x, target_y));
        }
    }

    public static void doRepair() {
        if (isHost()) {
            Unit unit = getManager().getSelectedUnit();
            dispatchEvent(new RepairEvent(unit.getX(), unit.getY()));
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_REPAIR));
        }
    }

    public static void doOccupy() {
        if (isHost()) {
            Unit unit = getManager().getSelectedUnit();
            dispatchEvent(new OccupyEvent(unit.getX(), unit.getY(), unit.getTeam()));
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_OCCUPY));
        }
    }

    public static void doBuyUnit(int index, int x, int y) {
        if (isHost()) {
            int team = getGame().getCurrentTeam();
            dispatchEvent(new UnitBuyEvent(index, team, x, y, getGame().getUnitPrice(index, team)));
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_BUY, index, x, y));
        }
    }

    public static void doStandbyUnit() {
        if (isHost()) {
            Unit unit = getManager().getSelectedUnit();
            if (getGame().isUnitAccessible(unit)) {
                dispatchEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
            }
        } else {
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_STANDBY));
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
            processing = true;
            getManager().getListener().onButtonUpdateRequested();
            getContext().getNetworkManager().postTask(new OperationTask(OPT_END_TURN));
        }
    }

    public static void updateGameStatus() {
        //default rule temporarily
        if (isHost()) {
            //get population
            int[] population = new int[4];
            for (int team = 0; team < 4; team++) {
                Player player = getGame().getPlayer(team);
                if (player != null) {
                    population[team] = player.getPopulation();
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
                    dispatchEvent(new PlayerRemoveEvent(team));
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
                    }
                }
            }
            if (winning_flag == true) {
                dispatchEvent(new GameOverEvent(alliance));
            }
        }
    }

    public static void dispatchEvent(GameEvent event) {
        if (isHost()) {
            if (getContext().getNetworkManager().isConnected()) {
                getContext().getNetworkManager().postTask(new GameEventSendingTask(event));
            }
            getManager().submitGameEvent(event);
        } else {
            getManager().submitGameEvent(event);
            if (processing) {
                processing = false;
                getManager().getListener().onButtonUpdateRequested();
            }
        }
    }

}
