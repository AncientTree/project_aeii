package net.toyknight.aeii.manager;

import static net.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.UnitFactory;
import net.toyknight.aeii.utils.UnitToolkit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 11/1/2015.
 */
public class GameEventExecutor {

    private final GameManager game_manager;

    private final Queue<JSONObject> event_queue;

    private boolean check_event_value = false;

    public GameEventExecutor(GameManager game_manager) {
        this.game_manager = game_manager;
        this.event_queue = new LinkedList<JSONObject>();
    }

    public GameManager getGameManager() {
        return game_manager;
    }

    public GameCore getGame() {
        return getGameManager().getGame();
    }

    public AnimationDispatcher getAnimationDispatcher() {
        return getGameManager().getAnimationDispatcher();
    }

    public void setCheckEventValue(boolean check_event_value) {
        this.check_event_value = check_event_value;
    }

    public void reset() {
        event_queue.clear();
    }

    public boolean isProcessing() {
        return event_queue.size() > 0;
    }

    public void submitGameEvent(JSONObject event) {
        event_queue.add(event);
    }

    public void submitGameEvent(int type, Object... params) {
        JSONObject event = GameEvent.create(type, params);
        submitGameEvent(event);

        getGameManager().onGameEventSubmitted(event);
    }

    public void dispatchGameEvents() throws CheatingException {
        if (getGame().isGameOver()) {
            getGameManager().onGameEventFinished();
        } else {
            if (event_queue.size() > 0) {
                try {
                    executeGameEvent(event_queue.poll());
                } catch (JSONException ex) {
                    //TODO: Notify invalid event
                }
                if (event_queue.isEmpty()) {
                    getGameManager().onGameEventFinished();
                }
            }
        }
    }

    public void executeGameEvent(JSONObject event) throws JSONException, CheatingException {
        switch (event.getInt("type")) {
            case GameEvent.ATTACK:
                int attacker_x = event.getJSONArray("parameters").getInt(0);
                int attacker_y = event.getJSONArray("parameters").getInt(1);
                int target_x = event.getJSONArray("parameters").getInt(2);
                int target_y = event.getJSONArray("parameters").getInt(3);
                int attack_damage = event.getJSONArray("parameters").getInt(4);
                onAttack(attacker_x, attacker_y, target_x, target_y, attack_damage);
                break;
            case GameEvent.BUY:
                int index = event.getJSONArray("parameters").getInt(0);
                int team = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                onBuy(index, team, target_x, target_y);
                break;
            case GameEvent.NEXT_TURN:
                onNextTurn();
                break;
            case GameEvent.HEAL:
                int healer_x = event.getJSONArray("parameters").getInt(0);
                int healer_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                int heal = event.getJSONArray("parameters").getInt(4);
                onHeal(healer_x, healer_y, target_x, target_y, heal);
                break;
            case GameEvent.MOVE:
                int unit_x = event.getJSONArray("parameters").getInt(0);
                int unit_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                int movement_point = event.getJSONArray("parameters").getInt(4);
                JSONArray move_path = event.getJSONArray("parameters").getJSONArray(5);
                onMove(unit_x, unit_y, target_x, target_y, movement_point, move_path);
                break;
            case GameEvent.OCCUPY:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                team = event.getJSONArray("parameters").getInt(2);
                onOccupy(target_x, target_y, team);
                break;
            case GameEvent.REPAIR:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                onRepair(target_x, target_y);
                break;
            case GameEvent.REVERSE:
                unit_x = event.getJSONArray("parameters").getInt(0);
                unit_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                onReverse(unit_x, unit_y, target_x, target_y);
                break;
            case GameEvent.SELECT:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                onSelect(target_x, target_y);
                break;
            case GameEvent.STANDBY:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                onStandby(target_x, target_y);
                break;
            case GameEvent.SUMMON:
                int summoner_x = event.getJSONArray("parameters").getInt(0);
                int summoner_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                onSummon(summoner_x, summoner_y, target_x, target_y);
                break;
            case GameEvent.HP_CHANGE:
                JSONArray changes = event.getJSONArray("parameters").getJSONArray(0);
                onHpChange(changes);
                break;
            case GameEvent.TILE_DESTROY:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                onTileDestroy(target_x, target_y);
                break;
            case GameEvent.UNIT_DESTROY:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                int destroyer_team = event.getJSONArray("parameters").getInt(2);
                onUnitDestroy(target_x, target_y, destroyer_team);
                break;
            case GameEvent.GAIN_EXPERIENCE:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                int experience = event.getJSONArray("parameters").getInt(2);
                onUnitGainExperience(target_x, target_y, experience);
                break;
            default:
                //do nothing
        }
    }

    private void onAttack(int attacker_x, int attacker_y, int target_x, int target_y, int attack_damage)
            throws CheatingException {
        if (canAttack(attacker_x, attacker_y, target_x, target_y, attack_damage)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            if (defender == null) {
                getAnimationDispatcher().submitUnitAttackAnimation(attacker, target_x, target_y);
            } else {
                if (attack_damage >= 0) {
                    defender.changeCurrentHp(-attack_damage);
                    UnitToolkit.attachAttackStatus(attacker, defender);
                    getAnimationDispatcher().submitUnitAttackAnimation(attacker, defender, attack_damage);
                }
            }
        } else {
            throw new CheatingException("attacking check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canAttack(int attacker_x, int attacker_y, int target_x, int target_y, int attack_damage) {
        Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
        Unit defender = getGame().getMap().getUnit(target_x, target_y);
        boolean base_check = getGame().canAttack(attacker, target_x, target_y);
        if (getGame().getCurrentPlayer().getType() == Player.REMOTE && check_event_value) {
            if (attacker != null && defender != null) {
                int expected_damage = getGameManager().getUnitToolkit().getDamage(attacker, defender, false);
                return base_check && Math.abs(attack_damage - expected_damage) <= 2;
            } else {
                return base_check;
            }
        } else {
            return base_check;
        }
    }

    private void onBuy(int index, int team, int target_x, int target_y) throws CheatingException {
        if (canBuy(index, team)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);
            int price = getGame().getUnitPrice(index, team);
            getGame().getCurrentPlayer().changeGold(-price);

            if (index == UnitFactory.getCommanderIndex()) {
                getGame().restoreCommander(team, target_x, target_y);
            } else {
                getGame().createUnit(index, team, target_x, target_y);
            }
        } else {
            throw new CheatingException("buying check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canBuy(int index, int team) {
        return getGame().isTeamAlive(team)
                && getGame().getPlayer(team).getGold() >= getGame().getUnitPrice(index, team)
                && getGame().getPlayer(team).getPopulation() < getGame().getRule().getInteger(MAX_POPULATION);
    }

    private void onNextTurn() throws JSONException {
        getGame().nextTurn();
        for (Unit unit : getGame().getMap().getUnits()) {
            if (unit.getTeam() == getGame().getCurrentTeam()) {
                unit.updateStatus();
                getGame().resetUnit(unit);
            }
        }
        int team = getGame().getCurrentTeam();
        int income = getGame().gainIncome(team);
        getAnimationDispatcher().submitMessageAnimation(
                Language.getText("LB_CURRENT_TURN") + ": " + getGame().getCurrentTurn(),
                Language.getText("LB_INCOME") + ": " + income,
                0.8f);
    }

    private void onHeal(int healer_x, int healer_y, int target_x, int target_y, int heal) throws CheatingException {
        if (canHeal(healer_x, healer_y, target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Unit target = getGame().getMap().getUnit(target_x, target_y);
            target.changeCurrentHp(heal);
            getAnimationDispatcher().submitHpChangeAnimation(target, heal);
        } else {
            throw new CheatingException("healing check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canHeal(int healer_x, int healer_y, int target_x, int target_y) {
        Unit healer = getGame().getMap().getUnit(healer_x, healer_y);
        return getGame().canHeal(healer, target_x, target_y);
    }

    private void onMove(int unit_x, int unit_y, int target_x, int target_y, int movement_point, JSONArray move_path)
            throws JSONException, CheatingException {
        if (canMove(unit_x, unit_y, target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Array<Position> path = new Array<Position>();
            for (int i = 0; i < move_path.length(); i++) {
                JSONObject step = move_path.getJSONObject(i);
                path.add(getGame().getMap().getPosition(step.getInt("x"), step.getInt("y")));
            }

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);

            getGame().moveUnit(unit_x, unit_y, target_x, target_y);
            unit.setCurrentMovementPoint(movement_point);
            getAnimationDispatcher().submitUnitMoveAnimation(unit, path);
        } else {
            throw new CheatingException("moving check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canMove(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        Position destination = getGame().getMap().getPosition(target_x, target_y);
        boolean base_check = unit != null && getGame().canUnitMove(unit, target_x, target_y);
        if (getGame().getCurrentPlayer().getType() == Player.REMOTE && check_event_value) {
            ObjectSet<Position> movable_positions = getGameManager().getPositionGenerator().createMovablePositions(unit);
            return base_check && movable_positions.contains(destination);
        } else {
            return base_check;
        }
    }

    private void onOccupy(int target_x, int target_y, int team) throws CheatingException {
        if (canOccupy(target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getCapturedTileIndex(team), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_OCCUPIED"), 0.5f);

            onCheckTeamDestroy(target_tile.getTeam());
        } else {
            throw new CheatingException("occupying check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canOccupy(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isCapturable();
    }

    private void onRepair(int target_x, int target_y) throws CheatingException {
        if (canRepair(target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getRepairedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_REPAIRED"), 0.5f);
        } else {
            throw new CheatingException("repairing check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canRepair(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isRepairable();
    }

    private void onReverse(int unit_x, int unit_y, int origin_x, int origin_y) throws CheatingException {
        if (canReverse(unit_x, unit_y, origin_x, origin_y)) {
            getGameManager().fireMapFocusEvent(origin_x, origin_y);

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
            getGame().getMap().moveUnit(unit, origin_x, origin_y);
            getGame().resetUnit(unit);
        } else {
            throw new CheatingException("reversing check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canReverse(int unit_x, int unit_y, int origin_x, int origin_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        return unit != null && getGame().getMap().canMove(origin_x, origin_y);
    }

    private void onSelect(int target_x, int target_y) throws CheatingException {
        if (canSelect(target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().setSelectedUnit(unit);
        } else {
            throw new CheatingException("selecting check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canSelect(int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return target != null && getGame().getCurrentTeam() == target.getTeam() && !target.isStandby();
    }

    private void onStandby(int target_x, int target_y) throws CheatingException {
        if (canStandby(target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGame().standbyUnit(target_x, target_y);

            //deal with tombs
            if (getGame().getMap().isTomb(unit.getX(), unit.getY())) {
                getGame().getMap().removeTomb(unit.getX(), unit.getY());
                if (!unit.hasAbility(Ability.NECROMANCER)) {
                    unit.attachStatus(new Status(Status.POISONED, 3));
                }
            }

            //deal with auras
            ObjectSet<Position> aura_positions =
                    getGameManager().getPositionGenerator().createPositionsWithinRange(target_x, target_y, 0, 2);

            for (Position target_position : aura_positions) {
                Unit target = getGame().getMap().getUnit(target_position);
                if (target != null) {
                    if (unit.hasAbility(Ability.ATTACK_AURA) && !getGame().isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.INSPIRED, 0));
                    }
                    if (unit.hasAbility(Ability.SLOWING_AURA) && !target.hasAbility(Ability.SLOWING_AURA)
                            && getGame().isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.SLOWED, 1));
                    }
                    if (unit.hasAbility(Ability.REFRESH_AURA) && getGame().canClean(unit, target)) {
                        target.clearStatus();
                    }
                }
            }
        } else {
            throw new CheatingException("standby check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canStandby(int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return target != null && !target.isStandby() && target.getCurrentHp() > 0;
    }

    private void onSummon(int summoner_x, int summoner_y, int target_x, int target_y) throws CheatingException {
        if (canSummon(summoner_x, summoner_y, target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Unit summoner = getGame().getMap().getUnit(summoner_x, summoner_y);
            getGame().getMap().removeTomb(target_x, target_y);
            getGame().createUnit(UnitFactory.getSkeletonIndex(), summoner.getTeam(), target_x, target_y);
            getAnimationDispatcher().submitSummonAnimation(summoner, target_x, target_y);
        } else {
            throw new CheatingException("summoning check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        Unit summoner = getGame().getMap().getUnit(summoner_x, summoner_y);
        return getGame().canSummon(summoner, target_x, target_y);
    }

    private void onTileDestroy(int target_x, int target_y) throws CheatingException {
        if (canDestroyTile(target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Tile tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(tile.getDestroyedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitDustAriseAnimation(target_x, target_y);
        } else {
            throw new CheatingException("tile destroying check failed", getGame().getCurrentTeam());
        }
    }

    private boolean canDestroyTile(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isDestroyable();
    }

    private void onUnitDestroy(int target_x, int target_y, int destroyer_team) throws CheatingException {
        if (canDestroyUnit(target_x, target_y)) {
            getGameManager().fireMapFocusEvent(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            if (destroyer_team >= 0) {
                getGame().getStatistics().addDestroy(destroyer_team, unit.getPrice());
            }
            getGame().destroyUnit(unit.getX(), unit.getY());
            getAnimationDispatcher().submitUnitDestroyAnimation(unit);
            getAnimationDispatcher().submitDustAriseAnimation(unit.getX(), unit.getY());

            onCheckTeamDestroy(unit.getTeam());
        } else {
            throw new CheatingException("unit destroying check failed", getGame().getCurrentTeam());
        }
    }

    private boolean canDestroyUnit(int target_x, int target_y) {
        return getGame().getMap().getUnit(target_x, target_y) != null;
    }

    private void onUnitGainExperience(int target_x, int target_y, int experience) throws CheatingException {
        Unit unit = getGame().getMap().getUnit(target_x, target_y);
        if (unit != null) {
            boolean level_up = unit.gainExperience(experience);
            if (level_up) {
                getAnimationDispatcher().submitUnitLevelUpAnimation(unit);
            }
        } else {
            throw new CheatingException("gaining experience check failed", getGame().getCurrentTeam());
        }
    }

    private void onHpChange(JSONArray changes) throws JSONException {
        if (changes.length() > 0) {
            ObjectMap<Position, Integer> change_map = new ObjectMap<Position, Integer>();
            ObjectSet<Unit> units = new ObjectSet<Unit>();
            for (int i = 0; i < changes.length(); i++) {
                JSONObject change = changes.getJSONObject(i);
                int x = change.getInt("x");
                int y = change.getInt("y");
                Position position = getGame().getMap().getPosition(x, y);
                Unit target = getGame().getMap().getUnit(position);
                if (target != null) {
                    target.changeCurrentHp(change.getInt("change"));
                    change_map.put(position, change.getInt("change"));
                    units.add(target);
                }
            }
            getAnimationDispatcher().submitHpChangeAnimation(change_map, units);
        }
    }

    private void onCheckTeamDestroy(int team) {
        Analyzer analyzer = new Analyzer(getGame());
        if (team >= 0 && analyzer.isTeamDestroyed(team)) {
            getGame().destroyTeam(team);
            int winner_alliance = analyzer.getWinnerAlliance();
            if (winner_alliance >= 0) {
                getGame().setGameOver(true);
            }
        }
    }

}
