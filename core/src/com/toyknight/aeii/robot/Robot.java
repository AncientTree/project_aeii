package com.toyknight.aeii.robot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.Random;

/**
 * @author toyknight 1/12/2016.
 */
public class Robot {

    private final int team;

    private final Random random;

    private final GameManager manager;

    private final ObjectSet<String> target_units;

    private final ObjectMap<String, Position> routines;

    private final Runnable calculate_task = new Runnable() {
        @Override
        public void run() {
            calculating = true;
            doCalculate();
            calculating = false;
        }
    };

    private boolean calculating;

    public Robot(GameManager manager, int team) {
        this.team = team;
        this.manager = manager;
        this.random = new Random();
        this.target_units = new ObjectSet<String>();
        this.routines = new ObjectMap<String, Position>();
    }

    public void initialize() {
        calculating = false;
        getRoutines().clear();
        target_units.clear();
        //TODO: Add target units from rule.
    }

    public GameManager getManager() {
        return manager;
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    private ObjectMap<String, Position> getRoutines() {
        return routines;
    }

    public boolean isCalculating() {
        return calculating;
    }

    public void calculate() {
        if (!isCalculating()) {
            new Thread(calculate_task, "robot-thread").start();
        }
    }

    private void doCalculate() {
        int unit_to_buy = calculateUnitToBuy();
        Position buying_position = getUnitBuyingPosition(unit_to_buy);
        if (unit_to_buy >= 0 && buying_position != null) {
            getManager().getOperationExecutor().submitOperation(
                    Operation.BUY, unit_to_buy, buying_position.x, buying_position.y);
            Unit bought_unit = getBoughtUnit(unit_to_buy, buying_position.x, buying_position.y);
            doUnitAction(bought_unit);
        } else {
            Unit next_unit = getNextUnit(team);
            if (next_unit == null) {
                getManager().getOperationExecutor().submitOperation(Operation.END_TURN);
                getRoutines().clear();
            } else {
                doUnitAction(next_unit);
            }
        }
    }

    private void doUnitAction(Unit unit) {
        Position target_position = calculateTargetPosition(unit);
        if (target_position == null) {
            doFight(unit);
        } else {
            Position next_position = getManager().getMovementGenerator().getNextPositionToTarget(unit, target_position);
            if (getGame().getEnemyAroundCount(next_position.x, next_position.y, unit.getTeam(), 4) > 2) {
                doFight(unit);
            } else {
                getManager().getOperationExecutor().submitOperation(Operation.SELECT, unit.getX(), unit.getY());
                getManager().getOperationExecutor().submitOperation(Operation.MOVE, next_position.x, next_position.y);
                if (canOccupy(unit, next_position.x, next_position.y)) {
                    getManager().getOperationExecutor().submitOperation(Operation.OCCUPY);
                    getManager().getOperationExecutor().submitOperation(Operation.STANDBY);
                } else {
                    if (canRepair(unit, next_position.x, next_position.y)) {
                        getManager().getOperationExecutor().submitOperation(Operation.REPAIR);
                        getManager().getOperationExecutor().submitOperation(Operation.STANDBY);
                    } else {
                        getManager().getOperationExecutor().submitOperation(Operation.STANDBY);
                    }
                }
            }
        }
    }

    private boolean canOccupy(Unit unit, int map_x, int map_y) {
        Tile tile = getGame().getMap().getTile(map_x, map_y);
        Unit target = getGame().getMap().getUnit(map_x, map_y);
        return (unit.isAt(map_x, map_y) || target == null) && !getGame().isAlly(tile.getTeam(), team)
                && ((unit.hasAbility(Ability.CONQUEROR) && tile.isVillage())
                || (unit.hasAbility(Ability.COMMANDER) && tile.isCastle()));
    }


    private boolean canRepair(Unit unit, int map_x, int map_y) {
        Tile tile = getGame().getMap().getTile(map_x, map_y);
        Unit target = getGame().getMap().getUnit(map_x, map_y);
        return (unit.isAt(map_x, map_y) || target == null) && unit.hasAbility(Ability.REPAIRER) && tile.isRepairable();
    }

    private void doFight(Unit unit) {
        ObjectSet<Position> movable_positions = getManager().getMovementGenerator().createMovablePositions(unit);
        for (Position position : movable_positions) {
            if (UnitToolkit.getRange(unit.getX(), unit.getY(), position.x, position.y) >= 0) {
                getManager().getOperationExecutor().submitOperation(Operation.SELECT, unit.getX(), unit.getY());
                getManager().getOperationExecutor().submitOperation(Operation.MOVE, position.x, position.y);
                getManager().getOperationExecutor().submitOperation(Operation.STANDBY);
                break;
            }
        }
    }

    private Position calculateTargetPosition(Unit unit) {
        //find tiles to repair
        if (unit.hasAbility(Ability.REPAIRER)) {
            Tile current_tile = getGame().getMap().getTile(unit.getX(), unit.getY());
            if (current_tile.isRepairable()) {
                return getGame().getMap().getPosition(unit.getX(), unit.getY());
            }
            Position nearest_ruin_position = null;
            int nearest_ruin_distance = Integer.MAX_VALUE;
            for (int x = 0; x < getGame().getMap().getWidth(); x++) {
                for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                    Tile tile = getGame().getMap().getTile(x, y);
                    Position position = getGame().getMap().getPosition(x, y);
                    if (tile.isRepairable() && !hasRoutine(position)) {
                        int distance = getManager().getMovementGenerator().getMovementPointsToTarget(unit, position);
                        if (distance >= 0 && distance < nearest_ruin_distance) {
                            nearest_ruin_distance = distance;
                            nearest_ruin_position = position;
                        }
                    }
                }
            }
            if (nearest_ruin_position != null) {
                getRoutines().put(unit.getUnitCode(), nearest_ruin_position);
                return nearest_ruin_position;
            }
        }
        //find tiles to
        if (unit.hasAbility(Ability.CONQUEROR)) {
            Tile current_tile = getGame().getMap().getTile(unit.getX(), unit.getY());
            if (current_tile.isVillage() && !getGame().isAlly(current_tile.getTeam(), team)) {
                return getGame().getMap().getPosition(unit.getX(), unit.getY());
            }
            Position nearest_village_position = null;
            int nearest_village_distance = Integer.MAX_VALUE;
            for (int x = 0; x < getGame().getMap().getWidth(); x++) {
                for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                    Tile tile = getGame().getMap().getTile(x, y);
                    Position position = getGame().getMap().getPosition(x, y);
                    if (tile.isVillage() && getGame().getMap().getUnit(x, y) == null
                            && !getGame().isAlly(tile.getTeam(), team)) {
                        int distance = getManager().getMovementGenerator().getMovementPointsToTarget(unit, position);
                        if (distance <= unit.getCurrentMovementPoint()) {
                            nearest_village_distance = distance;
                            nearest_village_position = position;
                            break;
                        } else {
                            if (!hasRoutine(position) && distance >= 0 && distance < nearest_village_distance) {
                                nearest_village_distance = distance;
                                nearest_village_position = position;
                            }
                        }
                    }
                }
            }
            if (nearest_village_position != null) {
                getRoutines().put(unit.getUnitCode(), nearest_village_position);
                return nearest_village_position;
            }
        }
        if (unit.hasAbility(Ability.COMMANDER)) {
            for (int x = 0; x < getGame().getMap().getWidth(); x++) {
                for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                    Tile tile = getGame().getMap().getTile(x, y);
                    Position position = getGame().getMap().getPosition(x, y);
                    if (tile.isCastle() && !getGame().isAlly(tile.getTeam(), team)
                            && getGame().getMap().getUnit(x, y) == null && !hasRoutine(position)) {
                        getRoutines().put(unit.getUnitCode(), position);
                        return position;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasRoutine(Position target) {
        for (Position position : getRoutines().values()) {
            if (position.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private int calculateUnitToBuy() {
        int enemy_team_count = 0;
        int enemy_total_population = 0;
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (player.getType() != Player.NONE && getGame().isEnemy(team, this.team)) {
                enemy_team_count++;
                enemy_total_population += player.getPopulation();
            }
        }
        int enemy_average_population = enemy_total_population / enemy_team_count;

        int unit_to_buy = -1;
        int highest_buying_score = 0;
        if (getGame().getPlayer(team).getPopulation() < getGame().getRule().getInteger(Rule.Entry.MAX_POPULATION)
                && getGame().getPlayer(team).getPopulation() - enemy_average_population <= 3) {
            for (int unit_index : getGame().getRule().getAvailableUnits()) {
                int buying_score = getUnitBuyingScore(unit_index, enemy_average_population);
                if (buying_score > highest_buying_score) {
                    highest_buying_score = buying_score;
                    unit_to_buy = unit_index;
                }
            }

        }
        return unit_to_buy;
    }

    private int getUnitBuyingScore(int unit_index, int enemy_average_population) {
        int price = getGame().getUnitPrice(unit_index, team);
        if (price >= 0 && getGame().getPlayer(team).getGold() >= price) {
            int enemy_count = 0;
            int enemy_air_force_count = 0;
            int enemy_debuff_giver_count = 0;
            int enemy_total_physical_attack = 0;
            int enemy_total_physical_defence = 0;
            int enemy_total_magic_attack = 0;
            int enemy_total_magic_defence = 0;
            int enemy_total_reach_range = 0;
            for (Unit unit : getGame().getMap().getUnits()) {
                if (getGame().isEnemy(team, unit.getTeam())) {
                    enemy_count++;
                    if (unit.hasAbility(Ability.POISONER)
                            || unit.hasAbility(Ability.SLOWING_AURA)
                            || unit.hasAbility(Ability.BLINDER)) {
                        enemy_debuff_giver_count++;
                    }
                    if (unit.hasAbility(Ability.AIR_FORCE)) {
                        enemy_air_force_count++;
                    }
                    enemy_total_physical_defence += unit.getPhysicalDefence();
                    enemy_total_magic_defence += unit.getMagicDefence();
                    if (unit.getAttackType() == Unit.ATTACK_PHYSICAL) {
                        enemy_total_physical_attack += unit.getAttack();
                    }
                    if (unit.getAttackType() == Unit.ATTACK_MAGIC) {
                        enemy_total_magic_attack += unit.getAttack();
                    }
                    enemy_total_reach_range += getUnitReachRange(unit);
                }
            }
            int enemy_average_physical_attack = enemy_total_physical_attack / enemy_count;
            int enemy_average_physical_defence = enemy_total_physical_defence / enemy_count;
            int enemy_average_magic_attack = enemy_total_magic_attack / enemy_count;
            int enemy_average_magic_defence = enemy_total_magic_defence / enemy_count;
            int enemy_average_reach_range = enemy_total_reach_range / enemy_count;

            Unit sample_unit = UnitFactory.getSample(unit_index);

            int score = 0;
            if (UnitFactory.getCommanderIndex() == unit_index && !getGame().isCommanderAlive(team)) {
                score += 100 + getGame().getCommander(team).getLevel() * 50;
            }
            if (sample_unit.hasAbility(Ability.CONQUEROR) && getUnitWithAbilityCount(Ability.CONQUEROR) < 3) {
                score += 100;
            }
            if (sample_unit.hasAbility(Ability.REPAIRER) && getUnitWithAbilityCount(Ability.REPAIRER) < 2) {
                score += 100;
            }
            if (enemy_average_physical_defence > enemy_average_magic_defence
                    && sample_unit.getAttackType() == Unit.ATTACK_MAGIC) {
                score += (sample_unit.getAttack() - 50) * 5;
            }
            if (enemy_average_physical_defence <= enemy_average_magic_defence
                    && sample_unit.getAttackType() == Unit.ATTACK_PHYSICAL) {
                score += (sample_unit.getAttack() - 50) * 5;
            }
            if (enemy_average_physical_attack > enemy_average_magic_attack) {
                score += (sample_unit.getPhysicalDefence() - 10) * 2;
            }
            if (enemy_average_physical_attack < enemy_average_magic_attack) {
                score += (sample_unit.getMagicDefence() - 10) * 2;
            }
            int reach_range = getUnitReachRange(sample_unit);
            if (reach_range >= enemy_average_reach_range) {
                score += reach_range * 10;
            } else {
                score += reach_range * 5;
            }
            if (sample_unit.hasAbility(Ability.REFRESH_AURA) && enemy_debuff_giver_count > 0) {
                score += 50 + enemy_debuff_giver_count * 25;
            }
            if (sample_unit.hasAbility(Ability.MARKSMAN) && enemy_air_force_count > 1) {
                score += 50 + enemy_air_force_count * 25;
            }
            if (sample_unit.hasAbility(Ability.HEALER)) {
                int healer_count = getUnitWithAbilityCount(Ability.HEALER);
                if (healer_count < 2) {
                    if (healer_count < 1) {
                        score += 100;
                    } else {
                        score += 50;
                    }
                }
            }
            if (sample_unit.hasAbility(Ability.BLOODTHIRSTY) && enemy_average_population >= 8) {
                score += 100;
            }
            if (sample_unit.hasAbility(Ability.NECROMANCER) && getGame().getMap().getTombs().size > 3) {
                score += 100;
            }
            return score - sample_unit.getPrice() / 10 - getUnitCount(unit_index) * 50;
        } else {
            return -1;
        }

    }

    private int getUnitReachRange(Unit unit) {
        int reach_range = unit.getMaxAttackRange();
        if (!unit.hasAbility(Ability.HEAVY_MACHINE)) {
            reach_range += unit.getMovementPoint();
        }
        return reach_range;
    }

    private Position getUnitBuyingPosition(int unit_index) {
        //TODO: Needs to be improved.
        if (unit_index >= 0) {
            for (int x = 0; x < getGame().getMap().getWidth(); x++) {
                for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                    Tile tile = getGame().getMap().getTile(x, y);
                    if (tile.isCastle() && tile.getTeam() == team && getManager().canBuy(unit_index, team, x, y)) {
                        return getGame().getMap().getPosition(x, y);
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private Unit getBoughtUnit(int unit_index, int map_x, int map_y) {
        if (UnitFactory.getCommanderIndex() == unit_index) {
            Unit unit = getGame().getCommander(team);
            unit.setCurrentHp(unit.getMaxHp());
            getGame().resetUnit(unit);
            unit.clearStatus();
            unit.setX(map_x);
            unit.setY(map_y);
            return unit;
        } else {
            Unit unit = UnitFactory.createUnit(unit_index, team);
            unit.setX(map_x);
            unit.setY(map_y);
            return unit;
        }
    }

    private Unit getNextUnit(int team) {
        Array<Unit> units = new Array<Unit>();
        for (Position position : getGame().getMap().getUnitPositions()) {
            Unit unit = getGame().getMap().getUnit(position);
            if (unit.getTeam() == team && !unit.isStandby()) {
                if (unit.hasAbility(Ability.CONQUEROR) && hasVillageWithinReach(unit)) {
                    return unit;
                }
                if (unit.hasAbility(Ability.REPAIRER) && hasRuinWithinReach(unit)) {
                    return unit;
                }
                units.add(unit);
            }
        }
        if (units.size > 0) {
            return units.get(random.nextInt(units.size));
        } else {
            return null;
        }
    }

    private int getUnitWithAbilityCount(int ability) {
        int count = 0;
        for (Unit unit : getGame().getMap().getUnits(team)) {
            if (unit.hasAbility(ability) && !unit.isCommander()) {
                count++;
            }
        }
        return count;
    }

    private int getUnitCount(int unit_index) {
        int count = 0;
        for (Unit unit : getGame().getMap().getUnits(team)) {
            if (unit.getIndex() == unit_index) {
                count++;
            }
        }
        return count;
    }

    private boolean hasRuinWithinReach(Unit unit) {
        for (Position position : getManager().getMovementGenerator().createMovablePositions(unit)) {
            Tile tile = getGame().getMap().getTile(position.x, position.y);
            if (tile.isRepairable() && getGame().getMap().getUnit(position) == null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVillageWithinReach(Unit unit) {
        for (Position position : getManager().getMovementGenerator().createMovablePositions(unit)) {
            Tile tile = getGame().getMap().getTile(position.x, position.y);
            if (tile.isVillage() && tile.getTeam() != team && getGame().getMap().getUnit(position) == null) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetUnit(Unit unit) {
        return target_units.contains(unit.getUnitCode());
    }

}
