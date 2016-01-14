package com.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Step;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 1/12/2016.
 */
public class MovementGenerator {

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    private final Array<Point> move_path;
    private final ObjectSet<Point> movable_positions;

    private GameCore game;

    private Unit current_unit;

    private int[][] move_mark_map;

    public MovementGenerator() {
        this.move_path = new Array<Point>();
        this.movable_positions = new ObjectSet<Point>();
    }

    public void setGame(GameCore game) {
        this.game = game;
        this.current_unit = null;
    }

    public GameCore getGame() {
        return game;
    }

    public Point getPosition(Unit unit) {
        return getGame().getMap().getPosition(unit.getX(), unit.getY());
    }

    private Queue<Step> createStartStep(Unit unit) {
        Step start_step = new Step(getPosition(unit), unit.getCurrentMovementPoint());
        Queue<Step> start_steps = new LinkedList<Step>();
        start_steps.add(start_step);
        return start_steps;
    }

    private void initializeMoveMarkMap() {
        int width = getGame().getMap().getWidth();
        int height = getGame().getMap().getHeight();
        move_mark_map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                move_mark_map[x][y] = Integer.MIN_VALUE;
            }
        }
    }

    public ObjectSet<Point> createMovablePositions(Unit unit) {
        current_unit = UnitFactory.cloneUnit(unit);
        movable_positions.clear();
        initializeMoveMarkMap();
        createMovablePositions(createStartStep(unit), unit);
        return movable_positions;
    }

    private void createMovablePositions(Queue<Step> current_steps, Unit unit) {
        Queue<Step> next_steps = new LinkedList<Step>();
        while (!current_steps.isEmpty()) {
            Step current_step = current_steps.poll();
            int step_x = current_step.getPosition().x;
            int step_y = current_step.getPosition().y;
            int current_mark = move_mark_map[step_x][step_y];
            int current_movement_point = current_step.getMovementPoint();
            if (current_movement_point > current_mark) {
                move_mark_map[step_x][step_y] = current_movement_point;
                if (getGame().canUnitMove(unit, step_x, step_y)) {
                    movable_positions.add(current_step.getPosition());
                }
            }
            for (int i = 0; i < 4; i++) {
                int next_x = current_step.getPosition().x + x_dir[i];
                int next_y = current_step.getPosition().y + y_dir[i];
                if (getGame().getMap().isWithinMap(next_x, next_y)) {
                    Point next = getGame().getMap().getPosition(next_x, next_y);
                    int movement_point_cost =
                            UnitToolkit.getMovementPointCost(unit, getGame().getMap().getTile(next_x, next_y));
                    int movement_point_left = current_movement_point - movement_point_cost;
                    if (movement_point_cost <= current_movement_point
                            && movement_point_left > move_mark_map[next_x][next_y]) {
                        Unit target_unit = game.getMap().getUnit(next_x, next_y);
                        if (getGame().canMoveThrough(unit, target_unit)) {
                            Step next_step = new Step(next, movement_point_left);
                            next_steps.add(next_step);
                        }
                    }
                }
            }
        }
        if (!next_steps.isEmpty()) {
            createMovablePositions(next_steps, unit);
        }
    }

    public Array<Point> createMovePath(Unit unit, int dest_x, int dest_y) {
        checkIdentity(unit);
        move_path.clear();
        int start_x = unit.getX();
        int start_y = unit.getY();
        if ((start_x != dest_x || start_y != dest_y) && move_mark_map[dest_x][dest_y] > Integer.MIN_VALUE) {
            createMovePath(dest_x, dest_y, start_x, start_y);
        }
        return move_path;
    }

    private void createMovePath(int current_x, int current_y, int start_x, int start_y) {
        move_path.insert(0, getGame().getMap().getPosition(current_x, current_y));
        if (current_x != start_x || current_y != start_y) {
            int next_x = 0;
            int next_y = 0;
            int next_mark = Integer.MIN_VALUE;
            for (int i = 0; i < 4; i++) {
                int temp_next_x = current_x + x_dir[i];
                int temp_next_y = current_y + y_dir[i];
                if (game.getMap().isWithinMap(temp_next_x, temp_next_y)) {
                    if (temp_next_x == start_x && temp_next_y == start_y) {
                        next_x = temp_next_x;
                        next_y = temp_next_y;
                        next_mark = Integer.MAX_VALUE;
                    } else {
                        int temp_next_mark = move_mark_map[temp_next_x][temp_next_y];
                        if (temp_next_mark > next_mark) {
                            next_x = temp_next_x;
                            next_y = temp_next_y;
                            next_mark = temp_next_mark;
                        }
                    }
                }
            }
            createMovePath(next_x, next_y, start_x, start_y);
        }
    }

    public int getMovementPointRemains(Unit unit, int dest_x, int dest_y) {
        checkIdentity(unit);
        Point dest_position = getGame().getMap().getPosition(dest_x, dest_y);
        if (movable_positions.contains(dest_position)) {
            return move_mark_map[dest_x][dest_y];
        } else {
            return -1;
        }
    }

    public Point getNextPositionToTarget(Unit unit, int target_x, int target_y) {
        current_unit = UnitFactory.cloneUnit(unit);
        initializeMoveMarkMap();
        searchPathToTarget(createStartStep(unit), unit, getGame().getMap().getPosition(target_x, target_y));
        createMovePath(unit, target_x, target_y);
        current_unit = null;
        if (move_path.size > 0) {
            for (int i = move_path.size - 1; i >= 0; i--) {
                int map_x = move_path.get(i).x;
                int map_y = move_path.get(i).y;
                if (move_mark_map[map_x][map_y] >= 0 && getGame().getMap().getUnit(map_x, map_y) == null) {
                    return move_path.get(i);
                }
            }
            return move_path.get(0);
        } else {
            return getGame().getMap().getPosition(target_x, target_y);
        }
    }

    private void searchPathToTarget(Queue<Step> current_steps, Unit unit, Point target) {
        Queue<Step> next_steps = new LinkedList<Step>();
        while (!current_steps.isEmpty()) {
            Step current_step = current_steps.poll();
            int step_x = current_step.getPosition().x;
            int step_y = current_step.getPosition().y;
            int current_mark = move_mark_map[step_x][step_y];
            int current_movement_point = current_step.getMovementPoint();
            if (current_movement_point > current_mark) {
                move_mark_map[step_x][step_y] = current_step.getMovementPoint();
            }
            if (current_step.getPosition().equals(target)) {
                return;
            } else {
                for (int i = 0; i < 4; i++) {
                    int next_x = current_step.getPosition().x + x_dir[i];
                    int next_y = current_step.getPosition().y + y_dir[i];
                    if (getGame().getMap().isWithinMap(next_x, next_y)) {
                        Point next_position = getGame().getMap().getPosition(next_x, next_y);
                        int movement_point_cost =
                                UnitToolkit.getMovementPointCost(unit, getGame().getMap().getTile(next_x, next_y));
                        int movement_point_left = current_movement_point - movement_point_cost;
                        if (movement_point_left > move_mark_map[next_x][next_y]) {
                            Unit target_unit = game.getMap().getUnit(next_x, next_y);
                            if (getGame().canMoveThrough(unit, target_unit)) {
                                Step next_step = new Step(next_position, movement_point_left);
                                next_steps.add(next_step);
                            }
                        }
                    }
                }
            }
        }
        if (!next_steps.isEmpty()) {
            searchPathToTarget(next_steps, unit, target);
        }
    }

    private void checkIdentity(Unit unit) {
        if (!UnitToolkit.isTheSameUnit(unit, current_unit)) {
            createMovablePositions(unit);
        }
    }

}
