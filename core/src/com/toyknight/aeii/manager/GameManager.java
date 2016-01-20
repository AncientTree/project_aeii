package com.toyknight.aeii.manager;

import static com.toyknight.aeii.rule.Rule.Entry.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.animation.*;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.net.server.EmptyAnimationManager;
import com.toyknight.aeii.robot.Robot;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

/**
 * @author toyknight  5/28/2015.
 */
public class GameManager implements GameEventListener, AnimationListener {

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

    private final MovementGenerator movement_generator;

    private final Robot robot;

    private GameCore game;
    private UnitToolkit unit_toolkit;
    private GameManagerListener manager_listener;

    private int state;
    protected Unit selected_unit;
    protected Point last_position;

    private final Array<Point> move_path;
    private final ObjectSet<Point> movable_positions;
    private final ObjectSet<Point> attackable_positions;

    public GameManager() {
        this(new EmptyAnimationManager());
    }

    public GameManager(AnimationDispatcher dispatcher) {
        this.robot = new Robot(this);
        this.animation_dispatcher = dispatcher;
        this.animation_dispatcher.setListener(this);
        this.movement_generator = new MovementGenerator();
        this.event_executor = new GameEventExecutor(this, dispatcher);
        this.move_path = new Array<Point>();
        this.movable_positions = new ObjectSet<Point>();
        this.attackable_positions = new ObjectSet<Point>();
    }

    public void setGame(GameCore game) {
        this.game = game;
        this.unit_toolkit = new UnitToolkit(game);
        this.state = STATE_SELECT;
        getGameEventExecutor().clearGameEvents();
        getAnimationDispatcher().clearAnimations();
        getMovementGenerator().setGame(game);
        getRobot().reset();
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

    public MovementGenerator getMovementGenerator() {
        return movement_generator;
    }

    public UnitToolkit getUnitToolkit() {
        return unit_toolkit;
    }

    public Robot getRobot() {
        return robot;
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
        if (!isProcessing()) {
            checkGameState();
        }
    }

    @Override
    public void onGameEventFinished() {
        if (!isAnimating()) {
            checkGameState();
        }
    }

    private void checkGameState() {
        if (getListener() != null) {
            if (getGame().isGameOver()) {
                getListener().onGameOver();
            } else {
                getListener().onScreenUpdateRequested();
            }
        }
    }

    public void update(float delta) {
        if (getAnimationDispatcher().isAnimating()) {
            getAnimationDispatcher().updateAnimation(delta);
        } else {
            getGameEventExecutor().dispatchGameEvents();
        }
        if (!isAnimating() && !isProcessing() && getGame().getCurrentPlayer().getType() == Player.ROBOT) {
            if (!getRobot().isCalculating()) {
                if (getRobot().isOperating()) {
                    getRobot().operate(delta);
                } else {
                    getRobot().calculate();
                }
            }
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

    public Unit getSelectedUnit() {
        return selected_unit;
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
        attackable_positions.clear();
        attackable_positions.addAll(createAttackablePositions(getSelectedUnit(), false));
    }

    public void beginSummonPhase() {
        setState(STATE_SUMMON);
        attackable_positions.clear();
        attackable_positions.addAll(createAttackablePositions(getSelectedUnit(), false));
    }

    public void beginHealPhase() {
        setState(STATE_HEAL);
        attackable_positions.clear();
        attackable_positions.addAll(createAttackablePositions(getSelectedUnit(), true));
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

    public void doMove(int dest_x, int dest_y) {
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
                getGame().getRule().getInteger(ATTACK_EXPERIENCE));

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
                            getGame().getRule().getInteger(ATTACK_EXPERIENCE));
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            defender.getX(), defender.getY(),
                            getGame().getRule().getInteger(COUNTER_EXPERIENCE));
                } else {
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            defender.getX(), defender.getY(),
                            getGame().getRule().getInteger(KILL_EXPERIENCE));
                }
            } else {
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        attacker.getX(), attacker.getY(),
                        getGame().getRule().getInteger(ATTACK_EXPERIENCE));
            }
        } else {
            submitGameEvent(
                    GameEvent.ATTACK,
                    attacker.getX(), attacker.getY(),
                    defender.getX(), defender.getY(),
                    attack_damage, -1);
            submitGameEvent(
                    GameEvent.GAIN_EXPERIENCE,
                    attacker.getX(), attacker.getY(),
                    getGame().getRule().getInteger(KILL_EXPERIENCE));
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
                    getGame().getRule().getInteger(ATTACK_EXPERIENCE));
            submitGameEvent(GameEvent.ACTION_FINISH, summoner.getX(), summoner.getY());
        }
    }

    public void doHeal(int target_x, int target_y) {
        Unit healer = getSelectedUnit();
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canHeal(healer, target_x, target_y)) {
            int heal = UnitToolkit.getHeal(healer, target);
            submitGameEvent(GameEvent.HEAL, healer.getX(), healer.getY(), target_x, target_y, heal);
            if (target.getCurrentHp() + heal > 0) {
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        healer.getX(), healer.getY(),
                        getGame().getRule().getInteger(ATTACK_EXPERIENCE));
            } else {
                submitGameEvent(GameEvent.UNIT_DESTROY, target.getX(), target.getY());
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        healer.getX(), healer.getY(),
                        getGame().getRule().getInteger(KILL_EXPERIENCE));
            }
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

    public void doStandbySelectedUnit() {
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
                doStandbySelectedUnit();
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
                doStandbySelectedUnit();
            }
        }
    }

    public void createMovablePositions() {
        movable_positions.clear();
        movable_positions.addAll(getMovementGenerator().createMovablePositions(getSelectedUnit()));
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
        move_path.clear();
        move_path.addAll(getMovementGenerator().createMovePath(getSelectedUnit(), dest_x, dest_y));
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
        return !getSelectedUnit().hasAbility(Ability.HEAVY_MACHINE)
                || getSelectedUnit().isAt(last_position.x, last_position.y);
    }

    public boolean canSelectedUnitMove(int dest_x, int dest_y) {
        Point destination = getGame().getMap().getPosition(dest_x, dest_y);
        System.out.println(getMovablePositions().size);
        return getMovablePositions().contains(destination)
                && getGame().isUnitAccessible(getSelectedUnit())
                && getGame().canUnitMove(getSelectedUnit(), dest_x, dest_y);
    }

    public boolean canBuy(int index, int team, int map_x, int map_y) {
        Unit sample_unit = UnitFactory.getSample(index);
        int price = getGame().getUnitPrice(index, team);
        int movement_point = sample_unit.getMovementPoint();
        sample_unit.setCurrentMovementPoint(movement_point);
        sample_unit.setX(map_x);
        sample_unit.setY(map_y);
        return price >= 0
                && getGame().getCurrentPlayer().getGold() >= price
                && getMovementGenerator().createMovablePositions(sample_unit).size > 0
                && (getGame().getCurrentTeamPopulation() < getGame().getMaxPopulation() || sample_unit.isCommander());
    }

}
