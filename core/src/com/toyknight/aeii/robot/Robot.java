package com.toyknight.aeii.robot;

import static com.toyknight.aeii.robot.BattleData.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;
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

    private final BattleData battle_data;

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
        this.battle_data = new BattleData();
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

    private BattleData getBattleData() {
        return battle_data;
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
        if (getGame().isTeamAlive(team)) {
            calculateBattleData();
            UnitBuyingOption unit_to_buy = calculateUnitToBuy();
            if (unit_to_buy.getUnitIndex() >= 0) {
                int unit_index = unit_to_buy.getUnitIndex();
                int buying_map_x = unit_to_buy.getPosition().x;
                int buying_map_y = unit_to_buy.getPosition().y;
                getManager().getOperationExecutor().submitOperation(
                        Operation.BUY, unit_index, buying_map_x, buying_map_y);
                Unit bought_unit = createBoughtUnit(unit_index, buying_map_x, buying_map_y);
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
    }

    private void calculateBattleData() {
        getBattleData().clear();
        int enemy_team_count = 0;
        int enemy_total_population = 0;
        for (int team = 0; team < 4; team++) {
            if (getGame().isTeamAlive(team) && getGame().isEnemy(this.team, team)) {
                enemy_team_count++;
                enemy_total_population += getGame().getPopulation(team);
            }
        }
        getBattleData().setValue(ENEMY_AVERAGE_POPULATION, enemy_total_population / enemy_team_count);
        int enemy_count = 0;
        int enemy_air_force_count = 0;
        int enemy_debuff_giver_count = 0;
        int enemy_total_physical_attack = 0;
        int enemy_total_physical_defence = 0;
        int enemy_total_magic_attack = 0;
        int enemy_total_magic_defence = 0;
        int enemy_total_reach_range = 0;
        int enemy_total_value = 0;

        int unit_total_value = 0;
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isEnemy(team, unit.getTeam())) {
                enemy_count++;
                if (unit.isDebuffGiver()) {
                    enemy_debuff_giver_count++;
                }
                if (unit.hasAbility(Ability.AIR_FORCE)) {
                    enemy_air_force_count++;
                }
                if (unit.getAttackType() == Unit.ATTACK_MAGIC) {
                    enemy_total_magic_attack += unit.getAttack();
                }
                enemy_total_physical_defence += unit.getPhysicalDefence();
                if (unit.getAttackType() == Unit.ATTACK_PHYSICAL) {
                    enemy_total_physical_attack += unit.getAttack();
                }
                enemy_total_magic_defence += unit.getMagicDefence();
                enemy_total_reach_range += getUnitReachRange(unit);
                enemy_total_value += unit.getPrice();
            } else {
                if (unit.getTeam() == team) {
                    unit_total_value += unit.getPrice();
                }
            }
        }
        getBattleData().setValue(ENEMY_AIR_FORCE_COUNT, enemy_air_force_count);
        getBattleData().setValue(ENEMY_DEBUFF_GIVER_COUNT, enemy_debuff_giver_count);
        getBattleData().setValue(ENEMY_AVERAGE_PHYSICAL_ATTACK, enemy_total_physical_attack / enemy_count);
        getBattleData().setValue(ENEMY_AVERAGE_PHYSICAL_DEFENCE, enemy_total_physical_defence / enemy_count);
        getBattleData().setValue(ENEMY_AVERAGE_MAGIC_ATTACK, enemy_total_magic_attack / enemy_count);
        getBattleData().setValue(ENEMY_AVERAGE_MAGIC_DEFENCE, enemy_total_magic_defence / enemy_count);
        getBattleData().setValue(ENEMY_AVERAGE_REACH_RANGE, enemy_total_reach_range / enemy_count);
        getBattleData().setValue(ENEMY_AVERAGE_TOTAL_VALUE, enemy_total_value / enemy_team_count);
        getBattleData().setValue(UNIT_TOTAL_VALUE, unit_total_value);
    }

    private UnitBuyingOption calculateUnitToBuy() {
        int unit_index = -1;
        int buying_map_x = -1;
        int buying_map_y = -1;
        int highest_buying_score = 0;

        Position buying_position = calculateBuyingPosition();
        for (Integer index : getGame().getRule().getAvailableUnits()) {
            int score = calculateBuyingScore(index, buying_position.x, buying_position.y);
            if (score > highest_buying_score) {
                unit_index = index;
                buying_map_x = buying_position.x;
                buying_map_y = buying_position.y;
                highest_buying_score = score;
            }
        }
        if (unit_index >= 0) {
            return new UnitBuyingOption(unit_index, getGame().getMap().getPosition(buying_map_x, buying_map_y));
        } else {
            return new UnitBuyingOption(unit_index, null);
        }
    }

    private Position calculateBuyingPosition() {
        return getGame().getMap().getCastlePositions(team).first();
    }

    private int calculateBuyingScore(int unit_index, int map_x, int map_y) {
        if (getManager().canBuy(unit_index, team, map_x, map_y)) {
            int score = 0;
            Unit unit = createBoughtUnit(unit_index, map_x, map_y);
            if (UnitFactory.getCommanderIndex() == unit_index && !getGame().isCommanderAlive(team)) {
                score += (getGame().getCommander(team).getLevel() + 1) * 100;
            }
            if (unit.hasAbility(Ability.CONQUEROR) && unit.getPrice() < 500) {
                score += (3 - getUnitWithAbilityCount(Ability.CONQUEROR)) * 100;
            }
            if (unit.hasAbility(Ability.REPAIRER) && unit.getPrice() < 500) {
                score += (2 - getUnitWithAbilityCount(Ability.REPAIRER)) * 100;
            }
            if (unit.getAttackType() == Unit.ATTACK_PHYSICAL) {
                score += (unit.getAttack() - getBattleData().getValue(ENEMY_AVERAGE_PHYSICAL_DEFENCE)) * 5;
            }
            if (unit.getAttackType() == Unit.ATTACK_MAGIC) {
                score += (unit.getAttack() - getBattleData().getValue(ENEMY_AVERAGE_MAGIC_DEFENCE)) * 5;
            }
            score -= (getBattleData().getValue(ENEMY_AVERAGE_PHYSICAL_ATTACK) - unit.getPhysicalDefence()) * 5;
            score -= (getBattleData().getValue(ENEMY_AVERAGE_MAGIC_ATTACK) - unit.getMagicDefence()) * 5;
            score += (getUnitReachRange(unit) - getBattleData().getValue(ENEMY_AVERAGE_REACH_RANGE)) * 10;
            if (unit.hasAbility(Ability.REFRESH_AURA)) {
                score += getBattleData().getValue(ENEMY_DEBUFF_GIVER_COUNT) * 25;
            }
            if (unit.hasAbility(Ability.MARKSMAN)) {
                score += getBattleData().getValue(ENEMY_AIR_FORCE_COUNT) * 25;
            }
            if (unit.hasAbility(Ability.HEALER)) {
                score += (2 - getUnitWithAbilityCount(Ability.HEALER)) * 50;
            }
            if (unit.hasAbility(Ability.BLOODTHIRSTY)) {
                score += getBattleData().getValue(ENEMY_AVERAGE_POPULATION) * 10;
            }
            if (unit.hasAbility(Ability.NECROMANCER)) {
                score += getGame().getMap().getTombs().size * 25;
            }
            if (unit.hasAbility(Ability.POISONER)) {
                score += 25;
            }
            if (unit.hasAbility(Ability.CHARGER)) {
                score += 25;
            }
            if (unit.hasAbility(Ability.ATTACK_AURA)) {
                score += (getBattleData().getValue(ENEMY_DEBUFF_GIVER_COUNT) + 1) * 25;
            }
            if (unit.hasAbility(Ability.CRAWLER)) {
                score += 25;
            }
            if (unit.hasAbility(Ability.AIR_FORCE)) {
                score += 25;
            }
            return score - unit.getPrice() / 5 - getUnitCount(unit_index) * 50;
        } else {
            return -1;
        }
    }

    private Unit createBoughtUnit(int unit_index, int map_x, int map_y) {
        if (UnitFactory.getCommanderIndex() == unit_index) {
            Unit commander = getGame().getCommander(team);
            Unit unit = new Unit(commander, commander.getUnitCode());
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

    private int getUnitReachRange(Unit unit) {
        int reach_range = unit.getMaxAttackRange();
        if (!unit.hasAbility(Ability.HEAVY_MACHINE)) {
            reach_range += unit.getMovementPoint();
        }
        return reach_range;
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
