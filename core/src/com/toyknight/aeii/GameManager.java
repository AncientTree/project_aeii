package com.toyknight.aeii;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Step;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameManager implements AnimationDispatcher {

    public static final int STATE_SELECT = 0x1;
    public static final int STATE_MOVE = 0x2;
    public static final int STATE_RMOVE = 0x3;
    public static final int STATE_ACTION = 0x4;
    public static final int STATE_ATTACK = 0x5;
    public static final int STATE_SUMMON = 0x6;
    public static final int STATE_HEAL = 0x7;

    private GameCore game;
    private int state;
    private Unit selected_unit;
    private Point last_position;

    private int[][] move_mark_map;
    private ArrayList<Point> move_path;
    private HashSet<Point> movable_positions;
    private HashSet<Point> attackable_positions;

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    public GameManager() {
    }

    public void setGame(GameCore game) {
        this.game = game;
        this.state = STATE_SELECT;
    }

    public GameCore getGame() {
        return game;
    }

    public int getState() {
        return state;
    }

    public void selectUnit(int x, int y) {
        if(getState() == STATE_SELECT || getState() == STATE_MOVE) {
            Unit unit = getGame().getMap().getUnit(x, y);
            if (unit != null) {

            }
        }
    }

    public Unit getSelectedUnit() {
        return selected_unit;
    }

    public HashSet<Point> getMovablePositions() {
        return movable_positions;
    }

    public ArrayList<Point> getMovePath(int dest_x, int dest_y) {
        if (move_path == null || move_path.size() == 0) {
            createMovePath(dest_x, dest_y);
        } else {
            Point current_dest = move_path.get(move_path.size() - 1);
            if(dest_x != current_dest.x || dest_y != current_dest.y) {
                createMovePath(dest_x, dest_y);
            }
        }
        return move_path;
    }

    public HashSet<Point> getAttackablePositions() {
        return attackable_positions;
    }

    private void createMoveMarkMap() {
        int width = getGame().getMap().getWidth();
        int height = getGame().getMap().getHeight();
        move_mark_map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                move_mark_map[x][y] = Integer.MIN_VALUE;
            }
        }
    }

    public void createMovablePositions() {
        createMoveMarkMap();
        movable_positions = new HashSet();
        int unit_x = getSelectedUnit().getX();
        int unit_y = getSelectedUnit().getY();
        int movement_point = getSelectedUnit().getCurrentMovementPoint();
        Point start_position = new Point(unit_x, unit_y);
        Step start_step = new Step(start_position, movement_point);
        Queue<Step> start_steps = new LinkedList();
        start_steps.add(start_step);
        createMovablePisitions(start_steps);
    }

    private void createMovablePisitions(Queue<Step> current_steps) {
        Queue<Step> next_steps = new LinkedList();
        while (!current_steps.isEmpty()) {
            Step current_step = current_steps.poll();
            int step_x = current_step.getPosition().x;
            int step_y = current_step.getPosition().y;
            if (getGame().canUnitMove(getSelectedUnit(), step_x, step_y)) {
                movable_positions.add(current_step.getPosition());
            }
            for (int i = 0; i < 4; i++) {
                int next_x = current_step.getPosition().x + x_dir[i];
                int next_y = current_step.getPosition().y + y_dir[i];
                Point next = new Point(next_x, next_y);
                int current_mp = current_step.getMovementPoint();
                if (game.getMap().isWithinMap(next_x, next_y)) {
                    int mp_cost = UnitToolkit.getMovementPointCost(getSelectedUnit(), getGame().getMap().getTile(next_x, next_y));
                    if (current_mp - mp_cost > move_mark_map[next_x][next_y]) {
                        if (mp_cost <= current_mp) {
                            Unit target_unit = game.getMap().getUnit(next_x, next_y);
                            if (getGame().canMoveThrough(getSelectedUnit(), target_unit)) {
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

    public void createMovePath(int dest_x, int dest_y) {
        move_path = new ArrayList();
        int start_x = getSelectedUnit().getX();
        int start_y = getSelectedUnit().getY();
        if (start_x != dest_x || start_x != dest_y) {
            Point dest_position = new Point(dest_x, dest_y);
            if (movable_positions.contains(dest_position)) {
                int current_x = dest_x;
                int current_y = dest_y;
                createMovePath(current_x, current_y, start_x, start_y);
            }
        }
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

    private void createAttackablePositions(Unit unit) {
        int unit_x = unit.getX();
        int unit_y = unit.getY();
        int min_ar = unit.getMinAttackRange();
        int max_ar = unit.getMaxAttackRange();
        attackable_positions = new HashSet();
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
    }

}
