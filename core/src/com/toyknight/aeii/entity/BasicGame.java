package com.toyknight.aeii.entity;

import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.entity.player.Player;
import com.toyknight.aeii.event.*;
import com.toyknight.aeii.listener.GameListener;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by toyknight on 4/3/2015.
 */
public class BasicGame {

    private final int poison_damage = 10;

    private final Map map;
    private int current_team;
    private final Player[] player_list;
    private GameListener game_listener;
    private final Queue<GameEvent> event_queue;

    private int turn;

    private final Unit[] commanders;
    private final int max_population;
    private final int[] commander_price_delta;

    public BasicGame(Map map, Player[] players, int max_population) {
        this.map = map;
        player_list = new Player[4];
        for (int team = 0; team < 4; team++) {
            if (team < players.length) {
                player_list[team] = players[team];
            } else {
                break;
            }
        }
        this.turn = 0;
        this.max_population = max_population;
        this.commander_price_delta = new int[4];
        this.commanders = new Unit[4];
        Set<Point> position_set = new HashSet(getMap().getUnitPositionSet());
        for (Point position : position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.isCommander()) {
                commanders[unit.getTeam()] = unit;
            }
        }
        for (int team = 0; team < 4; team++) {
            if (commanders[team] == null) {
                commanders[team] = UnitFactory.createUnit(9, team);
            }
        }
        this.event_queue = new LinkedList();
    }

    public void init() {
        current_team = -1;
        for (int team = 0; team < player_list.length; team++) {
            if (player_list[team] != null) {
                if (current_team == -1) {
                    current_team = team;
                }
                updatePopulation(team);
            } else {
                //remove all elements on the map that is related to this team
            }
        }
    }

    protected GameListener getGameListener() {
        return game_listener;
    }

    protected void submitGameEvent(GameEvent e) {
        event_queue.add(e);
    }

    public boolean isLocalPlayer() {
        return getCurrentPlayer() instanceof LocalPlayer;
    }

    public Player getCurrentPlayer() {
        return player_list[current_team];
    }

    public ArrayList<Integer> getBuyableUnits(int team) {
        int sketeton = UnitFactory.getSkeletonIndex();
        int crystal = UnitFactory.getCrystalIndex();
        ArrayList<Integer> unit_index_list = new ArrayList();
        for (int i = 0; i < UnitFactory.getUnitCount(); i++) {
            if (i != sketeton && i != crystal) {
                unit_index_list.add(i);
            }
        }
        return unit_index_list;
    }

    public int getCurrentTeam() {
        return current_team;
    }

    public Player getPlayer(int team) {
        return player_list[team];
    }

    public int getMaxPopulation() {
        return max_population;
    }

    public void setGameListener(GameListener listener) {
        this.game_listener = listener;
    }

    public void doAttack(int unit_x, int unit_y, int target_x, int target_y) {
        Unit attacker = getMap().getUnit(unit_x, unit_y);
        if (attacker != null && UnitToolkit.isWithinRange(attacker, target_x, target_y)) {
            Unit defender = getMap().getUnit(target_x, target_y);
            if (defender != null) {
                submitGameEvent(new UnitAttackEvent(this, attacker, defender));
                submitGameEvent(new UnitActionFinishEvent(attacker));
            } else {
                if (attacker.hasAbility(Ability.DESTROYER)
                        && getMap().getTile(target_x, target_y).isDestroyable()) {
                    submitGameEvent(new TileDestroyEvent(this, target_x, target_y));
                    submitGameEvent(new UnitActionFinishEvent(attacker));
                }
            }
        }
    }

    public void destroyUnit(int unit_x, int unit_y) {
        Unit unit = getMap().getUnit(unit_x, unit_y);
        if (unit != null) {
            submitGameEvent(new UnitDestroyEvent(this, unit));
        }
    }

    public void doSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        Unit summoner = getMap().getUnit(summoner_x, summoner_y);
        if (summoner != null
                && UnitToolkit.isWithinRange(summoner, target_x, target_y)
                && getMap().isTomb(target_x, target_y)) {
            submitGameEvent(new UnitSummonEvent(this, summoner, target_x, target_y));
            submitGameEvent(new UnitActionFinishEvent(summoner));
        }
    }

    public void doHeal(int healer_x, int healer_y, int target_x, int target_y) {
        Unit healer = getMap().getUnit(healer_x, healer_y);
        Unit target = getMap().getUnit(target_x, target_y);
        if (healer != null
                && UnitToolkit.isWithinRange(healer, target_x, target_y)
                && !isEnemy(healer.getTeam(), target.getTeam())) {
            submitGameEvent(new UnitHealEvent(this, healer, target));
            submitGameEvent(new UnitActionFinishEvent(healer));
        }
    }

    public void doOccupy(int conqueror_x, int conqueror_y, int x, int y) {
        Unit conqueror = getMap().getUnit(conqueror_x, conqueror_y);
        if (canOccupy(conqueror, x, y)) {
            submitGameEvent(new OccupyEvent(this, conqueror, x, y));
            submitGameEvent(new UnitActionFinishEvent(conqueror));
        }
    }

    public void doRepair(int repairer_x, int repairer_y, int x, int y) {
        Unit repairer = getMap().getUnit(x, y);
        if (canRepair(repairer, x, y)) {
            submitGameEvent(new RepairEvent(this, repairer, x, y));
            submitGameEvent(new UnitActionFinishEvent(repairer));
        }
    }

    public void changeTile(short index, int x, int y) {
        getMap().setTile(index, x, y);
    }

    public void standbyUnit(int unit_x, int unit_y) {
        Unit unit = getMap().getUnit(unit_x, unit_y);
        if (unit != null && getMap().canStandby(unit)) {
            standbyUnit(unit);
        }
    }

    protected void standbyUnit(Unit unit) {
        unit.setStandby(true);
    }

    public void restoreCommander(int team, int x, int y) {
        if (!isCommanderAlive(team)) {
            commanders[team].setX(x);
            commanders[team].setY(y);
            getMap().addUnit(commanders[team]);
            commanders[team].setCurrentHp(commanders[team].getMaxHp());
            restoreUnit(commanders[team]);
            updatePopulation(team);
        }
    }

    public void buyUnit(int index, int x, int y) {
        int current_cold = getCurrentPlayer().getGold();
        int unit_price = UnitFactory.getSample(index).getPrice();
        if (current_cold >= unit_price) {
            getCurrentPlayer().setGold(current_cold - unit_price);
            addUnit(index, getCurrentTeam(), x, y);
        }
    }

    public void addUnit(int index, int team, int x, int y) {
        Unit unit = UnitFactory.createUnit(index, team);
        unit.setX(x);
        unit.setY(y);
        getMap().addUnit(unit);
        updatePopulation(getCurrentTeam());
    }

    public void moveUnit(int unit_x, int unit_y, int dest_x, int dest_y) {
        Unit unit = getMap().getUnit(unit_x, unit_y);
        if (unit != null && getMap().canMove(dest_x, dest_y)) {
            submitGameEvent(new UnitMoveEvent(this, unit, dest_x, dest_y));
            submitGameEvent(new UnitMoveFinishEvent(unit, unit_x, unit_y));
        }
    }

    protected int getTerrainHeal(Unit unit) {
        int heal = 0;
        Tile tile = getMap().getTile(unit.getX(), unit.getY());
        if (tile.getTeam() == -1) {
            heal += tile.getHpRecovery();
        } else {
            if (tile.getTeam() == getCurrentTeam()) {
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

    protected void healAllys(Unit healer) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    int x = healer.getX() + dx;
                    int y = healer.getY() + dy;
                    if (getMap().isWithinMap(x, y)) {
                        Unit unit = getMap().getUnit(x, y);
                        if (unit != null && unit.getTeam() == healer.getTeam()) {
                            submitGameEvent(new UnitHpChangeEvent(this, unit, 10));
                        }
                    }
                }
            }
        }
    }

    public boolean canOccupy(Unit conqueror, int x, int y) {
        if (conqueror == null) {
            return false;
        }
        if (conqueror.getTeam() != getCurrentTeam()) {
            return false;
        }
        Tile tile = getMap().getTile(x, y);
        if (tile.getTeam() != getCurrentTeam()) {
            return (tile.isCastle() && conqueror.hasAbility(Ability.COMMANDER))
                    || (tile.isVillage() && conqueror.hasAbility(Ability.CONQUEROR));
        } else {
            return false;
        }
    }

    public boolean canRepair(Unit repairer, int x, int y) {
        if (repairer == null) {
            return false;
        }
        if (repairer.getTeam() != getCurrentTeam()) {
            return false;
        }
        Tile tile = getMap().getTile(x, y);
        return repairer.hasAbility(Ability.REPAIRER) && tile.isRepairable();
    }

    public int getAlliance(int team) {
        return getPlayer(team).getAlliance();
    }

    public int getCommanderPrice(int team) {
        if (commander_price_delta[team] > 0) {
            return UnitFactory.getSample(9).getPrice() + commander_price_delta[team];
        } else {
            return -1;
        }
    }

    public Unit getCommander(int team) {
        return commanders[team];
    }

    public void changeCommanderPriceDelta(int team, int change) {
        commander_price_delta[team] += change;
        if (commander_price_delta[team] < 0) {
            commander_price_delta[team] = 0;
        }
    }

    public boolean isCommanderAlive(int team) {
        Set<Point> position_set = new HashSet(getMap().getUnitPositionSet());
        for (Point position : position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team && unit.isCommander()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnemy(int team_a, int team_b) {
        if (team_a >= 0 && team_b >= 0) {
            return getAlliance(team_a) != getAlliance(team_b);
        } else {
            return false;
        }
    }

    public void updatePopulation(int team) {
        getPlayer(team).setPopulation(getMap().getUnitCount(team));
    }

    protected int getIncome(int team) {
        int income = 0;
        for (int x = 0; x < getMap().getWidth(); x++) {
            for (int y = 0; y < getMap().getHeight(); y++) {
                Tile tile = getMap().getTile(x, y);
                if (tile.getTeam() == team) {
                    if (tile.isVillage()) {
                        income += 100;
                    }
                    if (tile.isCastle()) {
                        income += 50;
                    }
                }
            }
        }
        return income;
    }

    public Point getTurnStartPosition(int team) {
        Set<Point> position_set = new HashSet(getMap().getUnitPositionSet());
        Unit first_unit = null;
        for (Point position : position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (first_unit == null) {
                first_unit = unit;
            }
            if (unit.getTeam() == team && unit.isCommander()) {
                return getMap().getPosition(unit.getX(), unit.getY());
            }
        }
        if (first_unit != null) {
            return getMap().getPosition(first_unit.getX(), first_unit.getY());
        }
        Point first_tile_position = null;
        for (int x = 0; x < getMap().getWidth(); x++) {
            for (int y = 0; y < getMap().getHeight(); y++) {
                if (getMap().getTile(x, y).getTeam() == team) {
                    if (first_tile_position == null) {
                        first_tile_position = getMap().getPosition(x, y);
                    }
                    if (getMap().getTile(x, y).isCastle()) {
                        return getMap().getPosition(x, y);
                    }
                }
            }
        }
        if (first_tile_position != null) {
            return first_tile_position;
        }
        return getMap().getPosition(0, 0);
    }

    public int getTurn() {
        return turn;
    }

    public final Map getMap() {
        return map;
    }

    public void startTurn() {
        turn++;
        //gain gold
        int income = getIncome(getCurrentTeam());
        getCurrentPlayer().addGold(income);

        game_listener.onTurnStart(turn, income, getCurrentTeam());

        //posit viewport
        Point team_start_position = getTurnStartPosition(getCurrentTeam());
        game_listener.onMapFocused(team_start_position.x, team_start_position.y);

        updateUnits(getCurrentTeam());
    }

    private void updateUnits(int team) {
        Set<Point> unit_position_set = new HashSet(getMap().getUnitPositionSet());
        HashMap<Point, Integer> hp_change_map = new HashMap();

        Set<Point> temp_unit_position_set = new HashSet(unit_position_set);
        for (Point position : temp_unit_position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                int change = 0;
                //deal with terrain heal issues
                change += getTerrainHeal(unit);
                //deal with buff issues
                if (unit.hasBuff(Buff.POISONED)) {
                    change -= poison_damage;
                }
                hp_change_map.put(position, change);
            } else {
                //remove other teams' unit position
                unit_position_set.remove(position);
            }
        }
        //healing aura
        for (Point position : unit_position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.hasAbility(Ability.HEALING_AURA)) {
                for (int x = unit.getX() - 1; x <= unit.getX() + 1; x++) {
                    for (int y = unit.getY() - 1; y <= unit.getY() + 1; y++) {
                        //not healer himself
                        if ((x != unit.getX() || y != unit.getY()) && getMap().isWithinMap(x, y)) {
                            Point target_position = getMap().getPosition(x, y);
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
        submitGameEvent(new MapHpChangeEvent(this, hp_change_map));
        //check for dead units after hp change
        for (Point position : hp_change_map.keySet()) {
            Unit unit = getMap().getUnit(position.x, position.y);
            int change = hp_change_map.get(position);
            int unit_hp = unit.getCurrentHp();
            if (unit_hp + change <= 0) {
                submitGameEvent(new UnitDestroyEvent(this, unit));
            }
        }
        submitGameEvent(new BuffUpdateEvent(this, unit_position_set));
    }

    private void restoreUnit(Unit unit) {
        unit.setCurrentMovementPoint(unit.getMovementPoint());
        unit.setStandby(false);
    }

    public void endTurn() {
        Collection<Unit> units = getMap().getUnitSet();
        for (Unit unit : units) {
            if (unit.getTeam() == getCurrentTeam()) {
                restoreUnit(unit);
            }
        }
        do {
            if (current_team < 3) {
                current_team++;
            } else {
                current_team = 0;
                getMap().updateTombs();
            }
        } while (getCurrentPlayer() == null);
        startTurn();
    }

    public boolean isDispatchingEvents() {
        return !event_queue.isEmpty();
    }

    protected void clearGameEvents() {
        event_queue.clear();
    }

    public void dispatchGameEvent() {
        GameEvent event = event_queue.poll();
        if (event != null) {
            //skip unexcutable events
            while (!event.canExecute()) {
                event = event_queue.poll();
                if (event == null) {
                    game_listener.onGameEventCleared();
                    return;
                }
            }
            event.execute(game_listener);
            if (event_queue.isEmpty()) {
                game_listener.onGameEventCleared();
            }
        }
    }

}
