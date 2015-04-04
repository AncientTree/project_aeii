package com.toyknight.aeii.utils;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.Map;

import java.util.*;

/**
 * Created by toyknight on 4/3/2015.
 */
public class UnitToolkit {

    private static final Random random = new Random(System.currentTimeMillis());

    private int dest_x;
    private int dest_y;
    private Unit current_unit;

    private final BasicGame game;

    private int[][] move_mark_map;
    private ArrayList<Point> move_path;
    private HashSet<Point> movable_positions;
    //private HashSet<Point> attackable_positions;

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    public UnitToolkit(BasicGame game) {
        this.game = game;
    }

    public void setCurrentUnit(Unit unit) {
        this.current_unit = unit;
        this.dest_x = -1;
        this.dest_y = -1;
        this.move_path = new ArrayList();
        this.movable_positions = null;
        //this.attackable_positions = null;
    }

    private void createMoveMarkMap() {
        int width = game.getMap().getWidth();
        int height = game.getMap().getHeight();
        move_mark_map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                move_mark_map[x][y] = Integer.MIN_VALUE;
            }
        }
    }

    public ArrayList<Point> createMovablePositions() {
        createMoveMarkMap();
        movable_positions = new HashSet();
        int unit_x = current_unit.getX();
        int unit_y = current_unit.getY();
        int movement_point = current_unit.getCurrentMovementPoint();
        Point start_position = new Point(unit_x, unit_y);
        Step start_step = new Step(start_position, movement_point);
        Queue<Step> start_steps = new LinkedList();
        start_steps.add(start_step);
        createMovablePisitions(start_steps);
        return new ArrayList(movable_positions);
    }

    private void createMovablePisitions(Queue<Step> current_steps) {
        Queue<Step> next_steps = new LinkedList();
        while (!current_steps.isEmpty()) {
            Step current_step = current_steps.poll();
            int step_x = current_step.getPosition().x;
            int step_y = current_step.getPosition().y;
            if (canUnitMove(current_unit, step_x, step_y)) {
                movable_positions.add(current_step.getPosition());
            }
            for (int i = 0; i < 4; i++) {
                int next_x = current_step.getPosition().x + x_dir[i];
                int next_y = current_step.getPosition().y + y_dir[i];
                Point next = new Point(next_x, next_y);
                int current_mp = current_step.getMp();
                if (game.getMap().isWithinMap(next_x, next_y)) {
                    int mp_cost = getMovementPointCost(current_unit, next_x, next_y);
                    if (current_mp - mp_cost > move_mark_map[next_x][next_y]) {
                        if (mp_cost <= current_mp) {
                            Unit target_unit = game.getMap().getUnit(next_x, next_y);
                            if (canMoveThrough(current_unit, target_unit)) {
                                Step next_step = new Step(next, current_mp - mp_cost);
                                move_mark_map[next_x][next_y] = current_mp - mp_cost;
                                next_steps.add(next_step);
                            }
                        }
                    }
                }
            }
        }
        if (!next_steps.isEmpty()) {
            createMovablePisitions(next_steps);
        }
    }

    public ArrayList<Point> createMovePath(Unit unit, int start_x, int start_y, int dest_x, int dest_y) {
        if (!isTheSameUnit(current_unit, unit)) {
            setCurrentUnit(unit);
            createMovablePositions();
        }
        if (this.dest_x != dest_x || this.dest_y != dest_y) {
            Point dest_position = new Point(dest_x, dest_y);
            if (movable_positions.contains(dest_position)) {
                move_path = new ArrayList();
                int current_x = dest_x;
                int current_y = dest_y;
                createMovePath(current_x, current_y, start_x, start_y);
            } else {
                move_path = new ArrayList();
            }
        }
        return move_path;
    }

    private void createMovePath(int current_x, int current_y, int start_x, int start_y) {
        move_path.add(0, new Point(current_x, current_y));
        if (current_x != start_x || current_y != start_y) {
            int next_x = 0;
            int next_y = 0;
            int next_mark = Integer.MIN_VALUE;
            for (int i = 0; i < 4; i++) {
                int tmp_next_x = current_x + x_dir[i];
                int tmp_next_y = current_y + y_dir[i];
                if (game.getMap().isWithinMap(tmp_next_x, tmp_next_y)) {
                    if (tmp_next_x == start_x && tmp_next_y == start_y) {
                        next_x = tmp_next_x;
                        next_y = tmp_next_y;
                        next_mark = Integer.MAX_VALUE;
                    } else {
                        int tmp_next_mark = move_mark_map[tmp_next_x][tmp_next_y];
                        if (tmp_next_mark > next_mark) {
                            next_x = tmp_next_x;
                            next_y = tmp_next_y;
                            next_mark = tmp_next_mark;
                        }
                    }
                }
            }
            createMovePath(next_x, next_y, start_x, start_y);
        }
    }

    public int getMovementPointRemains(Unit unit, int dest_x, int dest_y) {
        if (!isTheSameUnit(current_unit, unit)) {
            setCurrentUnit(unit);
            createMovablePositions();
        }
        Point dest_position = new Point(dest_x, dest_y);
        if (movable_positions.contains(dest_position)) {
            return move_mark_map[dest_x][dest_y];
        } else {
            return -1;
        }
    }

    public int getMovementPointCost(Unit unit, int x, int y) {
        Tile tile = game.getMap().getTile(x, y);
        int mp_cost = tile.getStepCost();
        int tile_type = tile.getType();
        if (unit.hasAbility(Ability.AIR_FORCE)) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.CRAWLER)
                && (tile_type == Tile.TYPE_LAND || tile_type == Tile.TYPE_FOREST || tile_type == Tile.TYPE_MOUNTAIN)) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.FIGHTER_OF_THE_SEA) && tile_type == Tile.TYPE_WATER) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.FIGHTER_OF_THE_FOREST) && tile_type == Tile.TYPE_FOREST) {
            mp_cost = 1;
        }
        if (unit.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN) && tile_type == Tile.TYPE_MOUNTAIN) {
            mp_cost = 1;
        }
        if (mp_cost < 1) {
            mp_cost = 1;
        }
        return mp_cost;
    }

    public ArrayList<Point> createAttackablePositions(Unit unit) {
        int unit_x = unit.getX();
        int unit_y = unit.getY();
        int min_ar = unit.getMinAttackRange();
        int max_ar = unit.getMaxAttackRange();
        HashSet<Point> attackable_positions = new HashSet();
        for (int ar = min_ar; ar <= max_ar; ar++) {
            for (int dx = -ar; dx <= ar; dx++) {
                int dy = dx >= 0 ? ar - dx : -ar - dx;
                if (game.getMap().isWithinMap(unit_x + dx, unit_y + dy)) {
                    attackable_positions.add(new Point(unit_x + dx, unit_y + dy));
                }
                if (dy != 0) {
                    if (game.getMap().isWithinMap(unit_x + dx, unit_y - dy)) {
                        attackable_positions.add(new Point(unit_x + dx, unit_y - dy));
                    }
                }
            }
        }
        return new ArrayList(attackable_positions);
    }

    private boolean canMoveThrough(Unit unit, Unit target_unit) {
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

    public boolean canUnitMove(Unit unit, int dest_x, int dest_y) {
        if (game.getMap().canMove(dest_x, dest_y)) {
            Unit dest_unit = game.getMap().getUnit(dest_x, dest_y);
            if (dest_unit == null) {
                return true;
            } else {
                return isTheSameUnit(unit, dest_unit);
            }
        } else {
            return false;
        }
    }

    public boolean isUnitAccessible(Unit unit) {
        return unit != null
                && unit.getTeam() == game.getCurrentTeam()
                && unit.getCurrentHp() > 0
                && !unit.isStandby();
    }

    public boolean isEnemy(Unit unit, Unit target_unit) {
        if (unit != null && target_unit != null) {
            return game.isEnemy(unit.getTeam(), target_unit.getTeam());
        } else {
            return false;
        }
    }

    public boolean isAlly(Unit unit, Unit target_unit) {
        if (unit != null && target_unit != null) {
            return !isEnemy(unit, target_unit);
        } else {
            return false;
        }
    }

    public static boolean isTheSameUnit(Unit unit_a, Unit unit_b) {
        if (unit_a == null || unit_b == null) {
            return false;
        } else {
            if (unit_a.isAt(unit_b.getX(), unit_b.getY())) {
                return unit_a.getUnitCode().equals(unit_b.getUnitCode());
            } else {
                return false;
            }
        }
    }

    public static boolean isWithinRange(Unit unit, int target_x, int target_y) {
        int range = Math.abs(target_x - unit.getX()) + Math.abs(target_y - unit.getY());
        return unit.getMinAttackRange() <= range && range <= unit.getMaxAttackRange();
    }

    public static boolean canCounter(Unit counter, Unit attacker) {
        return Math.abs(attacker.getX() - counter.getX())
                + Math.abs(attacker.getY() - counter.getY()) == 1
                && isWithinRange(counter, attacker.getX(), attacker.getY());
    }

    public static int getDefenceBonus(Unit unit, int tile_index) {
        int defence_bonus = 0;
        Tile tile = TileFactory.getTile(tile_index);
        if (!unit.hasAbility(Ability.AIR_FORCE)) {
            defence_bonus += tile.getDefenceBonus();
        }
        switch (tile.getType()) {
            case Tile.TYPE_FOREST:
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_FOREST)) {
                    defence_bonus += 10;
                }
            case Tile.TYPE_MOUNTAIN:
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN)) {
                    defence_bonus += 10;
                }
            case Tile.TYPE_WATER:
                if (unit.hasAbility(Ability.FIGHTER_OF_THE_SEA)) {
                    defence_bonus += 10;
                }
            default:
                //do nothing
        }
        return defence_bonus;
    }

    public static int getAttackBonus(Unit attacker, Unit defender, int tile_index) {
        int bonus = 0;
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_MOUNTAIN)
                && TileFactory.getTile(tile_index).getType() == Tile.TYPE_MOUNTAIN) {
            bonus += 10;
        }
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_FOREST)
                && TileFactory.getTile(tile_index).getType() == Tile.TYPE_FOREST) {
            bonus += 10;
        }
        if (attacker.hasAbility(Ability.FIGHTER_OF_THE_SEA)
                && TileFactory.getTile(tile_index).getType() == Tile.TYPE_WATER) {
            bonus += 10;
        }
        if (attacker.hasAbility(Ability.MARKSMAN) && defender.hasAbility(Ability.AIR_FORCE)) {
            bonus += 10;
        }
        return bonus;
    }

    public static int getDamage(Unit attacker, Unit defender, Map map) {
        int attacker_tile_index = map.getTileIndex(attacker.getX(), attacker.getY());
        int defender_tile_index = map.getTileIndex(defender.getX(), defender.getY());
        int attack_bonus = getAttackBonus(attacker, defender, attacker_tile_index);
        int attack = attacker.getAttack() + attack_bonus;
        int defence_bonus = getDefenceBonus(defender, defender_tile_index);
        int defence = attacker.getAttackType() == Unit.ATTACK_PHYSICAL
                ? defender.getPhysicalDefence() : defender.getMagicalDefence();
        defence += defence_bonus;
        int damage = attack > defence ? attack - defence : 0;
        int attacker_hp = attacker.getCurrentHp();
        int attacker_max_hp = attacker.getMaxHp();
        int offset = random.nextInt(5) - 2;
        damage = damage * attacker_hp / attacker_max_hp + offset;
        damage = damage > 0 ? damage : 0;
        //more process
        return damage;
    }

    public static boolean canMoveAgain(Unit unit) {
        return unit.getCurrentHp() > 0
                && unit.getCurrentMovementPoint() > 0
                && unit.hasAbility(Ability.CHARGER);
    }

    private class Step {

        private final int mp;
        private final Point position;

        public Step(Point position, int mp) {
            this.mp = mp;
            this.position = position;
        }

        public Point getPosition() {
            return position;
        }

        public int getMp() {
            return mp;
        }

    }

}
