package com.toyknight.aeii.manager;

import com.toyknight.aeii.AEIIApplet;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.events.*;
import com.toyknight.aeii.net.task.GameEventSendingTask;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author toyknight 7/27/2015.
 */
public class GameHost {

    private static AEIIApplet context;

    private static GameManager manager;

    private static boolean is_game_over;

    private GameHost() {
    }

    public static void setContext(AEIIApplet context) {
        GameHost.context = context;
    }

    public static void setGameManager(GameManager manager) {
        GameHost.manager = manager;
        GameHost.is_game_over = false;
    }

    public static AEIIApplet getContext() {
        return context;
    }

    public static GameManager getManager() {
        return manager;
    }

    public static GameCore getGame() {
        return manager.getGame();
    }

    public static boolean isGameOver() {
        return is_game_over;
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
                if (getGame().canAttack(attacker, target_x, target_y)) {
                    //attack pre-calculation
                    Unit real_attacker = UnitFactory.cloneUnit(UnitToolkit.getAttacker(attacker, defender));
                    Unit real_defender = UnitFactory.cloneUnit(UnitToolkit.getDefender(attacker, defender));
                    int attack_damage = UnitToolkit.getDamage(real_attacker, real_defender, getGame().getMap());
                    UnitToolkit.attachAttackStatus(real_attacker, real_defender);
                    real_defender.changeCurrentHp(-attack_damage);
                    if (real_defender.getCurrentHp() > 0) {
                        real_attacker.gainExperience(attack_experience);
                        dispatchEvent(new UnitAttackEvent(real_attacker.getX(), real_attacker.getY(), real_defender.getX(), real_defender.getY(), attack_damage, attack_experience));
                        if (UnitToolkit.canCounter(real_defender, real_attacker) || UnitToolkit.isAttackAmbushed(attacker, defender)) {
                            int counter_damage = UnitToolkit.getDamage(real_defender, real_attacker, getGame().getMap());
                            real_attacker.changeCurrentHp(-counter_damage);
                            if (real_attacker.getCurrentHp() > 0) {
                                dispatchEvent(new UnitAttackEvent(real_defender.getX(), real_defender.getY(), real_attacker.getX(), real_attacker.getY(), counter_damage, counter_experience));
                            } else {
                                dispatchEvent(new UnitAttackEvent(real_defender.getX(), real_defender.getY(), real_attacker.getX(), real_attacker.getY(), counter_damage, kill_experience));
                            }
                        }
                    } else {
                        dispatchEvent(new UnitAttackEvent(real_attacker.getX(), real_attacker.getY(), real_defender.getX(), real_defender.getY(), attack_damage, kill_experience));
                    }
                }
            }
        }
    }

    public static void doSummon(int target_x, int target_y) {
        Unit summoner = getManager().getSelectedUnit();
        if (getGame().canSummon(summoner, target_x, target_y)) {
            int experience = getGame().getRule().getAttackExperience();
            dispatchEvent(new SummonEvent(summoner.getX(), summoner.getY(), target_x, target_y, experience));
        }
    }

    public static void doHeal(int target_x, int target_y) {
        Unit healer = getManager().getSelectedUnit();
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canHeal(healer, target_x, target_y)) {
            int heal = UnitToolkit.getHeal(healer, target);
            int experience = target.getCurrentHp() + heal > 0 ?
                    getGame().getRule().getAttackExperience() : getGame().getRule().getKillExperience();
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

        dispatchEvent(new UnitStatusUpdateEvent(next_team));

        //calculate hp change at turn start
        HashMap<Point, Integer> hp_change_map = new HashMap<Point, Integer>();

        //terrain and poison hp change
        for (Point position : getGame().getMap().getUnitPositionSet()) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == next_team) {
                //the terrain heal
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                int change = UnitToolkit.getTerrainHeal(unit, tile);
                //the poison damage
                if (unit.hasStatus(Status.POISONED) && unit.getStatus().getRemainingTurn() > 0) {
                    if (unit.hasAbility(Ability.UNDEAD)) {
                        change += getGame().getRule().getPoisonDamage();
                    } else {
                        change = -getGame().getRule().getPoisonDamage();
                    }
                }
                if (unit.hasAbility(Ability.REHABILITATION)) {
                    change += unit.getMaxHp() / 4;
                }
                hp_change_map.put(position, change);
            } else {
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                if (getGame().isEnemy(unit.getTeam(), next_team) && tile.isCastle() && tile.getTeam() == next_team) {
                    hp_change_map.put(position, -50);
                }
            }
        }

        dispatchEvent(new HpChangeEvent(hp_change_map));

        // pre-calculate unit that will be destroyed
        for (Point position : hp_change_map.keySet()) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit != null && unit.getCurrentHp() + hp_change_map.get(position) <= 0) {
                dispatchEvent(new UnitDestroyEvent(unit.getX(), unit.getY()));
            }
        }
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
            if (population[team] == 0 && castle_count[team] == 0
                    && getGame().getPlayer(team) != null && getGame().getPlayer(team).getType() != Player.NONE) {
                getGame().removePlayer(team);
            }
        }

        //check winning status
        int alliance = -1;
        boolean winning_flag = true;
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (player != null && player.getType() != Player.NONE) {
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
            is_game_over = true;
        }
    }

    public static void dispatchEvent(GameEvent event) {
        if (getContext().getNetworkManager().isConnected()) {
            getContext().submitAsyncTask(new GameEventSendingTask(event));
        }
        getManager().submitGameEvent(event);
    }

}
