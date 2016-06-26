package net.toyknight.aeii.entity;

import static net.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.Serializable;
import net.toyknight.aeii.utils.UnitFactory;
import net.toyknight.aeii.utils.UnitToolkit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class GameCore implements Serializable {

    public static final int SKIRMISH = 0x1;
    public static final int CAMPAIGN = 0x2;

    protected final int type;

    protected final Map map;
    protected final Rule rule;
    protected final Player[] players;
    protected final Unit[] commanders;
    protected final boolean[] team_destroy;

    protected int turn;

    protected int current_team;

    protected boolean game_over;

    protected Statistics statistics;

    protected boolean initialized;

    public GameCore(JSONObject json) throws JSONException {
        this(new Map(json.getJSONObject("map")), new Rule(json.getJSONObject("rule")), 0, json.getInt("type"));
        setCurrentTurn(json.getInt("current_turn"));
        setCurrentTeam(json.getInt("current_team"));
        setGameOver(json.getBoolean("game_over"));
        setInitialized(json.getBoolean("initialized"));
        JSONArray players = json.getJSONArray("players");
        JSONArray commanders = json.getJSONArray("commanders");
        JSONArray team_destroy = json.getJSONArray("team_destroy");
        JSONArray income = json.getJSONObject("statistics").getJSONArray("income");
        JSONArray destroy = json.getJSONObject("statistics").getJSONArray("destroy");
        JSONArray lose = json.getJSONObject("statistics").getJSONArray("lose");
        for (int team = 0; team < 4; team++) {
            JSONObject player = players.getJSONObject(team);
            getPlayer(team).setType(player.getInt("type"));
            getPlayer(team).setGold(player.getInt("gold"));
            getPlayer(team).setAlliance(player.getInt("alliance"));
            getPlayer(team).setPopulation(player.getInt("population"));
            setTeamDestroyed(team, team_destroy.getBoolean(team));
            getStatistics().addIncome(team, income.getInt(team));
            getStatistics().addDestroy(team, destroy.getInt(team));
            getStatistics().addLose(team, lose.getInt(team));
            setCommander(team, UnitFactory.createUnit(commanders.getJSONObject(team)));
        }
    }

    public GameCore(GameCore game) {
        type = game.type;
        map = new Map(game.map);
        rule = new Rule(game.rule);
        commanders = new Unit[4];
        players = new Player[4];
        team_destroy = new boolean[4];
        turn = game.turn;
        current_team = game.current_team;
        game_over = game.game_over;
        statistics = new Statistics(game.statistics);
        initialized = game.initialized;

        for (int team = 0; team < 4; team++) {
            if (game.players[team] != null) {
                players[team] = new Player(game.players[team]);
            }
            if (game.commanders[team] != null) {
                setCommander(team, new Unit(game.commanders[team]));
            }
            team_destroy[team] = game.team_destroy[team];
        }
    }

    public GameCore(Map map, Rule rule, int start_gold, int type) {
        this.map = map;
        this.rule = rule;
        this.type = type;
        this.players = new Player[4];
        this.commanders = new Unit[4];
        this.team_destroy = new boolean[4];
        this.turn = 1;
        this.game_over = false;
        this.statistics = new Statistics();
        this.initialized = false;
        for (Unit unit : getMap().getUnits()) {
            if (unit.isCommander()) {
                commanders[unit.getTeam()] = unit;
            }
        }
        for (int team = 0; team < 4; team++) {
            players[team] = Player.createPlayer(Player.NONE, 0, 0, 0);
            players[team].setGold(start_gold);
            team_destroy[team] = false;

            if (commanders[team] == null) {
                commanders[team] = UnitFactory.createCommander(team);
            }
        }
    }

    public void initialize() {
        if (!initialized) {
            current_team = -1;
            for (int team = 0; team < 4; team++) {
                if (players[team].getType() == Player.NONE) {
                    if (getMap().hasTeamAccess(team)) {
                        getMap().removeTeam(team);
                    }
                } else {
                    if (current_team == -1) {
                        current_team = team;
                    }
                    statistics.addIncome(team, getPlayer(team).getGold());
                    updatePopulation(team);
                }
            }
            setInitialized(true);
        }
    }

    public final Statistics getStatistics() {
        return statistics;
    }

    public final boolean initialized() {
        return initialized;
    }

    public final void setInitialized(boolean initialized) {
        this.initialized = initialized;
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

    public boolean isTeamAlive(int team) {
        return 0 <= team && team < 4 && getPlayer(team).getType() != Player.NONE && !team_destroy[team];
    }

    public Player getPlayer(int team) {
        return players[team];
    }

    public Player getCurrentPlayer() {
        return players[current_team];
    }

    public int getCurrentTeam() {
        return current_team;
    }

    public int getNextTeam() {
        int team = getCurrentTeam();
        do {
            team = team < 3 ? team + 1 : 0;
        } while (!isTeamAlive(team));
        return team;
    }

    public void setCurrentTeam(int team) {
        current_team = team;
    }

    public int getCurrentTurn() {
        return turn;
    }

    public void setCurrentTurn(int turn) {
        this.turn = turn;
    }

    public int getMaxPopulation() {
        return getRule().getInteger(MAX_POPULATION);
    }

    public int getPopulation(int team) {
        return getPlayer(team).getPopulation();
    }

    public boolean canAddPopulation(int team, int population) {
        Player player = getPlayer(team);
        return player.getType() != Player.NONE
                && player.getPopulation() + population <= getRule().getInteger(MAX_POPULATION);
    }

    public void destroyTeam(int team) {
        getMap().removeTeam(team);
        setTeamDestroyed(team, true);
    }

    public boolean isTeamDestroyed(int team) {
        return team_destroy[team];
    }

    public void setTeamDestroyed(int team, boolean destroyed) {
        team_destroy[team] = destroyed;
    }

    public boolean isGameOver() {
        return game_over;
    }

    public void setGameOver(boolean game_over) {
        this.game_over = game_over;
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

    public void setCommander(int team, Unit commander) {
        commanders[team] = commander;
        if (isCommanderAlive(team)) {
            getMap().addUnit(commanders[team], true);
        }
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
        for (Unit unit : getMap().getUnits()) {
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
        for (Position position : getMap().getCastlePositions()) {
            if (getMap().getTile(position).getTeam() == team) {
                income += getRule().getInteger(CASTLE_INCOME);
            }
        }
        for (Position position : getMap().getVillagePositions()) {
            if (getMap().getTile(position).getTeam() == team) {
                income += getRule().getInteger(VILLAGE_INCOME);
            }
        }
        return income + getCommanderIncome(team);
    }

    private int getCommanderIncome(int team) {
        if (isCommanderAlive(team)) {
            return getRule().getInteger(COMMANDER_INCOME) * (getCommander(team).getLevel() + 1);
        } else {
            return 0;
        }
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

    public boolean isAlly(Unit unit_a, Unit unit_b) {
        return unit_a != null && unit_b != null && isAlly(unit_a.getTeam(), unit_b.getTeam());
    }

    public boolean isAlly(int team_a, int team_b) {
        return team_a >= 0 && team_b >= 0 && getAlliance(team_a) == getAlliance(team_b);
    }

    public int getEnemyAroundCount(Unit unit, int range) {
        return getEnemyAroundCount(unit.getX(), unit.getY(), unit.getTeam(), range);
    }

    public int getEnemyAroundCount(int map_x, int map_y, int team, int range) {
        if (range < 1) {
            return 0;
        }
        int count = 0;
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                if (Math.abs(dx) + Math.abs(dy) <= range) {
                    Unit unit = getMap().getUnit(map_x + dx, map_y + dy);
                    if (unit != null && isEnemy(team, unit.getTeam())) {
                        count++;
                    }
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

    public boolean canCounter(Unit attacker, Unit defender) {
        if (isUnitAlive(defender) && isEnemy(defender, attacker) && UnitToolkit.isWithinRange(defender, attacker)) {
            if (defender.hasAbility(Ability.COUNTER_MADNESS)) {
                return UnitToolkit.getRange(defender, attacker) <= 2;
            } else {
                return UnitToolkit.getRange(defender, attacker) == 1
                        && !(attacker.hasAbility(Ability.AMBUSH) && !defender.hasAbility(Ability.AMBUSH));
            }
        } else {
            return false;
        }
    }

    public boolean canOccupy(Unit conqueror, int x, int y) {
        if (getMap().isWithinMap(x, y)) {
            Tile tile = getMap().getTile(x, y);
            return !(conqueror == null || tile.getTeam() == conqueror.getTeam())
                    && tile.isCapturable()
                    && ((tile.isCastle() && conqueror.hasAbility(Ability.COMMANDER))
                    || (tile.isVillage() && conqueror.hasAbility(Ability.CONQUEROR)));
        } else {
            return false;
        }
    }

    public boolean canRepair(Unit repairer, int x, int y) {
        if (getMap().isWithinMap(x, y)) {
            if (repairer == null) {
                return false;
            } else {
                Tile tile = getMap().getTile(x, y);
                return repairer.hasAbility(Ability.REPAIRER) && tile.isRepairable();
            }
        } else {
            return false;
        }
    }

    public boolean canSummon(Unit summoner, int x, int y) {
        return summoner.hasAbility(Ability.NECROMANCER)
                && UnitToolkit.isWithinRange(summoner, x, y)
                && getMap().isTomb(x, y) && getMap().getUnit(x, y) == null;
    }

    public boolean canHeal(Unit healer, int x, int y) {
        if (getMap().isWithinMap(x, y)) {
            Unit target = getMap().getUnit(x, y);
            return canHeal(healer, target);
        } else {
            return false;
        }
    }

    public boolean canHeal(Unit healer, Unit target) {
        if (healer == null || target == null) {
            return false;
        } else {
            if (healer.hasAbility(Ability.HEALER) && canHealReachTarget(healer, target)) {
                if (canReceiveHeal(target)) {
                    return !isEnemy(healer, target)
                            && (UnitToolkit.isWithinRange(healer, target) || UnitToolkit.isTheSameUnit(healer, target));
                } else {
                    //heal becomes damage for the undead
                    return target.hasAbility(Ability.UNDEAD);
                }
            } else {
                return false;
            }
        }
    }

    public boolean canClean(Unit cleaner, Unit target) {
        return target != null && !isEnemy(cleaner, target) && Status.isDebuff(target.getStatus());
    }

    public boolean canHealReachTarget(Unit healer, Unit target) {
        return healer.hasAbility(Ability.AIR_FORCE) || !target.hasAbility(Ability.AIR_FORCE);
    }

    public boolean canReceiveHeal(Unit target) {
        return !target.hasAbility(Ability.UNDEAD)
                && !target.hasStatus(Status.POISONED)
                && target.getCurrentHp() <= target.getMaxHp();
    }

    public boolean canRefresh(Unit refresher, Unit target) {
        return !(refresher == null || target == null)
                && canHealReachTarget(refresher, target)
                && target.getCurrentHp() <= target.getCurrentHp()
                && (!isEnemy(refresher, target) || target.hasAbility(Ability.UNDEAD));
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
        return target_unit == null
                || !isEnemy(unit, target_unit)
                || unit.hasAbility(Ability.AIR_FORCE) && !target_unit.hasAbility(Ability.AIR_FORCE);
    }

    public boolean canBuyUponUnit(Unit unit, int team) {
        return unit == null || (unit.isCommander() && unit.getTeam() == team);
    }

    public boolean isUnitAccessible(Unit unit) {
        return isUnitAlive(unit) && unit.getTeam() == getCurrentTeam() && !unit.isStandby();
    }

    public boolean isUnitAlive(Unit unit) {
        return unit != null && unit.getCurrentHp() > 0;
    }

    public boolean isCastleAccessible(Tile tile) {
        return isCastleAccessible(tile, getCurrentTeam());
    }

    public boolean isCastleAccessible(Tile tile, int team) {
        return tile.isCastle() && tile.getTeam() == team;
    }

    public Position getTeamFocus(int team) {
        Position commander_position = null;
        Position first_unit_position = null;
        ObjectMap.Keys<Position> position_set = getMap().getUnitPositions();
        for (Position position : position_set) {
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
                    return new Position(x, y);
                }
            }
        }
        return new Position(-1, -1);
    }

    public void nextTurn() {
        ObjectMap.Values<Unit> units = getMap().getUnits();
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
        } while (!isTeamAlive(current_team));
        turn++;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", getType());
        json.put("map", getMap().toJson());
        json.put("rule", getRule().toJson());
        JSONArray players = new JSONArray();
        JSONArray commanders = new JSONArray();
        JSONArray team_destroy = new JSONArray();
        for (int team = 0; team < 4; team++) {
            players.put(getPlayer(team).toJson());
            team_destroy.put(isTeamDestroyed(team));
            commanders.put(getCommander(team).toJson());
        }
        json.put("players", players);
        json.put("commanders", commanders);
        json.put("team_destroy", team_destroy);
        json.put("current_turn", getCurrentTurn());
        json.put("current_team", getCurrentTeam());
        json.put("game_over", isGameOver());
        json.put("statistics", getStatistics().toJson());
        json.put("initialized", initialized());
        return json;
    }

}
