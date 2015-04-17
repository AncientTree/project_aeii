package com.toyknight.aeii.entity;

import com.toyknight.aeii.entity.player.Player;
import com.toyknight.aeii.listener.GameListener;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.utils.UnitFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by toyknight on 4/3/2015.
 */
public class GameCore {

    private final Map map;
    private final Rule rule;
    private int current_team;
    private final Player[] player_list;
    private GameListener game_listener;

    private int turn;

    private final Unit[] commanders;
    private final int[] commander_price_delta;

    public GameCore(Map map, Rule rule, Player[] players) {
        this.map = map;
        this.rule = rule;
        player_list = new Player[4];
        for (int team = 0; team < 4; team++) {
            if (team < players.length) {
                player_list[team] = players[team];
            } else {
                break;
            }
        }
        this.turn = 0;
        this.commander_price_delta = new int[4];
        this.commanders = new Unit[4];
    }

    public void init() {
        Set<Point> position_set = new HashSet(getMap().getUnitPositionSet());
        for (Point position : position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.isCommander()) {
                commanders[unit.getTeam()] = unit;
            }
        }
        current_team = -1;
        for (int team = 0; team < player_list.length; team++) {
            if (player_list[team] != null) {
                if (current_team == -1) {
                    current_team = team;
                }
                if (commanders[team] == null) {
                    commanders[team] = UnitFactory.createUnit(UnitFactory.getCommanderIndex(), team);
                }
                updatePopulation(team);
            } else {
                //remove all elements on the map that is related to this team
            }
        }
    }

    public void setGameListener(GameListener listener) {
        this.game_listener = listener;
    }

    private GameListener getGameListener() {
        return game_listener;
    }

    public final Map getMap() {
        return map;
    }

    public final Rule getRule() {
        return rule;
    }

    public Player getPlayer(int team) {
        return player_list[team];
    }

    public Player getCurrentPlayer() {
        return player_list[current_team];
    }

    public int getCurrentTeam() {
        return current_team;
    }

    public int getCurrentTurn() {
        return turn;
    }

    public int getCommanderPrice(int team) {
        if (commander_price_delta[team] > 0) {
            int commander_index = UnitFactory.getCommanderIndex();
            return UnitFactory.getSample(commander_index).getPrice() + commander_price_delta[team];
        } else {
            return -1;
        }
    }

    public void increaseUnitExperience(int unit_x, int unit_y, int experience) {
        Unit target = getMap().getUnit(unit_x, unit_y);
        if (target != null) {
            boolean level_up = target.gainExperience(experience);
            if (level_up == true) {
                game_listener.onUnitLevelUp(target);
            }
        }
    }

    public void destroyUnit(int target_x, int target_y) {
        Unit target = getMap().getUnit(target_x, target_y);
        if (target != null) {
            getMap().removeUnit(target_x, target_y);
            updatePopulation(target.getTeam());
            if (target.getIndex() != UnitFactory.getSkeletonIndex()) {
                getMap().addTomb(target.getX(), target.getY());
            }
            if (target.isCommander()) {
                changeCommanderPriceDelta(target.getTeam(), getRule().getCommanderPriceGrowth());
            }
            game_listener.onUnitDestroyed(target);
        }
    }

    public void summonSkeleton(int target_x, int target_y, int team) {
        if (getMap().isTomb(target_x, target_y)) {
            getMap().removeTomb(target_x, target_y);
            createUnit(UnitFactory.getSkeletonIndex(), team, target_x, target_y);
            standbyUnit(target_x, target_y);
        }
    }

    public void changeUnitHp(int target_x, int target_y, int change) {
        Unit target = getMap().getUnit(target_x, target_y);
        if (target != null) {
            int target_hp = target.getCurrentHp();
            target_hp += change;
            if (target_hp > target.getMaxHp()) {
                target_hp = target.getMaxHp();
            }
            if (target_hp < 0) {
                target_hp = 0;
            }
            target.setCurrentHp(target_hp);
            if (target.getCurrentHp() == 0) {
                destroyUnit(target_x, target_y);
            }
        }
    }

    public void standbyUnit(int unit_x, int unit_y) {
        Unit unit = getMap().getUnit(unit_x, unit_y);
        if (unit != null && getMap().canStandby(unit)) {
            unit.setStandby(true);
        }
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

    public void buyUnit(int index, int team, int x, int y) {
        int current_cold = getCurrentPlayer().getGold();
        int unit_price = UnitFactory.getUnitPrice(index);
        if (current_cold >= unit_price) {
            getCurrentPlayer().setGold(current_cold - unit_price);
            createUnit(index, getCurrentTeam(), x, y);
        }
    }

    public void createUnit(int index, int team, int x, int y) {
        Unit unit = UnitFactory.createUnit(index, team);
        unit.setX(x);
        unit.setY(y);
        getMap().addUnit(unit);
        updatePopulation(team);
    }

    public void moveUnit(int target_x, int target_y, int dest_x, int dest_y) {
        Unit target = getMap().getUnit(target_x, target_y);
        if (target != null && getMap().canMove(dest_x, dest_y)) {
            getMap().moveUnit(target, dest_x, dest_y);
        }
    }

    public void setTile(short index, int x, int y) {
        getMap().setTile(index, x, y);
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

   /* protected void healAllys(Unit healer) {
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
    }*/

    /*public boolean canOccupy(Unit conqueror, int x, int y) {
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
    }*/

    /*public boolean canRepair(Unit repairer, int x, int y) {
        if (repairer == null) {
            return false;
        }
        if (repairer.getTeam() != getCurrentTeam()) {
            return false;
        }
        Tile tile = getMap().getTile(x, y);
        return repairer.hasAbility(Ability.REPAIRER) && tile.isRepairable();
    }*/

    public int getAlliance(int team) {
        return getPlayer(team).getAlliance();
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

    public int getIncome(int team) {
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

    /*public Point getTurnStartPosition(int team) {
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
                    change -= POISON_DAMAGE;
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
    }*/

    public void restoreUnit(Unit unit) {
        unit.setCurrentMovementPoint(unit.getMovementPoint());
        unit.setStandby(false);
    }

    public void nextTurn() {
        do {
            if (current_team < 3) {
                current_team++;
            } else {
                current_team = 0;
                getMap().updateTombs();
            }
        } while (getCurrentPlayer() == null);
        turn++;
    }

}
