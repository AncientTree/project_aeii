package com.toyknight.aeii.manager;

import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.listener.AnimationListener;
import com.toyknight.aeii.listener.EventDispatcherListener;
import com.toyknight.aeii.listener.GameManagerListener;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.*;

/**
 * Created by toyknight on 5/28/2015.
 */
public abstract class GameManager implements AnimationDispatcher {

    public static final int STATE_SELECT = 0x1;
    public static final int STATE_MOVE = 0x2;
    public static final int STATE_REMOVE = 0x3;
    public static final int STATE_ACTION = 0x4;
    public static final int STATE_ATTACK = 0x5;
    public static final int STATE_SUMMON = 0x6;
    public static final int STATE_HEAL = 0x7;
    public static final int STATE_PREVIEW = 0x8;

    private final Queue<GameEvent> event_queue;
    private final Queue<Animator> animation_queue;
    private Animator current_animation = null;

    private GameCore game;
    private GameManagerListener manager_listener;
    private final ArrayList<EventDispatcherListener> event_dispatcher_listeners;
    private final ArrayList<AnimationListener> animation_listeners;

    private int state;
    private int last_state;
    protected Unit selected_unit;
    protected Point last_position;

    private int[][] move_mark_map;
    private ArrayList<Point> move_path;
    private HashSet<Point> movable_positions;
    private HashSet<Point> attackable_positions;

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    public GameManager() {
        this.event_queue = new LinkedList();
        this.animation_queue = new LinkedList();
        this.event_dispatcher_listeners = new ArrayList();
        this.animation_listeners = new ArrayList();
    }

    public void setGame(GameCore game) {
        this.game = game;
        this.game.init();
        this.state = STATE_SELECT;
        this.event_dispatcher_listeners.clear();
        this.animation_listeners.clear();
    }

    public GameCore getGame() {
        return game;
    }

    public void setGameManagerListener(GameManagerListener listener) {
        this.manager_listener = listener;
    }

    protected void setState(int state) {
        if (state != this.state) {
            this.last_state = this.state;
            this.state = state;
            if (manager_listener != null) {
                manager_listener.onManagerStateChanged(last_state);
            }
        }
    }

    public int getState() {
        return state;
    }

    public void beginPreviewPhase() {
        if (getSelectedUnit() != null) {
            createMovablePositions();
            setState(STATE_PREVIEW);
        }
    }

    public void cancelPreviewPhase() {
        if (getState() == STATE_PREVIEW) {
            setState(STATE_SELECT);
        }
    }

    protected void beginMovePhase() {
        if (getGame().isUnitAccessible(getSelectedUnit())) {
            createMovablePositions();
            setState(STATE_MOVE);
        }
    }

    public void cancelMovePhase() {
        if (getState() == STATE_MOVE) {
            setState(STATE_SELECT);
        }
    }

    public void beginAttackPhase() {
        if (getGame().isUnitAccessible(getSelectedUnit()) && getState() == STATE_ACTION) {
            createAttackablePositions(getSelectedUnit());
            setState(STATE_ATTACK);
        }
    }

    public void beginSummonPhase() {
        if (getState() == STATE_ACTION) {
            createAttackablePositions(getSelectedUnit());
            setState(STATE_SUMMON);
        }
    }

    protected void beginRemovePhase() {
        createMovablePositions();
        setState(STATE_REMOVE);
    }

    public void cancelActionPhase() {
        if (getState() == STATE_ATTACK || getState() == STATE_SUMMON || getState() == STATE_HEAL) {
            setState(STATE_ACTION);
        }
    }

    public boolean isActionPhase() {
        return getState() == STATE_ATTACK || getState() == STATE_SUMMON || getState() == STATE_HEAL || getState() == STATE_ACTION;
    }

    abstract public void selectUnit(int x, int y);

    abstract public void moveSelectedUnit(int dest_x, int dest_y);

    abstract public void reverseMove();

    abstract public void doAttack(int target_x, int target_y);

    abstract public void doSummon(int target_x, int target_y);

    abstract public void doRepair();

    abstract public void doOccupy();

    abstract public void buyUnit(String package_name, int index, int x, int y);

    abstract public void standbySelectedUnit();

    abstract public void endCurrentTurn();

    protected void submitGameEvent(GameEvent event) {
        if (isAnimating()) {
            event_queue.add(event);
        } else {
            executeGameEvent(event);
        }
    }

    private void dispatchGameEvents() {
        while (!isAnimating() && !event_queue.isEmpty()) {
            executeGameEvent(event_queue.poll());
        }
    }

    public void executeGameEvent(GameEvent event) {
        if (event.canExecute(getGame())) {
            event.execute(this);
            if (getGame().getCurrentPlayer() instanceof LocalPlayer) {

            } else {
                Point focus = event.getFocus();
                if (focus != null) {
                    manager_listener.onMapFocusRequired(focus.x, focus.y);
                }
            }
            //more process with this event
        }
    }

    @Override
    public void addAnimationListener(AnimationListener listener) {
        this.animation_listeners.add(listener);
    }

    @Override
    public void submitAnimation(Animator animation) {
        if (current_animation == null) {
            current_animation = animation;
        } else {
            this.animation_queue.add(animation);
        }
    }

    @Override
    public void updateAnimation(float delta) {
        if (current_animation == null || current_animation.isAnimationFinished()) {
            boolean finish_flag = false;
            if (current_animation != null) {
                for (AnimationListener listener : animation_listeners) {
                    listener.animationCompleted(current_animation);
                }
                finish_flag = true;
            }
            current_animation = animation_queue.poll();
            if (current_animation != null) {
                for (AnimationListener listener : animation_listeners) {
                    listener.animationStarted(current_animation);
                }
            } else {
                dispatchGameEvents();
                if (finish_flag == true && current_animation == null) {
                    manager_listener.onButtonUpdateRequested();
                }
            }
        } else {
            current_animation.update(delta);
        }
    }

    @Override
    public Animator getCurrentAnimation() {
        return current_animation;
    }

    @Override
    public boolean isAnimating() {
        return getCurrentAnimation() != null || !animation_queue.isEmpty();
    }

    private void createMoveMarkMap() {
        this.move_path = null;
        this.movable_positions = null;
        int width = getGame().getMap().getWidth();
        int height = getGame().getMap().getHeight();
        move_mark_map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                move_mark_map[x][y] = Integer.MIN_VALUE;
            }
        }
    }

    private void createMovablePositions() {
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

    public Unit getSelectedUnit() {
        return selected_unit;
    }

    public int getMovementPointRemains(int dest_x, int dest_y) {
        Point dest_position = new Point(dest_x, dest_y);
        if (movable_positions.contains(dest_position)) {
            return move_mark_map[dest_x][dest_y];
        } else {
            return -1;
        }
    }

    public HashSet<Point> getMovablePositions() {
        return movable_positions;
    }

    public HashSet<Point> getAttackablePositions() {
        return attackable_positions;
    }

    public ArrayList<Point> getMovePath(int dest_x, int dest_y) {
        if (move_path == null || move_path.size() == 0) {
            createMovePath(dest_x, dest_y);
        } else {
            Point current_dest = move_path.get(move_path.size() - 1);
            if (dest_x != current_dest.x || dest_y != current_dest.y) {
                createMovePath(dest_x, dest_y);
            }
        }
        return move_path;
    }

    private void createMovePath(int dest_x, int dest_y) {
        move_path = new ArrayList();
        int start_x = getSelectedUnit().getX();
        int start_y = getSelectedUnit().getY();
        if (start_x != dest_x || start_y != dest_y) {
            Point dest_position = getGame().getMap().getPosition(dest_x, dest_y);
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
