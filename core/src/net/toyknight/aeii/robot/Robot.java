package net.toyknight.aeii.robot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.manager.Operation;
import net.toyknight.aeii.utils.UnitFactory;
import net.toyknight.aeii.utils.UnitToolkit;

import java.util.Random;

/**
 * @author toyknight 1/12/2016.
 */
public class Robot {

    private final Random random;

    private final GameManager manager;

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
    }

    public GameManager getManager() {
        return manager;
    }

    public GameCore getGame() {
        return getManager().getGame();
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
        synchronized (GameContext.RENDER_LOCK) {
            Unit commander = getGame().getCommander(team);
            if (getGame().isCommanderAlive(team) && !commander.isStandby() && commander.getCurrentHp() > 0 && !commander.isStatic()) {
                ObjectSet<Position> positions = getManager().getPositionGenerator().createMovablePositions(commander);
                for (Position position : positions) {
                    if (canOccupyCastle(commander, position)) {
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
    }

    private boolean canOccupyCastle(Unit commander, Position castle_position) {
        Tile tile = getGame().getMap().getTile(castle_position);
        return tile.isCastle()
                && (!commander.isStatic() || commander.isAt(castle_position))
                && (tile.getTeam() < 0 || getGame().isEnemy(team, tile.getTeam()));
    }

    private void recruit() {
        Position recruit_position = getRecruitingCastlePosition();
        if (recruit_position == null) {
            select();
        } else {
            boolean commander_alive;
            synchronized (GameContext.RENDER_LOCK) {
                commander_alive = getGame().isCommanderAlive(team);
            }
            if (!commander_alive && getGame().getCommander(team).getPrice() <= getGold()) {
                getManager().doBuyUnit(UnitFactory.getCommanderIndex(), recruit_position.x, recruit_position.y);
            } else {
                int index;
                if (getAllyCountWithAbility(Ability.CONQUEROR) < 4) {
                    index = getCheapestUnitIndexWithAbility(Ability.CONQUEROR);
                } else if (getGame().getMap().getTombs().size > 1
                        && getAllyCountWithAbility(Ability.NECROMANCER) < 1
                        && ability_map.containsKey(Ability.NECROMANCER)) {
                    index = getCheapestUnitIndexWithAbility(Ability.NECROMANCER);
                } else if (getAllyCountWithAbility(Ability.HEALER) < 1 && ability_map.containsKey(Ability.HEALER)) {
                    index = getCheapestUnitIndexWithAbility(Ability.HEALER);
                } else if (getUnhealthyAllyCount() >= 5
                        && getAllyCountWithAbility(Ability.REFRESH_AURA) < 1
                        && ability_map.containsKey(Ability.REFRESH_AURA)) {
                    index = getCheapestUnitIndexWithAbility(Ability.REFRESH_AURA);
                } else if (getEnemyCountWithAbility(Ability.AIR_FORCE) > 0
                        && getAllyCountWithAbility(Ability.MARKSMAN) < 1
                        && ability_map.containsKey(Ability.MARKSMAN)) {
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
        synchronized (GameContext.RENDER_LOCK) {
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
    }

    private void move() {
        if (move_target == null) {
            calculateMoveAndAction();
        } else {
            getManager().doMove(move_target.x, move_target.y);
        }
    }

    private void calculateMoveAndAction() {
        Unit selected_unit = getManager().getSelectedUnit();
        if (selected_unit.isStatic()) {
            Position standby_position = getGame().getMap().getPosition(selected_unit);
            if (getManager().hasEnemyWithinRange(selected_unit)) {
                Position attack_target = null;
                for (Position position :
                        getManager().getPositionGenerator().createAttackablePositions(selected_unit, false)) {
                    if (getGame().isEnemy(selected_unit, getGame().getMap().getUnit(position))) {
                        attack_target = position;
                        break;
                    }
                }
                if (attack_target == null) {
                    submitAction(standby_position, standby_position, Operation.STANDBY);
                } else {
                    submitAction(standby_position, attack_target, Operation.ATTACK);
                }
            } else {
                submitAction(standby_position, standby_position, Operation.STANDBY);
            }
            return;
        }
        Position cp;
        Position vp;
        Position rp;
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
        } else if (selected_unit.hasAbility(Ability.COMMANDER)
                && (cp = getNearestEnemyCastlePositionWithinReach(selected_unit, movable_positions)) != null) {
            submitAction(cp, cp, Operation.OCCUPY);
        } else if (selected_unit.hasAbility(Ability.CONQUEROR)
                && (vp = getNearestEnemyVillagePositionWithinReach(selected_unit, movable_positions)) != null) {
            submitAction(vp, vp, Operation.OCCUPY);
        } else if (selected_unit.hasAbility(Ability.REPAIRER)
                && (rp = getNearestRuinPositionWithinReach(selected_unit, movable_positions)) != null) {
            submitAction(rp, rp, Operation.REPAIR);
        } else if (selected_unit.hasAbility(Ability.HEALER) && allies.size > 0
                && (ally = getPreferredHealTarget(selected_unit, allies)) != null) {
            Position heal_position = getPreferredHealPosition(selected_unit, ally, getManager().getMovablePositions());
            if (heal_position == null) {
                Position move_position = getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                submitAction(move_position, move_position, Operation.HEAL);
            } else {
                submitAction(heal_position, getGame().getMap().getPosition(ally), Operation.HEAL);
            }
        } else {
            Unit target_enemy;
            Position preferred_target;
            Position attack_position;
            if (!selected_unit.hasAbility(Ability.HEAVY_MACHINE) && enemies.size > 0
                    && (target_enemy = getPreferredAttackTarget(selected_unit, enemies)) != null
                    && (attack_position = getPreferredAttackPosition(selected_unit, target_enemy, getManager().getMovablePositions())) != null) {
                submitAction(attack_position, getGame().getMap().getPosition(target_enemy), Operation.ATTACK);
            } else if ((selected_unit.hasAbility(Ability.HEAVY_MACHINE)
                    && movable_positions.contains(getGame().getMap().getPosition(selected_unit))
                    && (preferred_target = getPreferredTargetWithinRange(selected_unit)) != null)) {
                submitAction(getGame().getMap().getPosition(selected_unit), preferred_target, Operation.ATTACK);
            } else {
                ObjectSet<Unit> enemy_commanders = getEnemyCommanders();
                if (selected_unit.isCommander()) {
                    ObjectSet<Position> enemy_castle_positions = getEnemyCastlePositions();
                    if (enemy_castle_positions.size > 0) {
                        Position move_position = getManager().getPositionGenerator().getNextPositionToTarget(
                                selected_unit, enemy_castle_positions.first());
                        //TODO: Remove this check after path finding is improved
                        if (!getGame().canUnitMove(selected_unit, move_position.x, move_position.y)) {
                            move_position = getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                        }
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
                        //TODO: Remove this check after path finding is improved
                        if (!getGame().canUnitMove(selected_unit, move_position.x, move_position.y)) {
                            move_position = getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                        }
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
                        //TODO: Remove this check after path finding is improved
                        if (!getGame().canUnitMove(selected_unit, move_position.x, move_position.y)) {
                            move_position = getPreferredStandbyPosition(selected_unit, getManager().getMovablePositions());
                        }
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
        Unit selected_unit = getManager().getSelectedUnit();
        if (selected_unit.hasAbility(Ability.HEAVY_MACHINE) &&
                !move_position.equals(getGame().getMap().getPosition(selected_unit))) {
            action_type = Operation.STANDBY;
        }
        this.action_type = action_type;
        this.action_target = action_target;
        getManager().doMove(move_position.x, move_position.y);
        switch (action_type) {
            case Operation.ATTACK:
                System.out.print("attack");
                break;
            case Operation.HEAL:
                System.out.print("heal");
                break;
            case Operation.SUMMON:
                System.out.print("summon");
                break;
            case Operation.OCCUPY:
                System.out.print("occupy");
                break;
            case Operation.REPAIR:
                System.out.print("repair");
                break;
            case Operation.STANDBY:
                System.out.print("standby");
                break;
        }
        System.out.print(" ");
        System.out.println(action_target == null ? "null" : String.format("[%d, %d]", action_target.x, action_target.y));
    }

    private Unit getPreferredAttackTarget(Unit unit, ObjectSet<Unit> enemies) {
        Unit target = null;
        int min_remaining_hp = Integer.MAX_VALUE;
        for (Unit enemy : enemies) {
            int damage = getManager().getUnitToolkit().getDamage(unit, enemy, false);
            int remaining_hp = enemy.getCurrentHp() - damage;
            if (remaining_hp <= 0) {
                return enemy;
            }
            if (unit.hasAbility(Ability.POISONER) || damage >= 10) {
                if (enemy.isCrystal()) {
                    return enemy;
                }
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

    private Position getPreferredTargetWithinRange(Unit unit) {
        synchronized (GameContext.RENDER_LOCK) {
            ObjectSet<Position> attackable_positions =
                    getManager().getPositionGenerator().createAttackablePositions(unit, false);

            Position target_position = null;
            int min_remaining_hp = Integer.MAX_VALUE;
            for (Position position : attackable_positions) {
                Unit target = getGame().getMap().getUnit(position);
                if (target == null) {
                    Tile tile = getGame().getMap().getTile(position);
                    if (unit.hasAbility(Ability.DESTROYER)
                            && tile.isDestroyable() && getGame().isEnemy(unit.getTeam(), tile.getTeam())) {
                        return position;
                    }
                } else {
                    if (getGame().isEnemy(unit, target)) {
                        int damage = getManager().getUnitToolkit().getDamage(unit, target, false);
                        int remaining_hp = target.getCurrentHp() - damage;
                        if (remaining_hp <= 0) {
                            return position;
                        }
                        if (unit.hasAbility(Ability.POISONER) || damage >= 10) {
                            if (target.isCrystal()) {
                                return position;
                            }
                            if (target.isCommander() && damage >= 20) {
                                return position;
                            }
                            if (remaining_hp < min_remaining_hp) {
                                target_position = position;
                                min_remaining_hp = remaining_hp;
                            }
                        }
                    }
                }
            }
            return target_position;
        }
    }

    private Unit getPreferredHealTarget(Unit unit, ObjectSet<Unit> allies) {
        Unit target = null;
        int max_attack = Integer.MIN_VALUE;
        for (Unit ally : allies) {
            if (!ally.isStandby() && team == ally.getTeam() && !ally.isSkeleton()
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
            if (healer.hasAbility(Ability.HEALER) && getGame().canHealReachTarget(healer, target)) {
                if (getGame().canReceiveHeal(target)) {
                    return !getGame().isEnemy(healer, target)
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
        Position preferred_position = movable_positions.first();
        int max_defence_bonus = Integer.MIN_VALUE;
        for (Position position : movable_positions) {
            Tile tile = getGame().getMap().getTile(position);
            if (unit.isCommander() || !tile.isCastle()) {
                if (getManager().getUnitToolkit().getTerrainHeal(unit, tile) > 0) {
                    return position;
                }
                if (tile.getDefenceBonus() > max_defence_bonus) {
                    preferred_position = position;
                    max_defence_bonus = tile.getDefenceBonus();
                }
            }
        }
        return preferred_position;
    }

    private void act() {
        synchronized (GameContext.RENDER_LOCK) {
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
        synchronized (GameContext.RENDER_LOCK) {
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
    }

    private int getEnemyDistance(Position position) {
        synchronized (GameContext.RENDER_LOCK) {
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
    }

    private int getAllyCountWithAbility(int ability) {
        synchronized (GameContext.RENDER_LOCK) {
            int count = 0;
            for (Unit unit : getGame().getMap().getUnits(team)) {
                if (unit.hasAbility(ability)) {
                    count++;
                }
            }
            return count;
        }
    }

    private int getEnemyCountWithAbility(int ability) {
        synchronized (GameContext.RENDER_LOCK) {
            int count = 0;
            for (Unit unit : getGame().getMap().getUnits()) {
                if (getGame().isEnemy(team, unit.getTeam()) && unit.hasAbility(ability)) {
                    count++;
                }
            }
            return count;
        }
    }

    private int getUnhealthyAllyCount() {
        synchronized (GameContext.RENDER_LOCK) {
            int count = 0;
            for (Unit unit : getGame().getMap().getUnits()) {
                if (getGame().isAlly(team, unit.getTeam())
                        && (Status.isDebuff(unit.getStatus()) || unit.getCurrentHp() < unit.getMaxHp())) {
                    count++;
                }
            }
            return count;
        }
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
        synchronized (GameContext.RENDER_LOCK) {
            for (Unit unit : getGame().getMap().getUnits(team)) {
                if (unit.hasAbility(ability)) {
                    return unit;
                }
            }
            return null;
        }
    }

    private int getEnemyAveragePhysicalDefence() {
        int enemy_number = 0;
        int enemy_total_physical_defence = 0;
        synchronized (GameContext.RENDER_LOCK) {
            for (Unit unit : getGame().getMap().getUnits()) {
                if (getGame().isEnemy(team, unit.getTeam())) {
                    enemy_number++;
                    enemy_total_physical_defence += unit.getPhysicalDefence();
                }
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
        synchronized (GameContext.RENDER_LOCK) {
            for (Unit unit : getGame().getMap().getUnits()) {
                if (getGame().isEnemy(team, unit.getTeam())) {
                    enemy_number++;
                    enemy_total_magic_defence += unit.getMagicDefence();
                }
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
        synchronized (GameContext.RENDER_LOCK) {
            ObjectSet<Unit> commanders = new ObjectSet<Unit>();
            for (Unit unit : getGame().getMap().getUnits()) {
                if (unit.isCommander() && getGame().isEnemy(team, unit.getTeam())) {
                    commanders.add(unit);
                }
            }
            return commanders;
        }
    }

    private ObjectSet<Position> getEnemyCastlePositions() {
        synchronized (GameContext.RENDER_LOCK) {
            ObjectSet<Position> positions = new ObjectSet<Position>();
            for (Position position : getGame().getMap().getCastlePositions()) {
                Tile tile = getGame().getMap().getTile(position);
                if (!getGame().isAlly(team, tile.getTeam())) {
                    positions.add(position);
                }
            }
            return positions;
        }
    }

    private ObjectSet<Position> getEnemyVillagePositions() {
        synchronized (GameContext.RENDER_LOCK) {
            ObjectSet<Position> positions = new ObjectSet<Position>();
            for (Position position : getGame().getMap().getVillagePositions()) {
                Tile tile = getGame().getMap().getTile(position);
                if (!getGame().isAlly(team, tile.getTeam())) {
                    positions.add(position);
                }
            }
            return positions;
        }
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
        synchronized (GameContext.RENDER_LOCK) {
            ObjectSet<Position> reachable_positions = getManager().getPositionGenerator().createPositionsWithinReach(unit);
            for (Position position : reachable_positions) {
                if (!getGame().getMap().isTomb(position)) {
                    reachable_positions.remove(position);
                }
            }
            return reachable_positions;
        }
    }

    private ObjectSet<Unit> getEnemyUnits() {
        synchronized (GameContext.RENDER_LOCK) {
            ObjectSet<Unit> enemies = new ObjectSet<Unit>();
            for (Unit unit : getGame().getMap().getUnits()) {
                if (getGame().isEnemy(team, unit.getTeam())) {
                    enemies.add(unit);
                }
            }
            return enemies;
        }
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
