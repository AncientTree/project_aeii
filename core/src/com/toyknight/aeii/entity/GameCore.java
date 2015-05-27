package com.toyknight.aeii.entity;

import com.toyknight.aeii.entity.player.Player;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.Collection;
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
        this.turn = 1;
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
                    commanders[team] = UnitFactory.createUnit(UnitFactory.getCommanderIndex(), team, "default");
                }
                updatePopulation(team);
            } else {
                //remove all elements on the map that is related to this team
            }
        }
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
            return UnitFactory.getSample(commander_index, "default").getPrice() + commander_price_delta[team];
        } else {
            return -1;
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
        }
    }

    public void summonSkeleton(int target_x, int target_y, int team) {
        if (getMap().isTomb(target_x, target_y)) {
            getMap().removeTomb(target_x, target_y);
            createUnit(UnitFactory.getSkeletonIndex(), team, "default", target_x, target_y);
            standbyUnit(target_x, target_y);
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

    public void buyUnit(int index, int team, String package_name, int x, int y) {
        int current_cold = getCurrentPlayer().getGold();
        int unit_price = UnitFactory.getUnitPrice(index, package_name);
        if (current_cold >= unit_price) {
            getCurrentPlayer().setGold(current_cold - unit_price);
            createUnit(index, getCurrentTeam(), package_name, x, y);
        }
    }

    public void createUnit(int index, int team, String package_name, int x, int y) {
        Unit unit = UnitFactory.createUnit(index, team, package_name);
        unit.setX(x);
        unit.setY(y);
        getMap().addUnit(unit);
        updatePopulation(team);
    }

    public void moveUnit(int target_x, int target_y, int dest_x, int dest_y) {
        Unit target = getMap().getUnit(target_x, target_y);
        if (target != null && canUnitMove(target, dest_x, dest_y)) {
            getMap().moveUnit(target, dest_x, dest_y);
        }
    }

    public void setTile(short index, int x, int y) {
        getMap().setTile(index, x, y);
    }

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

    public void updatePopulation(int team) {
        getPlayer(team).setPopulation(getMap().getUnitCount(team));
    }

    private int calcIncome(int team) {
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

    public int gainIncome(int team) {
        int income = calcIncome(team);
        getPlayer(team).addGold(income);
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
    }*/

    public void restoreUnit(Unit unit) {
        unit.setCurrentMovementPoint(unit.getMovementPoint());
        unit.setStandby(false);
    }

    public boolean isEnemy(Unit unit_a, Unit unit_b) {
        if (unit_a != null && unit_b != null) {
            return isEnemy(unit_a.getTeam(), unit_b.getTeam());
        } else {
            return false;
        }
    }

    public boolean isEnemy(int team_a, int team_b) {
        if (team_a >= 0 && team_b >= 0) {
            return getAlliance(team_a) != getAlliance(team_b);
        } else {
            return false;
        }
    }

    public boolean canAttack(Unit attacker, int x, int y) {
        if (attacker != null && UnitToolkit.isWithinRange(attacker, x, y)) {
            Unit defender = getMap().getUnit(x, y);
            if (defender != null) {
                return isEnemy(attacker, defender);
            } else {
                if (attacker.hasAbility(Ability.DESTROYER)) {
                    return getMap().getTile(x, y).isDestroyable();
                } else {
                    return false;
                }
            }
        } else {
            return false;
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

    public boolean canSummon(int x, int y) {
        if (getMap().isTomb(x, y)) {
            return getMap().getUnit(x, y) == null;
        } else {
            return false;
        }
    }

    public boolean canHeal(Unit healer, int x, int y) {
        Unit target = getMap().getUnit(x, y);
        if (target != null) {
            return target.getCurrentHp() < target.getMaxHp()
                    && target.getIndex() != UnitFactory.getSkeletonIndex() && !isEnemy(healer, target);
        } else {
            return false;
        }
    }

    public boolean canUnitMove(Unit unit, int dest_x, int dest_y) {
        if (getMap().canMove(dest_x, dest_y)) {
            Unit dest_unit = getMap().getUnit(dest_x, dest_y);
            if (dest_unit == null) {
                return true;
            } else {
                return UnitToolkit.isTheSameUnit(unit, dest_unit);
            }
        } else {
            return false;
        }
    }

    public boolean canMoveThrough(Unit unit, Unit target_unit) {
        if (target_unit == null) {
            return true;
        } else {
            if (isEnemy(unit, target_unit)) {
                return unit.hasAbility(Ability.AIR_FORCE) && !target_unit.hasAbility(Ability.AIR_FORCE);
            } else {
                return true;
            }
        }
    }

    public boolean isUnitAccessible(Unit unit) {
        return unit != null
                && unit.getTeam() == getCurrentTeam()
                && unit.getCurrentHp() > 0
                && !unit.isStandby();
    }

    public boolean isGameOver() {
        return false;
    }

    public int getNextTeam() {
        int team = current_team;
        do {
            if (team < 3) {
                team++;
            } else {
                team = 0;
            }
        } while (getPlayer(team) == null);
        return team;
    }

    public void nextTurn() {
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
        turn++;
    }

}
