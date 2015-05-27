package com.toyknight.aeii;

import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.event.*;
import com.toyknight.aeii.listener.AnimationListener;
import com.toyknight.aeii.listener.EventDispatcherListener;
import com.toyknight.aeii.listener.GameManagerListener;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.*;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameManager implements AnimationDispatcher {

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
    private Unit selected_unit;
    private Point last_position;

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

    private void setState(int state) {
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

    private void beginPreviewPhase() {
        if (getSelectedUnit() != null) {
            createMovablePositions();
            setState(STATE_PREVIEW);
        }
    }

    public void cancelPreviewPhase() {
        if (state == STATE_PREVIEW) {
            setState(STATE_SELECT);
        }
    }

    private void beginMovePhase() {
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

    public void reverseMove() {
        Unit unit = getSelectedUnit();
        if (getGame().isUnitAccessible(unit) && getState() == STATE_ACTION) {
            submitGameEvent(new UnitMoveEvent(unit.getX(), unit.getY(), last_position.x, last_position.y, unit.getMovementPoint(), null));
            beginMovePhase();
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

    private void beginRemovePhase() {
        createMovablePositions();
        setState(STATE_REMOVE);
    }

    public void cancelActionPhase() {
        if (getState() == STATE_ATTACK || getState() == STATE_SUMMON || getState() == STATE_HEAL) {
            setState(STATE_ACTION);
        }
    }

    public void selectUnit(int x, int y) {
        if (getState() == STATE_SELECT || getState() == STATE_MOVE || getState() == STATE_PREVIEW) {
            Unit unit = getGame().getMap().getUnit(x, y);
            if (unit != null && !unit.isStandby()) {
                selected_unit = unit;
                if (unit.getTeam() == getGame().getCurrentTeam()) {
                    last_position = new Point(x, y);
                    beginMovePhase();
                } else {
                    beginPreviewPhase();
                }
            }
        }
    }

    public void moveSelectedUnit(int dest_x, int dest_y) {
        if (getMovablePositions().contains(getGame().getMap().getPosition(dest_x, dest_y))) {
            Unit unit = getSelectedUnit();
            if (unit != null && getGame().isUnitAccessible(unit) && (state == STATE_MOVE || state == STATE_REMOVE)) {
                int start_x = unit.getX();
                int start_y = unit.getY();
                if (canSelectedUnitMove(dest_x, dest_y)) {
                    int mp_remains = getMovementPointRemains(dest_x, dest_y);
                    ArrayList<Point> move_path = getMovePath(dest_x, dest_y);
                    submitGameEvent(new UnitMoveEvent(start_x, start_y, dest_x, dest_y, mp_remains, move_path));
                    switch (state) {
                        case STATE_MOVE:
                            setState(STATE_ACTION);
                            break;
                        case STATE_REMOVE:
                            submitGameEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
                            setState(STATE_SELECT);
                            break;
                    }
                }
            }
        } else {
            if (getState() == STATE_MOVE) {
                cancelMovePhase();
            }
        }
    }

    public void doAttack(int target_x, int target_y) {
        Unit attacker = getSelectedUnit();
        if (getState() == STATE_ATTACK && UnitToolkit.isWithinRange(attacker, target_x, target_y)) {
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            int kill_experience = getGame().getRule().getKillExperience();
            int attack_experience = getGame().getRule().getAttackExperience();
            int counter_experience = getGame().getRule().getCounterExperience();
            if (defender == null) {
                if (attacker.hasAbility(Ability.DESTROYER) && getGame().getMap().getTile(target_x, target_y).isDestroyable()) {
                    submitGameEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), target_x, target_y, -1, attack_experience));
                    submitGameEvent(new UnitStandbyEvent(attacker.getX(), attacker.getY()));
                    submitGameEvent(new TileDestroyEvent(target_x, target_y));
                    onActionFinished(attacker);
                }
            } else {
                if (getGame().isEnemy(attacker, defender)) {
                    //attack pre-calculation
                    attacker = UnitFactory.cloneUnit(attacker);
                    defender = UnitFactory.cloneUnit(defender);
                    int attack_damage = UnitToolkit.getDamage(attacker, defender, getGame().getMap());
                    UnitToolkit.attachAttackStatus(attacker, defender);
                    defender.changeCurrentHp(-attack_damage);
                    if (defender.getCurrentHp() > 0) {
                        attacker.gainExperience(attack_experience);
                        submitGameEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), attack_damage, attack_experience));
                        if (UnitToolkit.canCounter(defender, attacker)) {
                            int counter_damage = UnitToolkit.getDamage(defender, attacker, getGame().getMap());
                            attacker.changeCurrentHp(-counter_damage);
                            if (attacker.getCurrentHp() > 0) {
                                submitGameEvent(new UnitAttackEvent(defender.getX(), defender.getY(), attacker.getX(), attacker.getY(), counter_damage, counter_experience));
                            } else {
                                submitGameEvent(new UnitAttackEvent(defender.getX(), defender.getY(), attacker.getX(), attacker.getY(), counter_damage, kill_experience));
                            }
                        }
                    } else {
                        submitGameEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), attack_damage, kill_experience));
                    }
                    onActionFinished(attacker);
                }
            }
        }
    }

    public void doSummon(int target_x, int target_y) {
        Unit summoner = getSelectedUnit();
        if (getState() == STATE_SUMMON && UnitToolkit.isWithinRange(summoner, target_x, target_y)) {
            int experience = getGame().getRule().getAttackExperience();
            submitGameEvent(new SummonEvent(summoner.getX(), summoner.getY(), target_x, target_y, experience));
            onActionFinished(summoner);
        }
    }

    public void doRepair() {
        if (getState() == STATE_ACTION) {
            Unit unit = getSelectedUnit();
            submitGameEvent(new RepairEvent(unit.getX(), unit.getY()));
            onActionFinished(unit);
        }
    }

    public void doOccupy() {
        if (getState() == STATE_ACTION) {
            Unit unit = getSelectedUnit();
            submitGameEvent(new OccupyEvent(unit.getX(), unit.getY(), unit.getTeam()));
            onActionFinished(unit);
        }
    }

    private void onActionFinished(Unit unit) {
        if (UnitToolkit.canMoveAgain(unit)) {
            beginRemovePhase();
        } else {
            submitGameEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
            setState(STATE_SELECT);
        }
    }

    public void standbySelectedUnit() {
        if (getState() == STATE_ACTION) {
            Unit unit = getSelectedUnit();
            if (getGame().isUnitAccessible(unit)) {
                submitGameEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
                setState(STATE_SELECT);
            }
        }
    }

    public void endCurrentTurn() {
        if (getState() == STATE_SELECT || getState() == STATE_PREVIEW) {
            submitGameEvent(new TurnEndEvent());
            //calculate hp change at turn start
            int team = getGame().getCurrentTeam();
            HashSet<Point> unit_position_set = new HashSet(getGame().getMap().getUnitPositionSet());
            HashMap<Point, Integer> hp_change_map = new HashMap();
            Set<Point> unit_position_set_copy = new HashSet(unit_position_set);
            for (Point position : unit_position_set_copy) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.getTeam() == team) {
                    int change = 0;
                    //deal with terrain heal issues
                    change += UnitToolkit.getTerrainHeal(unit, getGame().getMap().getTile(unit.getX(), unit.getY()));
                    //deal with buff issues
                    if (unit.getStatus() != null && unit.getStatus().getType() == Status.POISONED) {
                        change -= getGame().getRule().getPoisonDamage();
                    }
                    hp_change_map.put(position, change);
                } else {
                    //remove other teams' unit position
                    unit_position_set.remove(position);
                }
            }
            //the healing aura
            for (Point position : unit_position_set) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.hasAbility(Ability.HEALING_AURA)) {
                    for (int x = unit.getX() - 1; x <= unit.getX() + 1; x++) {
                        for (int y = unit.getY() - 1; y <= unit.getY() + 1; y++) {
                            //not healer himself
                            if ((x != unit.getX() || y != unit.getY()) && getGame().getMap().isWithinMap(x, y)) {
                                Point target_position = getGame().getMap().getPosition(x, y);
                                //there's a unit at the position
                                if (unit_position_set.contains(target_position)) {
                                    //see if this unit already has hp change
                                    if (hp_change_map.keySet().contains(target_position)) {
                                        int change = hp_change_map.get(target_position) + 15;
                                        hp_change_map.put(target_position, change);
                                    } else {
                                        hp_change_map.put(target_position, 15);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            submitGameEvent(new HpChangeEvent(hp_change_map));
            // pre-calculate unit that will be destroyed
            for (Point position : hp_change_map.keySet()) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.getCurrentHp() + hp_change_map.get(position) <= 0) {
                    submitGameEvent(new UnitDestroyEvent(unit.getX(), unit.getY()));
                }
            }
            submitGameEvent(new UnitStatusUpdateEvent(team));
            setState(STATE_SELECT);
        }
    }

    public boolean canSelectedUnitMove(int dest_x, int dest_y) {
        Point dest = getGame().getMap().getPosition(dest_x, dest_y);
        return movable_positions.contains(dest) && getGame().canUnitMove(getSelectedUnit(), dest_x, dest_y);
    }

    public boolean canSelectUnitAct() {
        if (getSelectedUnit().hasAbility(Ability.SIEGE_MACHINE) && !getSelectedUnit().isAt(last_position.x, last_position.y)) {
            return false;
        } else {
            return true;
        }
    }

    public Unit getSelectedUnit() {
        return selected_unit;
    }

    private int getMovementPointRemains(int dest_x, int dest_y) {
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

    private void submitGameEvent(GameEvent event) {
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
            event.execute(getGame(), this);
            if (getGame().getCurrentPlayer() instanceof LocalPlayer) {

            } else {
                Point focus = event.getFocus();
                if (focus.x >= 0 && focus.y >= 0) {
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
                if (finish_flag == true) {
                    manager_listener.onScreenUpdateRequested();
                }
                dispatchGameEvents();
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
