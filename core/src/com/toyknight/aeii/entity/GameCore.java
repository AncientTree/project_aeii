package com.toyknight.aeii.entity;

import static com.toyknight.aeii.rule.Rule.Entry.*;

import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.io.Serializable;

/**
 * @author toyknight 4/3/2015.
 */
public class GameCore implements Serializable {

    private static final long serialVersionUID = 9172015L;

    public static final int SKIRMISH = 0x1;
    public static final int CAMPAIGN = 0x2;

    protected final int type;

    protected final Map map;
    protected final Rule rule;
    protected final Player[] player_list;
    protected final Unit[] commanders;

    protected int turn;

    protected int current_team;

    protected boolean game_over;

    protected Statistics statistics;

    protected boolean initialized;

    public GameCore() {
        this(new Map(), Rule.createDefault(), 0, SKIRMISH);
    }

    public GameCore(GameCore game) {
        type = game.type;
        map = new Map(game.map);
        rule = new Rule(game.rule);
        player_list = new Player[4];
        commanders = new Unit[4];
        for (int team = 0; team < 4; team++) {
            if (game.player_list[team] != null) {
                player_list[team] = new Player(game.player_list[team]);
            }
            if (game.commanders[team] != null) {
                commanders[team] = new Unit(game.commanders[team]);
            }
        }
        turn = game.turn;
        current_team = game.current_team;
        game_over = game.game_over;
        statistics = new Statistics(game.statistics);
        initialized = game.initialized;
    }

    public GameCore(Map map, Rule rule, int start_gold, int type) {
        this.map = map;
        this.rule = rule;
        this.type = type;
        player_list = new Player[4];
        for (int team = 0; team < 4; team++) {
            player_list[team] = Player.createPlayer(Player.NONE, 0, 0, 0);
            player_list[team].setGold(start_gold);
        }
        this.turn = 1;
        this.game_over = false;
        this.commanders = new Unit[4];
        this.statistics = new Statistics();
        this.initialized = false;
    }

    public void initialize() {
        if (!initialized) {
            ObjectMap.Keys<Point> position_set = getMap().getUnitPositionSet();
            for (Point position : position_set) {
                Unit unit = getMap().getUnit(position.x, position.y);
                if (unit.isCommander()) {
                    commanders[unit.getTeam()] = unit;
                }
            }
            current_team = -1;
            for (int team = 0; team < 4; team++) {
                if (player_list[team].getType() == Player.NONE) {
                    if (getMap().hasTeamAccess(team)) {
                        getMap().removeTeam(team);
                    }
                } else {
                    if (current_team == -1) {
                        current_team = team;
                    }
                    if (commanders[team] == null) {
                        commanders[team] = UnitFactory.createUnit(UnitFactory.getCommanderIndex(), team);
                    }
                    statistics.addIncome(team, getPlayer(team).getGold());
                    updatePopulation(team);
                }
            }
            initialized = true;
        }
    }

    public final Statistics getStatistics() {
        return statistics;
    }

    public final boolean isInitialized() {
        return initialized;
    }

    public final Map getMap() {
        return map;
    }

    public final Rule getRule() {
        return rule;
    }

    public final int getType() {
        return type;
    }

    public boolean isPlayerAvailable(int team) {
        return getPlayer(team) != null && getPlayer(team).getType() != Player.NONE;
    }

    public void removePlayer(int team) {
        player_list[team].setType(Player.NONE);
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

    public void removeTeam(int team) {
        getMap().removeTeam(team);
        getPlayer(team).setType(Player.NONE);
    }

    public void destroyUnit(int target_x, int target_y) {
        Unit target = getMap().getUnit(target_x, target_y);
        if (target != null) {
            //update statistics
            getStatistics().addLose(target.getTeam(), target.getPrice());
            //remove unit
            getMap().removeUnit(target_x, target_y);
            //update status
            updatePopulation(target.getTeam());
            if (!target.hasAbility(Ability.UNDEAD) && !target.isCommander()) {
                getMap().addTomb(target.getX(), target.getY());
            }
            if (target.isCommander()) {
                Unit commander = getCommander(target.getTeam());
                int price = commander.getPrice();
                commander.setPrice(price + getRule().getInteger(COMMANDER_PRICE_STEP));
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
            commanders[team].clearStatus();
            getMap().addUnit(commanders[team]);
            commanders[team].setCurrentHp(commanders[team].getMaxHp());
            resetUnit(commanders[team]);
            updatePopulation(team);
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
        if (canUnitMove(target, dest_x, dest_y)) {
            getMap().moveUnit(target, dest_x, dest_y);
        }
    }

    public void setTile(short index, int x, int y) {
        getMap().setTile(index, x, y);
    }

    public int getAlliance(int team) {
        return 0 <= team && team < 4 && getPlayer(team) != null ? getPlayer(team).getAlliance() : -1;
    }

    public Unit getCommander(int team) {
        return commanders[team];
    }

    public int getUnitPrice(int index, int team) {
        Unit unit = UnitFactory.getSample(index);
        if (unit.isCommander()) {
            if (isCommanderAlive(team)) {
                return -1;
            } else {
                return commanders[team].getPrice();
            }
        } else {
            return unit.getPrice();
        }
    }

    public boolean isCommanderAlive(int team) {
        ObjectMap.Keys<Point> position_set = getMap().getUnitPositionSet();
        for (Point position : position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team && unit.isCommander()) {
                return true;
            }
        }
        return false;
    }

    public void updatePopulation(int team) {
        int population = getMap().getPopulation(team);
        getPlayer(team).setPopulation(population);
    }

    private int calcIncome(int team) {
        int income = 0;
        for (int x = 0; x < getMap().getWidth(); x++) {
            for (int y = 0; y < getMap().getHeight(); y++) {
                Tile tile = getMap().getTile(x, y);
                if (tile.getTeam() == team) {
                    if (tile.isVillage()) {
                        income += getRule().getInteger(VILLAGE_INCOME);
                    }
                    if (tile.isCastle()) {
                        income += getRule().getInteger(CASTLE_INCOME);
                    }
                }
            }
        }
        if (isCommanderAlive(team)) {
            income += getRule().getInteger(COMMANDER_INCOME) * (getCommander(team).getLevel() + 1);
        }
        return income;
    }

    public int gainIncome(int team) {
        int income = calcIncome(team);
        getPlayer(team).changeGold(income);
        getStatistics().addIncome(team, income);
        return income;
    }

    public void resetUnit(Unit unit) {
        unit.setCurrentMovementPoint(unit.getMovementPoint());
        unit.setStandby(false);
    }

    public boolean isEnemy(Unit unit_a, Unit unit_b) {
        return unit_a != null && unit_b != null && isEnemy(unit_a.getTeam(), unit_b.getTeam());
    }

    public boolean isEnemy(int team_a, int team_b) {
        return team_a >= 0 && team_b >= 0 && getAlliance(team_a) != getAlliance(team_b);
    }

    public boolean isAlly(int team_a, int team_b) {
        return team_a >= 0 && team_b >= 0 && getAlliance(team_a) == getAlliance(team_b);
    }

    public int getEnemyAroundCount(Unit unit, int range) {
        if (range < 1) {
            return 0;
        }
        int count = 0;
        for (int ar = -range; ar <= range; ar++) {
            for (int dx = -ar; dx <= ar; dx++) {
                int dy = dx >= 0 ? ar - dx : -ar - dx;
                if (isEnemy(unit, getMap().getUnit(unit.getX() + dx, unit.getY() + dy))) {
                    count++;
                }
                if (dy != 0 && isEnemy(unit, getMap().getUnit(unit.getX() + dx, unit.getY() - dy))) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean canAttack(Unit attacker, int x, int y) {
        if (attacker != null && UnitToolkit.isWithinRange(attacker, x, y)) {
            Unit defender = getMap().getUnit(x, y);
            if (defender == null) {
                return attacker.hasAbility(Ability.DESTROYER) && getMap().getTile(x, y).isDestroyable();
            } else {
                return isEnemy(attacker, defender);
            }
        } else {
            return false;
        }
    }

    public boolean canAttack(Unit attacker, Unit defender) {
        return canAttack(attacker, defender.getX(), defender.getY());
    }

    public boolean canOccupy(Unit conqueror, int x, int y) {
        if (conqueror == null) {
            return false;
        }
        if (conqueror.getTeam() != getCurrentTeam()) {
            return false;
        }
        Tile tile = getMap().getTile(x, y);
        return tile.getTeam() != getCurrentTeam()
                && ((tile.isCastle() && conqueror.hasAbility(Ability.COMMANDER)) || (tile.isVillage() && conqueror.hasAbility(Ability.CONQUEROR)));
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

    public boolean canSummon(Unit summoner, int x, int y) {
        return UnitToolkit.isWithinRange(summoner, x, y) && getMap().isTomb(x, y) && getMap().getUnit(x, y) == null;
    }

    public boolean canHeal(Unit healer, int x, int y) {
        Unit target = getMap().getUnit(x, y);
        return canHeal(healer, target);
    }

    public boolean canHeal(Unit healer, Unit target) {
        if (healer == null || target == null) {
            return false;
        } else {
            if (canReceiveHeal(target)) {
                return !isEnemy(healer, target)
                        && (UnitToolkit.isWithinRange(healer, target) || UnitToolkit.isTheSameUnit(healer, target))
                        && (healer.hasAbility(Ability.REFRESH_AURA) || healer.hasAbility(Ability.HEALER));
            } else {
                //heal becomes damage for the undead
                return healer.hasAbility(Ability.HEALER) && target.hasAbility(Ability.UNDEAD);
            }
        }
    }

    public boolean canClean(Unit cleaner, Unit target) {
        return target != null && !isEnemy(cleaner, target) && Status.isDebuff(target.getStatus());
    }

    public boolean canReceiveHeal(Unit target) {
        return !target.hasAbility(Ability.HEAVY_MACHINE)
                && !target.hasAbility(Ability.UNDEAD)
                && !target.hasStatus(Status.POISONED)
                && target.getCurrentHp() <= target.getMaxHp();
    }

    public boolean canUnitMove(Unit unit, int dest_x, int dest_y) {
        if (getMap().canMove(dest_x, dest_y)) {
            Unit dest_unit = getMap().getUnit(dest_x, dest_y);
            return dest_unit == null || UnitToolkit.isTheSameUnit(unit, dest_unit);
        } else {
            return false;
        }
    }

    public boolean canMoveThrough(Unit unit, Unit target_unit) {
        return target_unit == null || !isEnemy(unit, target_unit) || unit.hasAbility(Ability.AIR_FORCE) && !target_unit.hasAbility(Ability.AIR_FORCE);
    }

    public boolean isUnitAccessible(Unit unit) {
        return unit != null
                && unit.getTeam() == getCurrentTeam()
                && unit.getCurrentHp() > 0
                && !unit.isStandby();
    }

    public boolean isCastleAccessible(Tile tile) {
        return tile.isCastle() && tile.getTeam() == getCurrentTeam();
    }

    public boolean isGameOver() {
        return game_over;
    }

    public void setGameOver(boolean game_over) {
        this.game_over = game_over;
    }

    public Point getTeamFocus(int team) {
        Point commander_position = null;
        Point first_unit_position = null;
        ObjectMap.Keys<Point> position_set = getMap().getUnitPositionSet();
        for (Point position : position_set) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                first_unit_position = position;
                if (unit.isCommander()) {
                    commander_position = position;
                }
            }
        }
        if (commander_position != null) {
            return commander_position;
        }
        if (first_unit_position != null) {
            return first_unit_position;
        }
        for (int x = 0; x < getMap().getWidth(); x++) {
            for (int y = 0; y < getMap().getHeight(); y++) {
                Tile tile = getMap().getTile(x, y);
                if (tile.getTeam() == team && tile.isCastle()) {
                    return new Point(x, y);
                }
            }
        }
        return new Point(-1, -1);
    }

    public void nextTurn() {
        ObjectMap.Values<Unit> units = getMap().getUnitSet();
        for (Unit unit : units) {
            if (unit.getTeam() == getCurrentTeam()) {
                resetUnit(unit);
            }
        }
        do {
            if (current_team < 3) {
                current_team++;
            } else {
                current_team = 0;
                getMap().updateTombs();
            }
        } while (getCurrentPlayer().getType() == Player.NONE);
        turn++;
    }

}
