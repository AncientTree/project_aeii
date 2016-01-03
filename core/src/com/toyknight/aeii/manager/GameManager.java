package com.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.animator.*;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.*;

/**
 * @author toyknight  5/28/2015.
 */
public class GameManager implements GameEventListener, AnimationManagerListener {

    public static final int STATE_SELECT = 0x1;
    public static final int STATE_MOVE = 0x2;
    public static final int STATE_REMOVE = 0x3;
    public static final int STATE_ACTION = 0x4;
    public static final int STATE_ATTACK = 0x5;
    public static final int STATE_SUMMON = 0x6;
    public static final int STATE_HEAL = 0x7;
    public static final int STATE_PREVIEW = 0x8;
    public static final int STATE_BUY = 0x9;

    private final GameEventExecutor event_executor;
    private final AnimationDispatcher animation_dispatcher;

    private GameCore game;
    private UnitToolkit unit_toolkit;
    private GameManagerListener manager_listener;

    private int state;
    protected Unit selected_unit;
    protected Point last_position;

    private int[][] move_mark_map;
    private Array<Point> move_path;
    private final ObjectSet<Point> movable_positions;
    private ObjectSet<Point> attackable_positions;

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    public GameManager(AnimationDispatcher dispatcher) {
        this.animation_dispatcher = dispatcher;
        this.animation_dispatcher.setListener(this);
        this.event_executor = new GameEventExecutor(this, dispatcher);
        this.movable_positions = new ObjectSet<Point>();
    }

    public void setGame(GameCore game) {
        this.game = game;
        this.unit_toolkit = new UnitToolkit(game);
        this.state = STATE_SELECT;
        getGameEventExecutor().clearGameEvents();
        getAnimationDispatcher().clearAnimations();
    }

    public GameCore getGame() {
        return game;
    }

    public GameEventExecutor getGameEventExecutor() {
        return event_executor;
    }

    public AnimationDispatcher getAnimationDispatcher() {
        return animation_dispatcher;
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

    public void setState(int state) {
        if (state != this.state) {
            this.state = state;
            if (getListener() != null) {
                getListener().onManagerStateChanged();
            }
        }
    }

    public int getState() {
        return state;
    }

    public boolean isProcessing() {
        return getGameEventExecutor().isProcessing();
    }

    public boolean isAnimating() {
        return getAnimationDispatcher().isAnimating();
    }

    public Animator getCurrentAnimation() {
        return getAnimationDispatcher().getCurrentAnimation();
    }

    @Override
    public void onAnimationFinished() {
        if (getListener() != null) {
            if (getGame().isGameOver()) {
                getListener().onGameOver();
            } else {
                getListener().onScreenUpdateRequested();
            }
        }
    }

    @Override
    public void onGameEventFinished() {
        if (getListener() != null) {
            getListener().onScreenUpdateRequested();
        }
    }

    public void update(float delta) {
        if (getAnimationDispatcher().isAnimating()) {
            getAnimationDispatcher().updateAnimation(delta);
        } else {
            getGameEventExecutor().dispatchGameEvents();
        }
    }

    public void submitGameEvent(int type, Object... params) {
        submitGameEvent(new GameEvent(type, params));
    }

    public void submitGameEvent(GameEvent event) {
        if (getListener() != null) {
            getListener().onGameEventSubmitted(event);
        }
        getGameEventExecutor().submitGameEvent(event);
    }

    public void requestMapFocus(int map_x, int map_y) {
        if (getListener() != null) {
            getListener().onMapFocusRequired(map_x, map_y);
        }
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
        attackable_positions = createAttackablePositions(getSelectedUnit(), false);
    }

    public void beginSummonPhase() {
        setState(STATE_SUMMON);
        attackable_positions = createAttackablePositions(getSelectedUnit(), false);
    }

    public void beginHealPhase() {
        setState(STATE_HEAL);
        attackable_positions = createAttackablePositions(getSelectedUnit(), true);
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
            submitGameEvent(GameEvent.SELECT, x, y);
        }
    }

    public void doMoveUnit(int dest_x, int dest_y) {
        if (canSelectedUnitMove(dest_x, dest_y)) {
            int start_x = getSelectedUnit().getX();
            int start_y = getSelectedUnit().getY();
            submitGameEvent(GameEvent.MOVE, start_x, start_y, dest_x, dest_y);
        }
    }

    public void doReverseMove() {
        Point last_position = getLastPosition();
        Unit selected_unit = getSelectedUnit();
        submitGameEvent(GameEvent.REVERSE, selected_unit.getX(), selected_unit.getY(), last_position.x, last_position.y);
    }

    public void doAttack(int target_x, int target_y) {
        Unit attacker = getSelectedUnit();
        if (UnitToolkit.isWithinRange(attacker, target_x, target_y)) {
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            if (defender == null) {
                if (attacker.hasAbility(Ability.DESTROYER)
                        && getGame().getMap().getTile(target_x, target_y).isDestroyable()) {
                    onUnitAttackTile(attacker, target_x, target_y);
                }
            } else {
                if (getGame().canAttack(attacker, target_x, target_y)) {
                    onUnitAttackUnit(UnitFactory.cloneUnit(attacker), UnitFactory.cloneUnit(defender));
                }
            }
        }
    }

    private void onUnitAttackTile(Unit attacker, int target_x, int target_y) {
        submitGameEvent(GameEvent.ATTACK, attacker.getX(), attacker.getY(), target_x, target_y, -1, -1);
        submitGameEvent(GameEvent.STANDBY, attacker.getX(), attacker.getY());
        submitGameEvent(GameEvent.TILE_DESTROY, target_x, target_y);

        submitGameEvent(
                GameEvent.GAIN_EXPERIENCE,
                attacker.getX(), attacker.getY(),
                getGame().getRule().getAttackExperience());

        submitGameEvent(GameEvent.ACTION_FINISH, attacker.getX(), attacker.getY());
    }

    private void onUnitAttackUnit(Unit attacker, Unit defender) {
        //attack pre-calculation
        int attack_damage = getUnitToolkit().getDamage(attacker, defender);
        UnitToolkit.attachAttackStatus(attacker, defender);
        defender.changeCurrentHp(-attack_damage);
        if (defender.getCurrentHp() > 0) {
            int counter_damage = getUnitToolkit().canCounter(defender, attacker) ?
                    getUnitToolkit().getDamage(defender, attacker) : -1;
            submitGameEvent(
                    GameEvent.ATTACK,
                    attacker.getX(), attacker.getY(),
                    defender.getX(), defender.getY(),
                    attack_damage, counter_damage);
            if (counter_damage >= 0) {
                attacker.changeCurrentHp(-counter_damage);
                if (attacker.getCurrentHp() > 0) {
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            attacker.getX(), attacker.getY(),
                            getGame().getRule().getAttackExperience());
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            defender.getX(), defender.getY(),
                            getGame().getRule().getCounterExperience());
                } else {
                    submitGameEvent(GameEvent.UNIT_DESTROY, attacker.getX(), attacker.getY());
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            defender.getX(), defender.getY(),
                            getGame().getRule().getKillExperience());
                }
            } else {
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        attacker.getX(), attacker.getY(),
                        getGame().getRule().getAttackExperience());
            }
        } else {
            submitGameEvent(
                    GameEvent.ATTACK,
                    attacker.getX(), attacker.getY(),
                    defender.getX(), defender.getY(),
                    attack_damage, -1);
            submitGameEvent(GameEvent.UNIT_DESTROY, defender.getX(), defender.getY());
            submitGameEvent(
                    GameEvent.GAIN_EXPERIENCE,
                    attacker.getX(), attacker.getY(),
                    getGame().getRule().getKillExperience());
        }
        submitGameEvent(GameEvent.ACTION_FINISH, attacker.getX(), attacker.getY());
    }

    public void doSummon(int target_x, int target_y) {
        Unit summoner = getSelectedUnit();
        if (getGame().canSummon(summoner, target_x, target_y)) {
            submitGameEvent(GameEvent.SUMMON, summoner.getX(), summoner.getY(), target_x, target_y);
            submitGameEvent(
                    GameEvent.GAIN_EXPERIENCE,
                    summoner.getX(), summoner.getY(),
                    getGame().getRule().getAttackExperience());
            submitGameEvent(GameEvent.ACTION_FINISH, summoner.getX(), summoner.getY());
        }
    }

    public void doHeal(int target_x, int target_y) {
        Unit healer = getSelectedUnit();
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canHeal(healer, target_x, target_y)) {
            int heal = UnitToolkit.getHeal(healer, target);
            int experience = target.getCurrentHp() + heal > 0 ?
                    getGame().getRule().getAttackExperience() : getGame().getRule().getKillExperience();

            submitGameEvent(GameEvent.HEAL, healer.getX(), healer.getY(), target_x, target_y, heal);
            if (target.getCurrentHp() + heal <= 0) {
                submitGameEvent(GameEvent.UNIT_DESTROY, target.getX(), target.getY());
            }
            submitGameEvent(GameEvent.GAIN_EXPERIENCE, healer.getX(), healer.getY(), experience);
            submitGameEvent(GameEvent.ACTION_FINISH, healer.getX(), healer.getY());
        }
    }

    public void doRepair() {
        Unit unit = getSelectedUnit();
        submitGameEvent(GameEvent.REPAIR, unit.getX(), unit.getY());
        submitGameEvent(GameEvent.ACTION_FINISH, unit.getX(), unit.getY());
    }

    public void doOccupy() {
        Unit unit = getSelectedUnit();
        submitGameEvent(GameEvent.OCCUPY, unit.getX(), unit.getY(), unit.getTeam());
        submitGameEvent(GameEvent.ACTION_FINISH, unit.getX(), unit.getY());
    }

    public void doBuyUnit(int index, int x, int y) {
        int team = getGame().getCurrentTeam();
        submitGameEvent(GameEvent.BUY, index, team, x, y);
    }

    public void doStandbyUnit() {
        Unit unit = getSelectedUnit();
        if (getGame().isUnitAccessible(unit)) {
            submitGameEvent(GameEvent.STANDBY, unit.getX(), unit.getY());
        }
    }

    public void doEndTurn() {
        submitGameEvent(GameEvent.NEXT_TURN);
    }

    public void onUnitMoveFinish() {
        switch (getState()) {
            case GameManager.STATE_MOVE:
                setState(GameManager.STATE_ACTION);
                break;
            case GameManager.STATE_REMOVE:
                doStandbyUnit();
                break;
        }
    }

    public void onUnitActionFinish(Unit unit) {
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

    public ObjectSet<Point> createAttackablePositions(Unit unit, boolean itself) {
        int unit_x = unit.getX();
        int unit_y = unit.getY();
        int min_ar = unit.getMinAttackRange();
        int max_ar = unit.getMaxAttackRange();
        ObjectSet<Point> attackable_positions = createPositionsWithinRange(unit_x, unit_y, min_ar, max_ar);
        if (itself) {
            attackable_positions.add(getGame().getMap().getPosition(unit.getX(), unit.getY()));
        }
        return attackable_positions;
    }

    public ObjectSet<Point> createPositionsWithinRange(int x, int y, int min_range, int max_range) {
        ObjectSet<Point> positions = new ObjectSet<Point>();
        for (int ar = min_range; ar <= max_range; ar++) {
            for (int dx = -ar; dx <= ar; dx++) {
                int dy = dx >= 0 ? ar - dx : -ar - dx;
                if (game.getMap().isWithinMap(x + dx, y + dy)) {
                    positions.add(new Point(x + dx, y + dy));
                }
                if (dy != 0) {
                    if (game.getMap().isWithinMap(x + dx, y - dy)) {
                        positions.add(new Point(x + dx, y - dy));
                    }
                }
            }
        }
        return positions;
    }

    public boolean hasEnemyWithinRange(Unit unit) {
        ObjectSet<Point> attackable_positions = createAttackablePositions(unit, false);
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
        ObjectSet<Point> attackable_positions = createAttackablePositions(unit, true);
        for (Point point : attackable_positions) {
            Unit target = getGame().getMap().getUnit(point.x, point.y);
            if (getGame().canHeal(unit, target)) {
                return true;
            }
        }
        return getGame().canHeal(unit, unit);
    }

    public boolean hasTombWithinRange(Unit unit) {
        ObjectSet<Point> attackable_positions = createAttackablePositions(unit, false);
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
