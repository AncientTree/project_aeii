package net.toyknight.aeii.manager;

import static net.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.utils.UnitToolkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 1/28/2016.
 */
public class OperationExecutor {

    private final Object OPERATION_LOCK = new Object();

    private final GameManager manager;

    private final Queue<Operation> operation_queue;

    public OperationExecutor(GameManager manager) {
        this.manager = manager;
        this.operation_queue = new LinkedList<Operation>();
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    public GameManager getManager() {
        return manager;
    }

    public void reset() {
        synchronized (OPERATION_LOCK) {
            operation_queue.clear();
        }
    }

    public void operate() throws CheatingException {
        synchronized (OPERATION_LOCK) {
            if (operation_queue.size() > 0) {
                Operation operation;
                if ((operation = operation_queue.poll()) != null) {
                    executeOperation(operation);
                }
                if (operation_queue.size() == 0) {
                    getManager().onOperationFinished();
                }
            }
        }
    }

    public boolean isOperating() {
        return operation_queue.size() > 0;
    }

    public void submitOperation(int type, int... parameters) {
        synchronized (OPERATION_LOCK) {
            Operation operation = new Operation(type, parameters);
            operation_queue.add(operation);
        }
    }

    private void submitGameEvent(int type, Object... params) {
        getManager().getGameEventExecutor().submitGameEvent(type, params);
    }

    private void executeOperation(Operation operation) throws CheatingException {
        switch (operation.getType()) {
            case Operation.ACTION_FINISH:
                int unit_x = operation.getParameter(0);
                int unit_y = operation.getParameter(1);
                onActionFinish(unit_x, unit_y);
                break;
            case Operation.ATTACK:
                int attacker_x = operation.getParameter(0);
                int attacker_y = operation.getParameter(1);
                int target_x = operation.getParameter(2);
                int target_y = operation.getParameter(3);
                onAttack(attacker_x, attacker_y, target_x, target_y);
                break;
            case Operation.BUY:
                int index = operation.getParameter(0);
                int team = operation.getParameter(1);
                target_x = operation.getParameter(2);
                target_y = operation.getParameter(3);
                onBuy(index, team, target_x, target_y);
                break;
            case Operation.COUNTER:
                attacker_x = operation.getParameter(0);
                attacker_y = operation.getParameter(1);
                target_x = operation.getParameter(2);
                target_y = operation.getParameter(3);
                onCounter(attacker_x, attacker_y, target_x, target_y);
                break;
            case Operation.NEXT_TURN:
                onNextTurn();
                break;
            case Operation.HEAL:
                int healer_x = operation.getParameter(0);
                int healer_y = operation.getParameter(1);
                target_x = operation.getParameter(2);
                target_y = operation.getParameter(3);
                onHeal(healer_x, healer_y, target_x, target_y);
                break;
            case Operation.MOVE:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                target_x = operation.getParameter(2);
                target_y = operation.getParameter(3);
                onMove(unit_x, unit_y, target_x, target_y);
                break;
            case Operation.MOVE_FINISH:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onMoveFinish(unit_x, unit_y);
                break;
            case Operation.MOVE_REVERSE:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                target_x = operation.getParameter(2);
                target_y = operation.getParameter(3);
                onMoveReverse(unit_x, unit_y, target_x, target_y);
                break;
            case Operation.MOVE_REVERSE_FINISH:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onMoveReverseFinish(unit_x, unit_y);
                break;
            case Operation.OCCUPY:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onOccupy(unit_x, unit_y);
                break;
            case Operation.REPAIR:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onRepair(unit_x, unit_y);
                break;
            case Operation.SELECT:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onSelect(unit_x, unit_y);
                break;
            case Operation.SELECT_FINISH:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onSelectFinish(unit_x, unit_y);
                break;
            case Operation.STANDBY:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                onStandby(unit_x, unit_y);
                break;
            case Operation.SUMMON:
                unit_x = operation.getParameter(0);
                unit_y = operation.getParameter(1);
                target_x = operation.getParameter(2);
                target_y = operation.getParameter(3);
                onSummon(unit_x, unit_y, target_x, target_y);
                break;
            case Operation.TURN_STARTED:
                int current_turn = getGame().getCurrentTurn();
                getManager().fireTurnStartEvent(current_turn);

                Position map_focus = getGame().getTeamFocus(getGame().getCurrentTeam());
                getManager().fireMapFocusEvent(map_focus.x, map_focus.y, true);
                getManager().fireStateChangeEvent();
                break;
            default:
                //do nothing
        }
        getManager().getGameEventExecutor().dispatchGameEvents();
    }

    private void onActionFinish(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().isUnitAccessible(unit)) {
            getManager().fireMapFocusEvent(unit_x, unit_y, false);
            if (UnitToolkit.canMoveAgain(unit)) {
                Position position = getGame().getMap().getPosition(unit);
                getManager().setLastPosition(position);
                getManager().beginRemovePhase();
            } else {
                onStandby(unit_x, unit_y);
            }
        } else {
            getManager().setState(GameManager.STATE_SELECT);
        }
    }

    private void onAttack(int attacker_x, int attacker_y, int target_x, int target_y) {
        Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
        Unit defender = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canAttack(attacker, target_x, target_y)) {
            if (defender == null) {
                submitGameEvent(GameEvent.ATTACK, attacker_x, attacker_y, target_x, target_y, -1, false);
                submitGameEvent(GameEvent.TILE_DESTROY, target_x, target_y);
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        attacker_x, attacker_y,
                        getGame().getRule().getInteger(ATTACK_EXPERIENCE));
            } else {
                int attack_damage = getManager().getUnitToolkit().getDamage(attacker, defender, true);
                submitGameEvent(GameEvent.ATTACK, attacker_x, attacker_y, target_x, target_y, attack_damage, false);
                if (attack_damage < defender.getCurrentHp()) {
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            attacker_x, attacker_y,
                            getGame().getRule().getInteger(ATTACK_EXPERIENCE));
                } else {
                    submitGameEvent(GameEvent.UNIT_DESTROY, target_x, target_y, attacker.getTeam());
                    submitGameEvent(
                            GameEvent.GAIN_EXPERIENCE,
                            attacker_x, attacker_y,
                            getGame().getRule().getInteger(KILL_EXPERIENCE));
                }
            }
        }
    }

    private void onBuy(int index, int team, int target_x, int target_y) {
        submitGameEvent(GameEvent.BUY, index, team, target_x, target_y);
    }

    private void onCounter(int attacker_x, int attacker_y, int target_x, int target_y) {
        Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
        Unit defender = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canCounter(attacker, defender)) {
            int counter_damage = getManager().getUnitToolkit().getDamage(defender, attacker, true);
            submitGameEvent(GameEvent.ATTACK, target_x, target_y, attacker_x, attacker_y, counter_damage, true);
            if (counter_damage < attacker.getCurrentHp()) {
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        target_x, target_y,
                        getGame().getRule().getInteger(COUNTER_EXPERIENCE));
            } else {
                submitGameEvent(GameEvent.UNIT_DESTROY, attacker_x, attacker_y, defender.getTeam());
                submitGameEvent(
                        GameEvent.GAIN_EXPERIENCE,
                        target_x, target_y,
                        getGame().getRule().getInteger(KILL_EXPERIENCE));
            }
        }
    }

    private void onNextTurn() {
        getManager().setState(GameManager.STATE_SELECT);
        submitGameEvent(GameEvent.NEXT_TURN);
        //calculate hp change at turn start
        int next_team = getGame().getNextTeam();

        JSONArray hp_changes = new JSONArray();
        ObjectSet<Unit> destroyed_units = new ObjectSet<Unit>();

        for (Unit unit : getGame().getMap().getUnits()) {
            int change = 0;
            if (unit.getTeam() == next_team) {
                //the terrain heal
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                change = getManager().getUnitToolkit().getTerrainHeal(unit, tile);
                //the poison damage
                if (getGame().isGonnaBePoisoned(unit)) {
                    if (unit.hasAbility(Ability.UNDEAD)) {
                        change += Rule.POISON_DAMAGE;
                    } else {
                        change = -Rule.POISON_DAMAGE;
                    }
                }
                //rehabilitation
                if (unit.hasAbility(Ability.REHABILITATION)) {
                    change += unit.getMaxHp() / 4;
                }
                if (unit.getCurrentHp() > unit.getMaxHp()) {
                    change -= unit.getCurrentHp() - unit.getMaxHp();
                }
                change = UnitToolkit.validateHpChange(unit, change);
            } else {
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                if (getGame().isEnemy(unit.getTeam(), next_team)
                        && tile.isCastle() && tile.getTeam() == next_team) {
                    change = UnitToolkit.validateHpChange(unit, -50);
                }
            }
            if (change != 0) {
                JSONObject hp_change = new JSONObject();
                hp_change.put("x", unit.getX());
                hp_change.put("y", unit.getY());
                hp_change.put("change", change);
                hp_changes.put(hp_change);
                if (unit.getCurrentHp() + change <= 0) {
                    destroyed_units.add(unit);
                }
            }
        }
        submitGameEvent(GameEvent.HP_CHANGE, hp_changes);
        for (Unit unit : destroyed_units) {
            submitGameEvent(GameEvent.UNIT_DESTROY, unit.getX(), unit.getY(), -1);
        }
    }

    private void onHeal(int healer_x, int healer_y, int target_x, int target_y) {
        Unit healer = getGame().getMap().getUnit(healer_x, healer_y);
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (getGame().canHeal(healer, target)) {
            int heal = UnitToolkit.getHealerHeal(healer, target);
            if (target.getCurrentHp() + heal <= 0) {
                submitGameEvent(GameEvent.HEAL,
                        healer_x, healer_y, target_x, target_y, UnitToolkit.validateHpChange(target, heal));
                submitGameEvent(GameEvent.UNIT_DESTROY, target_x, target_y, healer.getTeam());
            } else {
                submitGameEvent(GameEvent.HEAL, healer_x, healer_y, target_x, target_y, heal);
            }
            int experience = heal + target.getCurrentHp() > 0 ?
                    getGame().getRule().getInteger(ATTACK_EXPERIENCE) :
                    getGame().getRule().getInteger(KILL_EXPERIENCE);
            submitGameEvent(GameEvent.GAIN_EXPERIENCE, healer_x, healer_y, experience);
        }
    }

    private void onMove(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        getManager().getPositionGenerator().createMovablePositions(unit);
        int movement_point = getManager().getPositionGenerator().getMovementPointRemains(unit, target_x, target_y);

        if (getGame().canUnitMove(unit, target_x, target_y) && movement_point >= 0) {
            JSONArray move_path = new JSONArray();
            for (Position position : getManager().getPositionGenerator().createMovePath(unit, target_x, target_y)) {
                JSONObject step = new JSONObject();
                step.put("x", position.x);
                step.put("y", position.y);
                move_path.put(step);
            }
            submitGameEvent(GameEvent.MOVE, unit_x, unit_y, target_x, target_y, movement_point, move_path);
        }
    }

    private void onMoveFinish(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().isUnitAccessible(unit)) {
            switch (getManager().getState()) {
                case GameManager.STATE_MOVE:
                    getManager().setState(GameManager.STATE_ACTION);
                    break;
                case GameManager.STATE_REMOVE:
                    onStandby(unit_x, unit_y);
                    break;
                default:
                    //do nothing
            }
        } else {
            getManager().setState(GameManager.STATE_SELECT);
        }
    }

    private void onMoveReverse(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (unit != null && getGame().getMap().canMove(target_x, target_y)) {
            submitGameEvent(GameEvent.REVERSE, unit_x, unit_y, target_x, target_y);
        }
    }

    private void onMoveReverseFinish(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().isUnitAccessible(unit)) {
            getManager().setSelectedUnit(unit);
            getManager().beginMovePhase();
        }
    }

    private void onOccupy(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().canOccupy(unit, unit_x, unit_y)) {
            submitGameEvent(GameEvent.OCCUPY, unit_x, unit_y, unit.getTeam());
        }
    }

    private void onRepair(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().canRepair(unit, unit_x, unit_y)) {
            submitGameEvent(GameEvent.REPAIR, unit_x, unit_y);
        }
    }

    private void onSelect(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().isUnitAccessible(unit)) {
            submitGameEvent(GameEvent.SELECT, unit_x, unit_y);
        }
    }

    private void onSelectFinish(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().isUnitAccessible(unit)) {
            switch (getGame().getCurrentPlayer().getType()) {
                case Player.LOCAL:
                    Tile tile = getGame().getMap().getTile(unit_x, unit_y);
                    if (unit.isCommander() && getGame().isCastleAccessible(tile)) {
                        getManager().setState(GameManager.STATE_BUY);
                    } else {
                        getManager().beginMovePhase();
                    }
                    break;
                case Player.ROBOT:
                    getManager().beginMovePhase();
                    break;
                default:
                    //do nothing
            }
        }
    }

    private void onStandby(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (getGame().isUnitAccessible(unit)) {
            submitGameEvent(GameEvent.STANDBY, unit_x, unit_y);

            //deal with refresh aura
            ObjectSet<Position> aura_positions =
                    getManager().getPositionGenerator().createPositionsWithinRange(unit_x, unit_y, 0, 2);

            JSONArray hp_changes = new JSONArray();
            ObjectSet<Unit> destroyed_units = new ObjectSet<Unit>();

            if (unit.getCurrentHp() > unit.getMaxHp()) {
                int change = unit.getMaxHp() - unit.getCurrentHp();
                JSONObject hp_change = new JSONObject();
                hp_change.put("x", unit.getX());
                hp_change.put("y", unit.getY());
                hp_change.put("change", change);
                hp_changes.put(hp_change);
            }

            for (Position target_position : aura_positions) {
                Unit target = getGame().getMap().getUnit(target_position);
                if (unit.hasAbility(Ability.REFRESH_AURA) && getGame().canRefresh(unit, target)) {
                    int heal = UnitToolkit.getRefresherHeal(unit, target);
                    int change = UnitToolkit.validateHpChange(target, heal);
                    if (change != 0) {
                        JSONObject hp_change = new JSONObject();
                        hp_change.put("x", target.getX());
                        hp_change.put("y", target.getY());
                        hp_change.put("change", change);
                        hp_changes.put(hp_change);
                        if (target.getCurrentHp() + change <= 0) {
                            destroyed_units.add(target);
                        }
                    }
                }
            }
            submitGameEvent(GameEvent.HP_CHANGE, hp_changes);
            for (Unit destroyed_unit : destroyed_units) {
                submitGameEvent(GameEvent.UNIT_DESTROY, destroyed_unit.getX(), destroyed_unit.getY(), unit.getTeam());
            }
            int experience = destroyed_units.size * getGame().getRule().getInteger(KILL_EXPERIENCE);
            if (experience > 0) {
                submitGameEvent(GameEvent.GAIN_EXPERIENCE, unit_x, unit_y, experience);
            }
            submitGameEvent(GameEvent.STANDBY_FINISH, unit_x, unit_y);
            getManager().setState(GameManager.STATE_SELECT);
        }
    }

    private void onSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(summoner_x, summoner_y);
        if (getGame().canSummon(unit, target_x, target_y)) {
            submitGameEvent(GameEvent.SUMMON, summoner_x, summoner_y, target_x, target_y);
            submitGameEvent(
                    GameEvent.GAIN_EXPERIENCE,
                    summoner_x, summoner_y,
                    getGame().getRule().getInteger(ATTACK_EXPERIENCE));
        }
    }

}
