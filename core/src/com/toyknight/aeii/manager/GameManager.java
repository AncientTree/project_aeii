package com.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.AnimationDispatcher;
import com.toyknight.aeii.animator.*;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.listener.AnimationListener;
import com.toyknight.aeii.listener.GameManagerListener;
import com.toyknight.aeii.manager.events.*;
import com.toyknight.aeii.utils.Recorder;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.*;

/**
 * @author toyknight  5/28/2015.
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
    public static final int STATE_BUY = 0x9;

    private boolean is_server_manager;

    private final Queue<GameEvent> event_queue;
    private final Queue<Animator> animation_queue;
    private Animator current_animation = null;

    private GameCore game;
    private UnitToolkit unit_toolkit;
    private GameManagerListener manager_listener;
    private final Array<AnimationListener> animation_listeners;

    private int state;
    protected Unit selected_unit;
    protected Point last_position;

    private int[][] move_mark_map;
    private Array<Point> move_path;
    private final ObjectSet<Point> movable_positions;
    private ObjectSet<Point> attackable_positions;

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    public GameManager() {
        this.is_server_manager = false;
        this.event_queue = new LinkedList<GameEvent>();
        this.animation_queue = new LinkedList<Animator>();
        this.animation_listeners = new Array<AnimationListener>();
        this.movable_positions = new ObjectSet<Point>();
    }

    public void setGame(GameCore game) {
        this.game = game;
        this.unit_toolkit = new UnitToolkit(game);
        this.state = STATE_SELECT;
        this.event_queue.clear();
        this.animation_queue.clear();
        this.current_animation = null;
        this.animation_listeners.clear();
    }

    public GameCore getGame() {
        return game;
    }

    public UnitToolkit getUnitToolkit() {
        return unit_toolkit;
    }

    public void setGameManagerListener(GameManagerListener listener) {
        this.manager_listener = listener;
    }

    public GameManagerListener getListener() {
        return manager_listener;
    }

    public void setServerManager(boolean b) {
        is_server_manager = b;
    }

    public boolean isProcessing() {
        return isAnimating() || !event_queue.isEmpty();
    }

    public void setState(int state) {
        if (state != this.state) {
            this.state = state;
            if (manager_listener != null) {
                manager_listener.onManagerStateChanged();
            }
        }
    }

    public int getState() {
        return state;
    }

    public void setSelectedUnit(Unit unit) {
        this.selected_unit = unit;
        this.movable_positions.clear();
        setLastPosition(new Point(unit.getX(), unit.getY()));
    }

    public void setLastPosition(Point position) {
        this.last_position = position;
    }

    public Point getLastPosition() {
        return last_position;
    }

    public void beginPreviewPhase(Unit target) {
        this.selected_unit = target;
        createMovablePositions();
        setState(STATE_PREVIEW);
    }

    public void cancelPreviewPhase() {
        setState(STATE_SELECT);
    }

    public void beginMovePhase() {
        createMovablePositions();
        setState(STATE_MOVE);
    }

    public void cancelMovePhase() {
        setState(STATE_SELECT);
    }

    public void beginAttackPhase() {
        setState(STATE_ATTACK);
        attackable_positions = createAttackablePositions(getSelectedUnit());
    }

    public void beginSummonPhase() {
        setState(STATE_SUMMON);
        attackable_positions = createAttackablePositions(getSelectedUnit());
    }

    public void beginHealPhase() {
        setState(STATE_HEAL);
        attackable_positions = createAttackablePositions(getSelectedUnit());
    }

    public void beginRemovePhase() {
        createMovablePositions();
        setState(STATE_REMOVE);
    }

    public void cancelActionPhase() {
        setState(STATE_ACTION);
    }

    public void doSelect(int x, int y) {
        Unit unit = getGame().getMap().getUnit(x, y);
        if (getGame().isUnitAccessible(unit)) {
            dispatchEvent(new UnitSelectEvent(x, y));
        }
    }

    public void doMoveUnit(int dest_x, int dest_y) {
        if (canSelectedUnitMove(dest_x, dest_y)) {
            int start_x = getSelectedUnit().getX();
            int start_y = getSelectedUnit().getY();
            int mp_remains = getMovementPointRemains(dest_x, dest_y);
            Array<Point> move_path = getMovePath(dest_x, dest_y);
            dispatchEvent(new UnitMoveEvent(start_x, start_y, dest_x, dest_y, mp_remains, move_path));
        }
    }

    public void doReverseMove() {
        Point last_position = getLastPosition();
        Unit selected_unit = getSelectedUnit();
        dispatchEvent(
                new UnitMoveReverseEvent(selected_unit.getX(), selected_unit.getY(), last_position.x, last_position.y));

    }

    public void doAttack(int target_x, int target_y) {
        Unit attacker = getSelectedUnit();
        if (UnitToolkit.isWithinRange(attacker, target_x, target_y)) {
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            int kill_experience = getGame().getRule().getKillExperience();
            int attack_experience = getGame().getRule().getAttackExperience();
            int counter_experience = getGame().getRule().getCounterExperience();
            if (defender == null) {
                if (attacker.hasAbility(Ability.DESTROYER) && getGame().getMap().getTile(target_x, target_y).isDestroyable()) {
                    dispatchEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), target_x, target_y, -1, attack_experience));
                    dispatchEvent(new UnitStandbyEvent(attacker.getX(), attacker.getY()));
                    dispatchEvent(new TileDestroyEvent(target_x, target_y));
                }
            } else {
                if (getGame().canAttack(attacker, target_x, target_y)) {
                    //attack pre-calculation
                    Unit real_attacker = UnitFactory.cloneUnit(UnitToolkit.getAttacker(attacker, defender));
                    Unit real_defender = UnitFactory.cloneUnit(UnitToolkit.getDefender(attacker, defender));
                    int attack_damage = getUnitToolkit().getDamage(real_attacker, real_defender, getGame().getMap());
                    UnitToolkit.attachAttackStatus(real_attacker, real_defender);
                    real_defender.changeCurrentHp(-attack_damage);
                    if (real_defender.getCurrentHp() > 0) {
                        real_attacker.gainExperience(attack_experience);
                        dispatchEvent(new UnitAttackEvent(real_attacker.getX(), real_attacker.getY(), real_defender.getX(), real_defender.getY(), attack_damage, attack_experience));
                        if (getUnitToolkit().canCounter(real_defender, real_attacker) || UnitToolkit.isAttackAmbushed(attacker, defender)) {
                            int counter_damage = getUnitToolkit().getDamage(real_defender, real_attacker, getGame().getMap());
                            real_attacker.changeCurrentHp(-counter_damage);
                            if (real_attacker.getCurrentHp() > 0) {
                                dispatchEvent(new UnitAttackEvent(real_defender.getX(), real_defender.getY(), real_attacker.getX(), real_attacker.getY(), counter_damage, counter_experience));
                            } else {
                                dispatchEvent(new UnitAttackEvent(real_defender.getX(), real_defender.getY(), real_attacker.getX(), real_attacker.getY(), counter_damage, kill_experience));
                            }
                        }
                    } else {
                        dispatchEvent(new UnitAttackEvent(real_attacker.getX(), real_attacker.getY(), real_defender.getX(), real_defender.getY(), attack_damage, kill_experience));
                    }
                }
            }
        }
    }

    public void doSummon(int target_x, int target_y) {
        Unit summoner = getSelectedUnit();
        if (getGame().canSummon(summoner, target_x, target_y)) {
            int experience = getGame().getRule().getAttackExperience();
            dispatchEvent(new SummonEvent(summoner.getX(), summoner.getY(), target_x, target_y, experience));
        }
    }

    public void doHeal(int target_x, int target_y) {
        Unit healer = getSelectedUnit();
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canHeal(healer, target_x, target_y)) {
            int heal = UnitToolkit.getHeal(healer, target);
            int experience = target.getCurrentHp() + heal > 0 ?
                    getGame().getRule().getAttackExperience() : getGame().getRule().getKillExperience();
            dispatchEvent(new UnitHealEvent(healer.getX(), healer.getY(), target_x, target_y, heal, experience));
        }
    }

    public void doRepair() {
        Unit unit = getSelectedUnit();
        dispatchEvent(new RepairEvent(unit.getX(), unit.getY()));
    }

    public void doOccupy() {
        Unit unit = getSelectedUnit();
        dispatchEvent(new OccupyEvent(unit.getX(), unit.getY(), unit.getTeam()));
    }

    public void doBuyUnit(int index, int x, int y) {
        int team = getGame().getCurrentTeam();
        dispatchEvent(new UnitBuyEvent(index, team, x, y, getGame().getUnitPrice(index, team)));
    }

    public void doStandbyUnit() {
        Unit unit = getSelectedUnit();
        if (getGame().isUnitAccessible(unit)) {
            dispatchEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
        }
    }

    public void doEndTurn() {
        int next_team = getGame().getNextTeam();

        dispatchEvent(new TurnEndEvent());

        dispatchEvent(new UnitStatusUpdateEvent(next_team));

        //calculate hp change at turn start
        ObjectMap<Point, Integer> hp_change_map = new ObjectMap<Point, Integer>();

        //terrain and poison hp change
        for (Point position : getGame().getMap().getUnitPositionSet()) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == next_team) {
                //the terrain heal
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                int change = getUnitToolkit().getTerrainHeal(unit, tile);
                //the poison damage
                if (unit.hasStatus(Status.POISONED) && unit.getStatus().getRemainingTurn() > 0) {
                    if (unit.hasAbility(Ability.UNDEAD)) {
                        change += getGame().getRule().getPoisonDamage();
                    } else {
                        change = -getGame().getRule().getPoisonDamage();
                    }
                }
                if (unit.hasAbility(Ability.REHABILITATION)) {
                    change += unit.getMaxHp() / 4;
                }
                hp_change_map.put(position, change);
            } else {
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                if (getGame().isEnemy(unit.getTeam(), next_team) && tile.isCastle() && tile.getTeam() == next_team) {
                    hp_change_map.put(position, -50);
                }
            }
        }

        dispatchEvent(new HpChangeEvent(hp_change_map));

        // pre-calculate unit that will be destroyed
        for (Point position : hp_change_map.keys()) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit != null && unit.getCurrentHp() + hp_change_map.get(position) <= 0) {
                dispatchEvent(new UnitDestroyEvent(unit.getX(), unit.getY()));
            }
        }
    }

    public void onUnitMoveFinished() {
        switch (getState()) {
            case GameManager.STATE_MOVE:
                setState(GameManager.STATE_ACTION);
                break;
            case GameManager.STATE_REMOVE:
                doStandbyUnit();
                break;
        }
    }

    public void onUnitActionFinished(Unit unit) {
        if (unit == null || unit.getCurrentHp() <= 0) {
            setState(GameManager.STATE_SELECT);
        } else {
            if (UnitToolkit.canMoveAgain(unit)) {
                setLastPosition(new Point(unit.getX(), unit.getY()));
                beginRemovePhase();
            } else {
                doStandbyUnit();
            }
        }
    }

    public void onUnitStandby(Unit unit) {
        //all the status auras
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Unit target = game.getMap().getUnit(unit.getX() + i, unit.getY() + j);
                if (target != null) {
                    if (unit.hasAbility(Ability.ATTACK_AURA) && !game.isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.INSPIRED, 0));
                    }
                    if (unit.hasAbility(Ability.SLOWING_AURA)
                            && !target.hasAbility(Ability.SLOWING_AURA) && game.isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.SLOWED, 1));
                    }
                }
            }
        }
        //the refresh aura
        ObjectMap<Point, Integer> hp_change_map = new ObjectMap<Point, Integer>();
        if (unit.hasAbility(Ability.REFRESH_AURA)) {
            int heal = 10 + unit.getLevel() * 5;
            ObjectSet<Point> attackable_positions = createAttackablePositions(unit);
            attackable_positions.add(game.getMap().getPosition(unit.getX(), unit.getY()));
            for (Point target_position : attackable_positions) {
                Unit target = game.getMap().getUnit(target_position.x, target_position.y);
                if (target != null && !game.isEnemy(unit, target) && target.hasStatus(Status.POISONED)) {
                    target.clearStatus();
                }
                if (game.canHeal(unit, target)) {
                    hp_change_map.put(target_position, heal);
                }

            }
        }
        //deal with tombs
        if (game.getMap().isTomb(unit.getX(), unit.getY())) {
            game.getMap().removeTomb(unit.getX(), unit.getY());
            if (!unit.hasAbility(Ability.HEAVY_MACHINE) && !unit.hasAbility(Ability.NECROMANCER)) {
                unit.attachStatus(new Status(Status.POISONED, 3));
            }
        }
        //validate hp change
        if (unit.getCurrentHp() > unit.getMaxHp()) {
            Point position = game.getMap().getPosition(unit.getX(), unit.getY());
            hp_change_map.put(position, unit.getMaxHp() - unit.getCurrentHp());
        }
        executeGameEvent(new HpChangeEvent(hp_change_map), false);
        setState(GameManager.STATE_SELECT);
    }

    public void dispatchEvent(GameEvent event) {
        if (manager_listener != null) {
            manager_listener.onGameEventDispatched(event);
        }
        submitGameEvent(event);
    }

    public void submitGameEvent(GameEvent event) {
        event_queue.add(event);
    }

    private void dispatchGameEvents() {
        while (!isAnimating() && !event_queue.isEmpty()) {
            executeGameEvent(event_queue.poll());
        }
    }

    public void executeGameEvent(GameEvent event) {
        executeGameEvent(event, true);
    }

    public void executeGameEvent(GameEvent event, boolean record) {
        if (event.canExecute(getGame())) {
            event.execute(this);
            Point focus = event.getFocus(getGame());
            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                if (focus != null && manager_listener != null && event instanceof TurnEndEvent) {
                    manager_listener.onMapFocusRequired(focus.x, focus.y);
                }
            } else {
                if (focus != null && manager_listener != null) {
                    manager_listener.onMapFocusRequired(focus.x, focus.y);
                }
            }
            if (record) {
                Recorder.submitGameEvent(event);
            }
        }
    }

    public void submitHpChangeAnimation(ObjectMap<Point, Integer> change_map, ObjectSet<Unit> units) {
        if (canSubmitAnimation()) {
            submitAnimation(new HpChangeAnimator(change_map, units));
        }
    }

    public void submitHpChangeAnimation(Unit unit, int change) {
        if (canSubmitAnimation()) {
            submitAnimation(new HpChangeAnimator(unit, change));
        }
    }

    public void submitMessageAnimation(String message, float delay) {
        if (canSubmitAnimation()) {
            submitAnimation(new MessageAnimator(message, delay));
        }
    }

    public void submitMessageAnimation(String message_upper, String message_lower, float delay) {
        if (canSubmitAnimation()) {
            submitAnimation(new MessageAnimator(message_upper, message_lower, delay));
        }
    }

    public void submitSummonAnimation(Unit summoner, int target_x, int target_y) {
        if (canSubmitAnimation()) {
            submitAnimation(new SummonAnimator(summoner, target_x, target_y));
        }
    }

    public void submitUnitLevelUpAnimation(Unit unit) {
        if (canSubmitAnimation()) {
            submitAnimation(new UnitLevelUpAnimator(unit));
        }
    }

    public void submitDustAriseAnimation(int map_x, int map_y) {
        if (canSubmitAnimation()) {
            submitAnimation(new DustAriseAnimator(map_x, map_y));
        }
    }

    public void submitUnitAttackAnimation(Unit attacker, Unit target, int damage) {
        if (canSubmitAnimation()) {
            submitAnimation(new UnitAttackAnimator(attacker, target, damage));
        }
    }

    public void submitUnitAttackAnimation(Unit attacker, int target_x, int target_y) {
        if (canSubmitAnimation()) {
            submitAnimation(new UnitAttackAnimator(attacker, target_x, target_y));
        }
    }

    public void submitUnitDestroyAnimation(Unit unit) {
        if (canSubmitAnimation()) {
            submitAnimation(new UnitDestroyAnimator(unit));
        }
    }

    public void submitUnitMoveAnimation(Unit unit, Array<Point> path) {
        if (canSubmitAnimation()) {
            submitAnimation(new UnitMoveAnimator(unit, path));
        }
    }

    @Override
    public boolean canSubmitAnimation() {
        return !is_server_manager;
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
                if (getGame().isGameOver() && animation_queue.isEmpty()) {
                    manager_listener.onGameOver();
                }
                finish_flag = true;
            }
            current_animation = animation_queue.poll();
            if (current_animation == null) {
                dispatchGameEvents();
                if (finish_flag) {
                    manager_listener.onButtonUpdateRequested();
                }
            } else {
                for (AnimationListener listener : animation_listeners) {
                    listener.animationStarted(current_animation);
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
        createMovablePositions(getSelectedUnit());
    }

    public ObjectSet<Point> createMovablePositions(Unit unit) {
        createMoveMarkMap();
        move_path = null;
        movable_positions.clear();
        int unit_x = unit.getX();
        int unit_y = unit.getY();
        int movement_point = unit.getCurrentMovementPoint();
        Point start_position = new Point(unit_x, unit_y);
        Step start_step = new Step(start_position, movement_point);
        Queue<Step> start_steps = new LinkedList<Step>();
        start_steps.add(start_step);
        createMovablePositions(start_steps, unit);
        return movable_positions;
    }

    private void createMovablePositions(Queue<Step> current_steps, Unit unit) {
        Queue<Step> next_steps = new LinkedList<Step>();
        while (!current_steps.isEmpty()) {
            Step current_step = current_steps.poll();
            int step_x = current_step.getPosition().x;
            int step_y = current_step.getPosition().y;
            if (current_step.getMovementPoint() > move_mark_map[step_x][step_y]) {
                move_mark_map[step_x][step_y] = current_step.getMovementPoint();
                if (getGame().canUnitMove(unit, step_x, step_y)) {
                    movable_positions.add(current_step.getPosition());
                }
            }
            for (int i = 0; i < 4; i++) {
                int next_x = current_step.getPosition().x + x_dir[i];
                int next_y = current_step.getPosition().y + y_dir[i];
                Point next = new Point(next_x, next_y);
                int current_mp = current_step.getMovementPoint();
                if (getGame().getMap().isWithinMap(next_x, next_y)) {
                    int mp_cost = UnitToolkit.getMovementPointCost(unit, getGame().getMap().getTile(next_x, next_y));
                    if (mp_cost <= current_mp && current_mp - mp_cost > move_mark_map[next_x][next_y]) {
                        Unit target_unit = game.getMap().getUnit(next_x, next_y);
                        if (getGame().canMoveThrough(unit, target_unit)) {
                            Step next_step = new Step(next, current_mp - mp_cost);
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

    public ObjectSet<Point> getMovablePositions() {
        return movable_positions;
    }

    public ObjectSet<Point> getAttackablePositions() {
        return attackable_positions;
    }

    public Array<Point> getMovePath(int dest_x, int dest_y) {
        if (move_path == null || move_path.size == 0) {
            createMovePath(dest_x, dest_y);
        } else {
            Point current_dest = move_path.get(move_path.size - 1);
            if (dest_x != current_dest.x || dest_y != current_dest.y) {
                createMovePath(dest_x, dest_y);
            }
        }
        return move_path;
    }

    private void createMovePath(int dest_x, int dest_y) {
        move_path = new Array<Point>();
        int start_x = getSelectedUnit().getX();
        int start_y = getSelectedUnit().getY();
        if (start_x != dest_x || start_y != dest_y) {
            Point dest_position = getGame().getMap().getPosition(dest_x, dest_y);
            if (movable_positions.contains(dest_position)) {
                createMovePath(dest_x, dest_y, start_x, start_y);
            }
        }
    }

    private void createMovePath(int current_x, int current_y, int start_x, int start_y) {
        move_path.insert(0, new Point(current_x, current_y));
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

    public ObjectSet<Point> createAttackablePositions(Unit unit) {
        int unit_x = unit.getX();
        int unit_y = unit.getY();
        int min_ar = unit.getMinAttackRange();
        int max_ar = unit.getMaxAttackRange();
        ObjectSet<Point> attackable_positions = new ObjectSet<Point>();
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
        if (getState() == STATE_HEAL) {
            attackable_positions.add(getGame().getMap().getPosition(unit.getX(), unit.getY()));
        }
        return attackable_positions;
    }

    public boolean hasEnemyWithinRange(Unit unit) {
        ObjectSet<Point> attackable_positions = createAttackablePositions(unit);
        for (Point point : attackable_positions) {
            if (getSelectedUnit().hasAbility(Ability.DESTROYER) && getGame().getMap().getUnit(point.x, point.y) == null
                    && getGame().getMap().getTile(point.x, point.y).isDestroyable()) {
                return true;
            }
            Unit target = getGame().getMap().getUnit(point.x, point.y);
            if (getGame().isEnemy(unit, target)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllyCanHealWithinRange(Unit unit) {
        ObjectSet<Point> attackable_positions = createAttackablePositions(unit);
        for (Point point : attackable_positions) {
            Unit target = getGame().getMap().getUnit(point.x, point.y);
            if (getGame().canHeal(unit, target)) {
                return true;
            }
        }
        return getGame().canHeal(unit, unit);
    }

    public boolean hasTombWithinRange(Unit unit) {
        ObjectSet<Point> attackable_positions = createAttackablePositions(unit);
        for (Point point : attackable_positions) {
            if (getGame().getMap().isTomb(point.x, point.y) && getGame().getMap().getUnit(point.x, point.y) == null) {
                return true;
            }
        }
        return false;
    }

    public boolean canSelectedUnitAct() {
        return !getSelectedUnit().hasAbility(Ability.HEAVY_MACHINE) || getSelectedUnit().isAt(last_position.x, last_position.y);
    }

    public boolean canSelectedUnitMove(int dest_x, int dest_y) {
        Point dest = getGame().getMap().getPosition(dest_x, dest_y);
        return getMovablePositions().contains(dest)
                && getGame().canUnitMove(getSelectedUnit(), dest_x, dest_y)
                && getGame().isUnitAccessible(getSelectedUnit());
    }

    public boolean isMovablePosition(int map_x, int map_y) {
        return getMovablePositions().contains(getGame().getMap().getPosition(map_x, map_y));
    }

}
