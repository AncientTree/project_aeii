package com.toyknight.aeii.robot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.manager.Operation;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.Random;

/**
 * @author toyknight 1/12/2016.
 */
public class Robot {

    private final Random random;

    private final GameManager manager;

    private final ObjectSet<String> target_units;

    private final ObjectMap<String, Position> routes;

    private final ObjectMap<Integer, ObjectSet<Integer>> ability_map;

    private boolean prepared;

    private boolean calculating;

    private int team;

    private int action_type;

    private Position move_target;

    private Position action_target;

    public Robot(GameManager manager) {
        this.manager = manager;
        this.random = new Random();
        this.target_units = new ObjectSet<String>();
        this.routes = new ObjectMap<String, Position>();
        this.ability_map = new ObjectMap<Integer, ObjectSet<Integer>>();
    }

    public void initialize() {
        prepared = false;
        calculating = false;
        ability_map.clear();
        for (Integer index : getGame().getRule().getAvailableUnits()) {
            for (int ability : UnitFactory.getSample(index).getAbilities()) {
                if (ability_map.containsKey(ability)) {
                    ability_map.get(ability).add(index);
                } else {
                    ability_map.put(ability, new ObjectSet<Integer>());
                    ability_map.get(ability).add(index);
                }
            }
        }
        target_units.clear();
        //TODO: Add target units from rule.
    }

    public GameManager getManager() {
        return manager;
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    public ObjectSet<String> getTargetUnits() {
        return target_units;
    }

    private int getGold() {
        return getGame().getPlayer(team).getGold();
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
        if (!prepared) {
            prepare();
        }
        switch (getManager().getState()) {
            case GameManager.STATE_SELECT:
                move_target = null;
                action_target = null;
                action_type = -1;
                if (!checkCastleOccupation()) {
                    if (needRecruiting()) {
                        recruit();
                    } else {
                        select();
                    }
                }
                break;
            case GameManager.STATE_MOVE:
                move();
                break;
            case GameManager.STATE_ACTION:
                act();
                break;
            case GameManager.STATE_REMOVE:
                remove();
                break;
            default:
                finish();
        }
    }

    public void prepare() {
        team = getGame().getCurrentTeam();
        routes.clear();
        prepared = true;
    }

    private boolean checkCastleOccupation() {
        Unit commander = getGame().getCommander(team);
        if (getGame().isCommanderAlive(team) && !commander.isStandby()) {
            ObjectSet<Position> positions = getManager().getPositionGenerator().createMovablePositions(commander);
            for (Position position : positions) {
                Tile tile = getGame().getMap().getTile(position);
                if (tile.isCastle() && (tile.getTeam() < 0 || getGame().isEnemy(team, tile.getTeam()))) {
                    move_target = position;
                    action_target = position;
                    action_type = Operation.OCCUPY;
                    getManager().doSelect(commander.getX(), commander.getY());
                    return true;
                }
            }
        }
        return false;
    }

    private void recruit() {
        Position recruit_position = getRecruitingCastlePosition();
        if (recruit_position == null) {
            select();
        } else {
            if (!getGame().isCommanderAlive(team) && getGame().getCommander(team).getPrice() <= getGold()) {
                getManager().doBuyUnit(UnitFactory.getCommanderIndex(), recruit_position.x, recruit_position.y);
            } else {
                int index;
                if (getAllyCountWithAbility(Ability.CONQUEROR) < 4) {
                    index = getCheapestUnitIndexWithAbility(Ability.CONQUEROR);
                } else if (getGame().getMap().getTombs().size > 1 && getAllyCountWithAbility(Ability.NECROMANCER) < 1) {
                    index = getCheapestUnitIndexWithAbility(Ability.NECROMANCER);
                } else if (getAllyCountWithAbility(Ability.HEALER) < 1) {
                    index = getCheapestUnitIndexWithAbility(Ability.HEALER);
                } else if (getUnhealthyAllyCount() >= 5 && getAllyCountWithAbility(Ability.REFRESH_AURA) < 1) {
                    index = getCheapestUnitIndexWithAbility(Ability.REFRESH_AURA);
                } else if (getEnemyCountWithAbility(Ability.AIR_FORCE) > 0 && getAllyCountWithAbility(Ability.MARKSMAN) < 1) {
                    index = getCheapestUnitIndexWithAbility(Ability.MARKSMAN);
                } else {
                    if (getEnemyAveragePhysicalDefence() > getEnemyAverageMagicDefence()) {
                        index = getRandomAffordableUnitIndexWithAttackType(Unit.ATTACK_MAGIC);
                    } else {
                        index = getRandomAffordableUnitIndexWithAttackType(Unit.ATTACK_PHYSICAL);
                    }
                }
                if (index >= 0 && getManager().canBuy(index, team, recruit_position.x, recruit_position.y)) {
                    getManager().doBuyUnit(index, recruit_position.x, recruit_position.y);
                } else {
                    select();
                }

            }
        }
    }

    private void select() {
        Unit refresher = getFirstUnitWithAbility(Ability.REFRESH_AURA);
        if (refresher != null && !refresher.isStandby()) {
            getManager().doSelect(refresher.getX(), refresher.getY());
            return;
        }
        Unit healer = getFirstUnitWithAbility(Ability.HEALER);
        if (healer != null && !healer.isStandby()) {
            getManager().doSelect(healer.getX(), healer.getY());
            return;
        }
        for (Unit unit : getGame().getMap().getUnits(team)) {
            if (!unit.isStandby()) {
                getManager().doSelect(unit.getX(), unit.getY());
                return;
            }
        }
        finish();
    }

    private void move() {
        if (move_target == null) {
            calculateMoveAndAction();
        } else {
            getManager().doMove(move_target.x, move_target.y);
        }
    }

    private void calculateMoveAndAction() {
        Position cp;
        Position vp;
        Position rp;
        Unit selected_unit = getManager().getSelectedUnit();
        ObjectSet<Unit> allies = getAlliesWithinReach(selected_unit);
        ObjectSet<Unit> enemies = getEnemiesWithinReach(selected_unit);
        ObjectSet<Position> tombs = getTombPositionsWithinReach(selected_unit);
        ObjectSet<Position> movable_positions = getManager().getMovablePositions();

        Unit ally;
        if (selected_unit.hasAbility(Ability.NECROMANCER) && tombs.size > 0) {
            Position target = getPreferredSummonTarget(selected_unit, tombs);
            Position summon_position = getPreferredSummonPosition(
                    selected_unit, target, getManager().getMovablePositions());
            submitAction(summon_position, target, Operation.SUMMON);
        } else if (selected_unit.hasAbility(Ability.HEALER) && allies.size > 0
                && (ally = getPreferredHealTarget(selected_unit, allies)) != null) {
            Position heal_position = getPreferredHealPosition(selected_unit, ally, getManager().getMovablePositions());
            submitAction(heal_position, getGame().getMap().getPosition(ally), Operation.HEAL);
        } else if (selected_unit.hasAbility(Ability.COMMANDER)
                && (cp = getNearestEnemyCastlePositionWithinReach(selected_unit, movable_positions)) != null) {
            submitAction(cp, cp, Operation.OCCUPY);
        } else if (selected_unit.hasAbility(Ability.CONQUEROR)
                && (vp = getNearestEnemyVillagePositionWithinReach(selected_unit, movable_positions)) != null) {
            submitAction(vp, vp, Operation.OCCUPY);
        } else if (selected_unit.hasAbility(Ability.REPAIRER)
                && (rp = getNearestRuinPositionWithinReach(selected_unit, movable_positions)) != null) {
            submitAction(rp, rp, Operation.REPAIR);
        } else {
            Unit target_enemy;
            Position attack_position;
            if (enemies.size > 0
                    && (target_enemy = getPreferredAttackTarget(selected_unit, enemies)) != null
                    && (attack_position = getPreferredAttackPosition(selected_unit, target_enemy, getManager().getMovablePositions())) != null) {
                submitAction(attack_position, getGame().getMap().getPosition(target_enemy), Operation.ATTACK);
            } else {
                ObjectSet<Unit> enemy_commanders = getEnemyCommanders();
                if (selected_unit.isCommander()) {
                    ObjectSet<Position> enemy_castle_positions = getEnemyCastlePositions();
                    if (enemy_castle_positions.size > 0) {
                        Position move_position = getManager().getPositionGenerator().getNextPositionToTarget(
                                selected_unit, enemy_castle_positions.first());
                        submitAction(move_position, move_position, Operation.STANDBY);
                    } else {
                        Position move_position =
                                getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                        submitAction(move_position, move_position, Operation.STANDBY);
                    }
                } else if (selected_unit.hasAbility(Ability.CONQUEROR)) {
                    ObjectSet<Position> enemy_village_positions = getEnemyVillagePositions();
                    if (enemy_village_positions.size > 0) {
                        Position move_position = getManager().getPositionGenerator().getNextPositionToTarget(
                                selected_unit, enemy_village_positions.first());
                        submitAction(move_position, move_position, Operation.STANDBY);
                    } else {
                        Position move_position =
                                getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                        submitAction(move_position, move_position, Operation.STANDBY);
                    }
                } else {
                    if (enemy_commanders.size > 0) {
                        Position move_position = getManager().getPositionGenerator().getNextPositionToTarget(
                                selected_unit, getGame().getMap().getPosition(enemy_commanders.first()));
                        submitAction(move_position, move_position, Operation.STANDBY);
                    } else {
                        ObjectSet<Unit> all_enemies = getEnemyUnits();
                        Position move_position = all_enemies.size > 0 ?
                                getManager().getPositionGenerator().getNextPositionToTarget(
                                        selected_unit, getGame().getMap().getPosition(all_enemies.first())) :
                                getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                        submitAction(move_position, move_position, Operation.STANDBY);
                    }
                }
            }
        }
    }

    private void submitAction(Position move_position, Position action_target, int action_type) {
        this.action_type = action_type;
        this.action_target = action_target;
        getManager().doMove(move_position.x, move_position.y);
    }

    private Unit getPreferredAttackTarget(Unit unit, ObjectSet<Unit> enemies) {
        Unit target = null;
        int min_remaining_hp = Integer.MAX_VALUE;
        for (Unit enemy : enemies) {
            int damage = getManager().getUnitToolkit().getDamage(unit, enemy);
            int remaining_hp = enemy.getCurrentHp() - damage;
            if (enemy.getCurrentHp() - damage <= 0) {
                return enemy;
            }
            if (unit.hasAbility(Ability.POISONER) || damage >= 10) {
                if (enemy.isCommander() && damage >= 20) {
                    return enemy;
                }
                if (remaining_hp < min_remaining_hp) {
                    target = enemy;
                    min_remaining_hp = remaining_hp;
                }
            }
        }
        return target;
    }

    private Unit getPreferredHealTarget(Unit unit, ObjectSet<Unit> allies) {
        Unit target = null;
        int max_attack = Integer.MIN_VALUE;
        for (Unit ally : allies) {
            if (!ally.isStandby() && team == ally.getTeam()
                    && !UnitToolkit.isTheSameUnit(unit, ally)
                    && canHeal(unit, ally) && ally.getAttack() > max_attack) {
                target = ally;
                max_attack = ally.getAttack();
            }
        }
        return target;
    }

    private Position getPreferredSummonTarget(Unit unit, ObjectSet<Position> tomb_positions) {
        Position preferred_position = null;
        int min_distance = Integer.MAX_VALUE;
        for (Position position : tomb_positions) {
            if (getDistance(getGame().getMap().getPosition(unit), position) < min_distance) {
                preferred_position = position;
                min_distance = getDistance(getGame().getMap().getPosition(unit), position);
            }
        }
        return preferred_position;
    }

    public boolean canHeal(Unit healer, Unit target) {
        if (healer == null || target == null) {
            return false;
        } else {
            if (getGame().canReceiveHeal(target)) {
                return !getGame().isEnemy(healer, target)
                        && getGame().canHealReachTarget(healer, target);
            } else {
                //heal becomes damage for the undead
                return healer.hasAbility(Ability.HEALER) && target.hasAbility(Ability.UNDEAD);
            }
        }
    }

    private Position getPreferredAttackPosition(Unit unit, Unit target, ObjectSet<Position> movable_positions) {
        Position preferred_position = null;
        int max_defence_bonus = Integer.MIN_VALUE;
        for (Position position : movable_positions) {
            Tile tile = getGame().getMap().getTile(position);
            if (UnitToolkit.isWithinRange(
                    position.x, position.y, target.getX(), target.getY(),
                    unit.getMinAttackRange(), unit.getMaxAttackRange())) {
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_SEA) && tile.getType() == Tile.TYPE_WATER) {
                    return position;
                }
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_FOREST) && tile.getType() == Tile.TYPE_FOREST) {
                    return position;
                }
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN) && tile.getType() == Tile.TYPE_MOUNTAIN) {
                    return position;
                }
                if (preferred_position == null) {
                    preferred_position = position;
                    max_defence_bonus = tile.getDefenceBonus();
                } else {
                    if (UnitToolkit.getRange(target.getX(), target.getY(), position.x, position.y) > 1) {
                        if (UnitToolkit.getRange(
                                target.getX(), target.getY(), preferred_position.x, preferred_position.y) > 1) {
                            if (tile.getDefenceBonus() > max_defence_bonus) {
                                preferred_position = position;
                                max_defence_bonus = tile.getDefenceBonus();
                            }
                        } else {
                            preferred_position = position;
                            max_defence_bonus = tile.getDefenceBonus();
                        }
                    } else {
                        if (tile.getDefenceBonus() > max_defence_bonus && UnitToolkit.getRange(
                                target.getX(), target.getY(), preferred_position.x, preferred_position.y) == 1) {
                            preferred_position = position;
                            max_defence_bonus = tile.getDefenceBonus();
                        }
                    }
                }
            }
        }
        return preferred_position;
    }

    private Position getPreferredHealPosition(Unit unit, Unit target, ObjectSet<Position> movable_positions) {
        for (Position position : movable_positions) {
            if (UnitToolkit.isWithinRange(
                    position.x, position.y, target.getX(), target.getY(),
                    unit.getMinAttackRange(), unit.getMaxAttackRange())) {
                return position;
            }
        }
        return null;
    }

    private Position getPreferredSummonPosition(Unit unit, Position tomb_position, ObjectSet<Position> movable_positions) {
        for (Position position : movable_positions) {
            if (UnitToolkit.isWithinRange(
                    position.x, position.y, tomb_position.x, tomb_position.y,
                    unit.getMinAttackRange(), unit.getMaxAttackRange())) {
                return position;
            }
        }
        return null;
    }

    private Position getPreferredStandbyPosition(Unit unit, ObjectSet<Position> movable_positions) {
        Position preferred_position = null;
        int max_defence_bonus = Integer.MIN_VALUE;
        for (Position position : movable_positions) {
            Tile tile = getGame().getMap().getTile(position);
            if (getManager().getUnitToolkit().getTerrainHeal(unit, tile) > 0) {
                return position;
            }
            if (tile.getDefenceBonus() > max_defence_bonus) {
                preferred_position = position;
                max_defence_bonus = tile.getDefenceBonus();
            }
        }
        return preferred_position;
    }

    private void act() {
        switch (action_type) {
            case Operation.OCCUPY:
                getManager().doOccupy();
                break;
            case Operation.REPAIR:
                getManager().doRepair();
                break;
            case Operation.ATTACK:
                getManager().doAttack(action_target.x, action_target.y);
                break;
            case Operation.HEAL:
                getManager().doHeal(action_target.x, action_target.y);
                break;
            case Operation.SUMMON:
                getManager().doSummon(action_target.x, action_target.y);
                break;
            case Operation.STANDBY:
            default:
                getManager().doStandbySelectedUnit();
        }
    }

    private void remove() {
        Position target = getPreferredStandbyPosition(getManager().getSelectedUnit(), getManager().getMovablePositions());
        getManager().doMove(target.x, target.y);
    }

    private void finish() {
        getManager().doEndTurn();
        prepared = false;
    }

    private boolean needRecruiting() {
        return getGame().getMap().getCastleCount(team) > 0 &&
                getGame().getPopulation(team) < getGame().getMaxPopulation();
    }

    private Position getRecruitingCastlePosition() {
        Position position = null;
        int min_enemy_distance = Integer.MIN_VALUE;
        for (Position cp : getGame().getMap().getCastlePositions(team)) {
            Unit unit = getGame().getMap().getUnit(cp);
            if (unit == null || (unit.isCommander() && unit.getTeam() == team)) {
                if (position == null) {
                    position = cp;
                    min_enemy_distance = getEnemyDistance(cp);
                } else {
                    int distance = getEnemyDistance(cp);
                    if (distance < min_enemy_distance) {
                        position = cp;
                        min_enemy_distance = getEnemyDistance(cp);
                    }
                }
            }
        }
        return position;
    }

    private int getEnemyDistance(Position position) {
        int total_distance = 0;
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isEnemy(team, unit.getTeam())) {
                int distance = getDistance(getGame().getMap().getPosition(unit), position);
                if (distance <= 4) {
                    return distance;
                }
                total_distance += distance;
            }
        }
        for (Position cp : getGame().getMap().getCastlePositions()) {
            if (getGame().isEnemy(team, getGame().getMap().getTile(cp).getTeam())) {
                total_distance += getDistance(cp, position);
            }
        }
        return total_distance;
    }

    private int getAllyCountWithAbility(int ability) {
        int count = 0;
        for (Unit unit : getGame().getMap().getUnits(team)) {
            if (unit.hasAbility(ability)) {
                count++;
            }
        }
        return count;
    }

    private int getEnemyCountWithAbility(int ability) {
        int count = 0;
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isEnemy(team, unit.getTeam()) && unit.hasAbility(ability)) {
                count++;
            }
        }
        return count;
    }

    private int getUnhealthyAllyCount() {
        int count = 0;
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isAlly(team, unit.getTeam())
                    && (Status.isDebuff(unit.getStatus()) || unit.getCurrentHp() < unit.getMaxHp())) {
                count++;
            }
        }
        return count;
    }

    private int getCheapestUnitIndexWithAbility(int ability) {
        if (ability_map.containsKey(ability)) {
            int cheapest_index = -1;
            int cheapest_price = Integer.MAX_VALUE;
            for (Integer index : ability_map.get(ability)) {
                int price = getGame().getUnitPrice(index, team);
                if (price >= 0 && price < cheapest_price) {
                    cheapest_index = index;
                    cheapest_price = price;
                }
            }
            return cheapest_index;
        } else {
            return -1;
        }
    }

    private Unit getFirstUnitWithAbility(int ability) {
        for (Unit unit : getGame().getMap().getUnits(team)) {
            if (unit.hasAbility(ability)) {
                return unit;
            }
        }
        return null;
    }

    private int getEnemyAveragePhysicalDefence() {
        int enemy_number = 0;
        int enemy_total_physical_defence = 0;
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isEnemy(team, unit.getTeam())) {
                enemy_number++;
                enemy_total_physical_defence += unit.getPhysicalDefence();
            }
        }
        if (enemy_number > 0) {
            return enemy_total_physical_defence / enemy_number;
        } else {
            return 0;
        }
    }

    private int getEnemyAverageMagicDefence() {
        int enemy_number = 0;
        int enemy_total_magic_defence = 0;
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isEnemy(team, unit.getTeam())) {
                enemy_number++;
                enemy_total_magic_defence += unit.getMagicDefence();
            }
        }
        if (enemy_number > 0) {
            return enemy_total_magic_defence / enemy_number;
        } else {
            return 0;
        }
    }

    private int getRandomAffordableUnitIndexWithAttackType(int attack_type) {
        Array<Integer> units = new Array<Integer>();
        for (Integer index : getGame().getRule().getAvailableUnits()) {
            if (getGame().getUnitPrice(index, team) < getGold()
                    && getGame().canAddPopulation(team, UnitFactory.getSample(index).getOccupancy())
                    && index != UnitFactory.getCommanderIndex()
                    && UnitFactory.getSample(index).getAttack() >= 50
                    && UnitFactory.getSample(index).getAttackType() == attack_type) {
                units.add(index);
            }
        }
        if (units.size > 0) {
            return units.get(random.nextInt(units.size));
        } else {
            return -1;
        }
    }

    private ObjectSet<Unit> getEnemiesWithinReach(Unit unit) {
        ObjectSet<Unit> enemies = new ObjectSet<Unit>();
        for (Position position : getManager().getPositionGenerator().createPositionsWithinReach(unit)) {
            Unit target = getGame().getMap().getUnit(position);
            if (target != null && getGame().isEnemy(unit, target)) {
                enemies.add(target);
            }
        }
        return enemies;
    }

    private ObjectSet<Unit> getAlliesWithinReach(Unit unit) {
        ObjectSet<Unit> allies = new ObjectSet<Unit>();
        for (Position position : getManager().getPositionGenerator().createPositionsWithinReach(unit)) {
            Unit target = getGame().getMap().getUnit(position);
            if (target != null && getGame().isAlly(unit, target)) {
                allies.add(target);
            }
        }
        return allies;
    }

    private ObjectSet<Unit> getEnemyCommanders() {
        ObjectSet<Unit> commanders = new ObjectSet<Unit>();
        for (Unit unit : getGame().getMap().getUnits()) {
            if (unit.isCommander() && getGame().isEnemy(team, unit.getTeam())) {
                commanders.add(unit);
            }
        }
        return commanders;
    }

    private ObjectSet<Position> getEnemyCastlePositions() {
        ObjectSet<Position> positions = new ObjectSet<Position>();
        for (Position position : getGame().getMap().getCastlePositions()) {
            Tile tile = getGame().getMap().getTile(position);
            if (!getGame().isAlly(team, tile.getTeam())) {
                positions.add(position);
            }
        }
        return positions;
    }

    private ObjectSet<Position> getEnemyVillagePositions() {
        ObjectSet<Position> positions = new ObjectSet<Position>();
        for (Position position : getGame().getMap().getVillagePositions()) {
            Tile tile = getGame().getMap().getTile(position);
            if (!getGame().isAlly(team, tile.getTeam())) {
                positions.add(position);
            }
        }
        return positions;
    }

    private Position getNearestEnemyCastlePositionWithinReach(Unit unit, ObjectSet<Position> movable_positions) {
        Position nearest_position = null;
        int min_distance = Integer.MAX_VALUE;
        for (Position position : movable_positions) {
            Tile tile = getGame().getMap().getTile(position);
            Position unit_position = getGame().getMap().getPosition(unit);
            if (tile.isCastle()
                    && !getGame().isAlly(team, tile.getTeam())
                    && getDistance(unit_position, position) < min_distance) {
                nearest_position = position;
                min_distance = getDistance(unit_position, position);
            }
        }
        return nearest_position;
    }

    private Position getNearestEnemyVillagePositionWithinReach(Unit unit, ObjectSet<Position> movable_positions) {
        Position nearest_position = null;
        int min_distance = Integer.MAX_VALUE;
        for (Position position : movable_positions) {
            Tile tile = getGame().getMap().getTile(position);
            Position unit_position = getGame().getMap().getPosition(unit);
            if (tile.isVillage()
                    && !getGame().isAlly(team, tile.getTeam())
                    && getDistance(unit_position, position) < min_distance) {
                nearest_position = position;
                min_distance = getDistance(unit_position, position);
            }
        }
        return nearest_position;
    }

    private Position getNearestRuinPositionWithinReach(Unit unit, ObjectSet<Position> movable_positions) {
        Position nearest_position = null;
        int min_distance = Integer.MAX_VALUE;
        for (Position position : movable_positions) {
            Tile tile = getGame().getMap().getTile(position);
            Position unit_position = getGame().getMap().getPosition(unit);
            if (tile.isRepairable() && getDistance(unit_position, position) < min_distance) {
                nearest_position = position;
                min_distance = getDistance(unit_position, position);
            }
        }
        return nearest_position;
    }

    private ObjectSet<Position> getTombPositionsWithinReach(Unit unit) {
        ObjectSet<Tomb> all_tombs = new ObjectSet<Tomb>(getGame().getMap().getTombs());
        ObjectSet<Position> reachable_positions = getManager().getPositionGenerator().createPositionsWithinReach(unit);
        ObjectSet<Position> tomb_positions = new ObjectSet<Position>();
        for (Tomb tomb : all_tombs) {
            Position tomb_position = getGame().getMap().getPosition(tomb.x, tomb.y);
            if (reachable_positions.contains(tomb_position)) {
                tomb_positions.add(tomb_position);
            }
        }
        return tomb_positions;
    }

    private ObjectSet<Unit> getEnemyUnits() {
        ObjectSet<Unit> enemies = new ObjectSet<Unit>();
        for (Unit unit : getGame().getMap().getUnits()) {
            if (getGame().isEnemy(team, unit.getTeam())) {
                enemies.add(unit);
            }
        }
        return enemies;
    }

    private int getDistance(Position p1, Position p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    private final Runnable calculate_task = new Runnable() {
        @Override
        public void run() {
            try {
                calculating = true;
                Thread.sleep(150);
                doCalculate();
                calculating = false;
            } catch (InterruptedException ignored) {
            }
        }
    };

}
