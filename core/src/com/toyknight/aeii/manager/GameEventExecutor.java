package com.toyknight.aeii.manager;

import static com.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.record.GameRecorder;
import com.toyknight.aeii.entity.Rule;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 11/1/2015.
 */
public class GameEventExecutor {

    private static final String TAG = "Executor";

    private final GameManager game_manager;

    private final Queue<JSONObject> event_queue;

    private final Queue<JSONObject> buffer_event_queue;

    private final AnimationDispatcher animation_dispatcher;

    public GameEventExecutor(GameManager game_manager, AnimationDispatcher dispatcher) {
        this.game_manager = game_manager;
        this.animation_dispatcher = dispatcher;
        this.event_queue = new LinkedList<JSONObject>();
        this.buffer_event_queue = new LinkedList<JSONObject>();
    }

    public GameManager getGameManager() {
        return game_manager;
    }

    public GameCore getGame() {
        return getGameManager().getGame();
    }

    public UnitToolkit getUnitToolkit() {
        return getGameManager().getUnitToolkit();
    }

    public AnimationDispatcher getAnimationDispatcher() {
        return animation_dispatcher;
    }

    public void reset() {
        event_queue.clear();
        buffer_event_queue.clear();
    }

    public boolean isProcessing() {
        return event_queue.size() > 0 || buffer_event_queue.size() > 0;
    }

    public void submitGameEvent(JSONObject event) {
        event_queue.add(event);
    }

    private void submitBufferGameEvent(JSONObject event) {
        buffer_event_queue.add(event);
    }

    public void dispatchGameEvents() {
        if (getGame().isGameOver()) {
            getGameManager().onGameEventFinished();
        } else {
            if (buffer_event_queue.size() > 0) {
                try {
                    executeGameEvent(buffer_event_queue.poll(), false);
                } catch (JSONException ex) {
                    Gdx.app.log(TAG, ex.toString());
                }
                checkEventFinishing();
            } else {
                if (event_queue.size() > 0) {
                    executeGameEvent(event_queue.poll());
                    checkEventFinishing();
                }
            }
        }
    }

    private void checkEventFinishing() {
        if (event_queue.isEmpty() && buffer_event_queue.isEmpty()) {
            getGameManager().onGameEventFinished();
        }
    }

    public void executeGameEvent(JSONObject event) {
        try {
            executeGameEvent(event, true);
        } catch (JSONException ex) {
            Gdx.app.log(TAG, ex.toString());
        }
    }

    public void executeGameEvent(JSONObject event, boolean record) throws JSONException {
        switch (event.getInt("type")) {
            case GameEvent.ATTACK:
                int attacker_x = event.getJSONArray("parameters").getInt(0);
                int attacker_y = event.getJSONArray("parameters").getInt(1);
                int target_x = event.getJSONArray("parameters").getInt(2);
                int target_y = event.getJSONArray("parameters").getInt(3);
                int attack_damage = event.getJSONArray("parameters").getInt(4);
                int counter_damage = event.getJSONArray("parameters").getInt(5);
                onAttack(attacker_x, attacker_y, target_x, target_y, attack_damage, counter_damage);
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
                onMove(unit_x, unit_y, target_x, target_y);
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
                onUnitDestroy(target_x, target_y);
                break;
            case GameEvent.GAIN_EXPERIENCE:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                int experience = event.getJSONArray("parameters").getInt(2);
                onUnitGainExperience(target_x, target_y, experience);
                break;
            case GameEvent.ACTION_FINISH:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                onUnitActionFinish(target_x, target_y);
                break;
            case GameEvent.CHECK_TEAM_DESTROY:
                team = event.getJSONArray("parameters").getInt(0);
                onCheckTeamDestroy(team);
                break;
            default:
                //do nothing
        }
        if (record) {
            GameRecorder.submitGameEvent(event);
        }
    }

    private void onAttack(
            int attacker_x, int attacker_y, int target_x, int target_y, int attack_damage, int counter_damage)
            throws JSONException {
        if (canAttack(attacker_x, attacker_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            if (attacker != null) {
                if (defender == null) {
                    getAnimationDispatcher().submitUnitAttackAnimation(attacker, target_x, target_y);
                } else {
                    if (attack_damage >= 0) {
                        defender.changeCurrentHp(-attack_damage);
                        UnitToolkit.attachAttackStatus(attacker, defender);
                        getAnimationDispatcher().submitUnitAttackAnimation(attacker, defender, attack_damage);
                        if (defender.getCurrentHp() <= 0) {
                            getGame().getStatistics().addDestroy(attacker.getTeam(), defender.getPrice());
                            submitBufferGameEvent(GameEvent.create(GameEvent.UNIT_DESTROY, target_x, target_y));
                        }
                    }
                    if (counter_damage >= 0) {
                        attacker.changeCurrentHp(-counter_damage);
                        UnitToolkit.attachAttackStatus(defender, attacker);
                        getAnimationDispatcher().submitUnitAttackAnimation(defender, attacker, counter_damage);
                        if (attacker.getCurrentHp() <= 0) {
                            getGame().getStatistics().addDestroy(defender.getTeam(), attacker.getPrice());
                            submitBufferGameEvent(GameEvent.create(GameEvent.UNIT_DESTROY, attacker_x, attacker_y));
                        }
                    }
                }
            }
        }
    }

    private boolean canAttack(int attacker_x, int attacker_y, int target_x, int target_y) {
        Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
        Unit defender = getGame().getMap().getUnit(target_x, target_y);
        if (attacker == null) {
            return false;
        } else {
            if (defender == null) {
                Tile tile = getGame().getMap().getTile(target_x, target_y);
                return tile != null && tile.isDestroyable();
            } else {
                return getGame().isEnemy(attacker, defender);
            }
        }
    }

    private void onBuy(int index, int team, int target_x, int target_y) {
        if (canBuy(index, team)) {
            getGameManager().requestMapFocus(target_x, target_y);
            int price = getGame().getUnitPrice(index, team);
            if (index == UnitFactory.getCommanderIndex()) {
                getGame().restoreCommander(team, target_x, target_y);
            } else {
                getGame().createUnit(index, team, target_x, target_y);
            }
            getGame().getCurrentPlayer().changeGold(-price);
            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().setSelectedUnit(unit);

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().beginMovePhase();
            }
        }
    }

    private boolean canBuy(int index, int team) {
        return getGame().isPlayerAvailable(team)
                && getGame().getPlayer(team).getGold() >= getGame().getUnitPrice(index, team)
                && getGame().getPlayer(team).getPopulation() < getGame().getRule().getInteger(MAX_POPULATION);
    }

    private void onNextTurn() throws JSONException {
        getGameManager().setState(GameManager.STATE_SELECT);
        getGame().nextTurn();
        int team = getGame().getCurrentTeam();
        int income = getGame().gainIncome(team);
        getAnimationDispatcher().submitMessageAnimation(
                Language.getText("LB_CURRENT_TURN") + ": " + getGame().getCurrentTurn(),
                Language.getText("LB_INCOME") + ": " + income,
                0.8f);

        //calculate hp change at turn start
        JSONArray hp_changes = new JSONArray();

        //terrain heal and poison damage
        for (Unit unit : getGame().getMap().getUnits()) {
            int change = 0;
            if (unit.getTeam() == team) {
                //update status
                unit.updateStatus();
                getGame().resetUnit(unit);
                //the terrain heal
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                change = getUnitToolkit().getTerrainHeal(unit, tile);
                //the poison damage
                if (unit.hasStatus(Status.POISONED)) {
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
            } else {
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                if (getGame().isEnemy(unit.getTeam(), team) && tile.isCastle() && tile.getTeam() == team) {
                    change = -50;
                }
            }
            change = UnitToolkit.validateHpChange(unit, change);
            if (change != 0) {
                hp_changes.put(createHpChange(getGame().getMap().getPosition(unit), change));
            }
        }
        submitBufferGameEvent(GameEvent.create(GameEvent.HP_CHANGE, hp_changes));
    }

    private void onHeal(int healer_x, int healer_y, int target_x, int target_y, int heal) throws JSONException {
        if (canHeal(healer_x, healer_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit target = getGame().getMap().getUnit(target_x, target_y);
            target.changeCurrentHp(heal);
            getAnimationDispatcher().submitHpChangeAnimation(target, heal);
            if (target.getCurrentHp() <= 0) {
                submitBufferGameEvent(GameEvent.create(GameEvent.UNIT_DESTROY, target_x, target_y));
            }
        }
    }

    private boolean canHeal(int healer_x, int healer_y, int target_x, int target_y) {
        return getGame().getMap().getUnit(healer_x, healer_y) != null
                && getGame().getMap().getUnit(target_x, target_y) != null;
    }

    private void onMove(int unit_x, int unit_y, int target_x, int target_y) {
        if (canMove(unit_x, unit_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
            getGameManager().setSelectedUnit(unit);
            getGameManager().createMovablePositions();
            Array<Position> move_path = getGameManager().getMovePath(target_x, target_y);
            int movement_point =
                    getGameManager().getMovementGenerator().getMovementPointRemains(unit, target_x, target_y);

            getGame().moveUnit(unit_x, unit_y, target_x, target_y);
            unit.setCurrentMovementPoint(movement_point);
            getAnimationDispatcher().submitUnitMoveAnimation(unit, move_path);
            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().onUnitMoveFinish();
            }
        }
    }

    private boolean canMove(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        return unit != null && getGame().canUnitMove(unit, target_x, target_y);
    }

    private void onOccupy(int target_x, int target_y, int team) throws JSONException {
        if (canOccupy(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getCapturedTileIndex(team), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_OCCUPIED"), 0.5f);

            submitBufferGameEvent(GameEvent.create(GameEvent.CHECK_TEAM_DESTROY, target_tile.getTeam()));
        }
    }

    private boolean canOccupy(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isCapturable();
    }

    private void onRepair(int target_x, int target_y) {
        if (canRepair(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getRepairedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_REPAIRED"), 0.5f);

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                Unit unit = getGame().getMap().getUnit(target_x, target_y);
                getGameManager().onUnitActionFinish(unit);
            }
        }
    }

    private boolean canRepair(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isRepairable();
    }

    private void onReverse(int unit_x, int unit_y, int origin_x, int origin_y) {
        if (canReverse(unit_x, unit_y)) {
            getGameManager().requestMapFocus(origin_x, origin_y);

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
            if (getGame().getMap().canMove(origin_x, origin_y)) {
                getGame().getMap().moveUnit(unit, origin_x, origin_y);
            }
            unit.setCurrentMovementPoint(unit.getMovementPoint());

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().beginMovePhase();
            }
        }
    }

    private boolean canReverse(int unit_x, int unit_y) {
        return getGame().getMap().getUnit(unit_x, unit_y) != null;
    }

    private void onSelect(int target_x, int target_y) {
        if (canSelect(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().setSelectedUnit(unit);

            switch (getGame().getCurrentPlayer().getType()) {
                case Player.LOCAL:
                    Tile tile = getGame().getMap().getTile(target_x, target_y);
                    if (unit.isCommander() && getGame().isCastleAccessible(tile)) {
                        getGameManager().setState(GameManager.STATE_BUY);
                    } else {
                        getGameManager().beginMovePhase();
                    }
                    break;
                case Player.ROBOT:
                    getGameManager().beginMovePhase();
                    break;
                default:
                    //do nothing
            }
        }
    }

    private boolean canSelect(int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return target != null && getGame().getCurrentTeam() == target.getTeam() && !target.isStandby();
    }

    private void onStandby(int target_x, int target_y) throws JSONException {
        if (canStandby(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGame().standbyUnit(target_x, target_y);
            getGameManager().setState(GameManager.STATE_SELECT);

            //deal with auras
            ObjectSet<Position> aura_positions = getGameManager().createPositionsWithinRange(target_x, target_y, 0, 2);

            JSONArray hp_changes = new JSONArray();

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
                    if (unit.hasAbility(Ability.REFRESH_AURA)) {
                        int heal = Rule.REFRESH_BASE_HEAL + unit.getLevel() * 5;
                        if (getGame().canClean(unit, target)) {
                            target.clearStatus();
                        }
                        if (getGame().canHeal(unit, target)) {
                            int change = UnitToolkit.validateHpChange(target, heal);
                            if (change != 0) {
                                hp_changes.put(createHpChange(target_position, change));
                            }
                        }
                    }
                }
            }
            //deal with tombs
            if (getGame().getMap().isTomb(unit.getX(), unit.getY())) {
                getGame().getMap().removeTomb(unit.getX(), unit.getY());
                if (!unit.hasAbility(Ability.NECROMANCER)) {
                    unit.attachStatus(new Status(Status.POISONED, 3));
                }
            }
            submitBufferGameEvent(GameEvent.create(GameEvent.HP_CHANGE, hp_changes));
        }
    }

    private boolean canStandby(int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return target != null && !target.isStandby() && target.getCurrentHp() > 0;
    }

    private void onSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        if (canSummon(summoner_x, summoner_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit summoner = getGame().getMap().getUnit(summoner_x, summoner_y);
            getGame().getMap().removeTomb(target_x, target_y);
            getGame().createUnit(UnitFactory.getSkeletonIndex(), summoner.getTeam(), target_x, target_y);
            getAnimationDispatcher().submitSummonAnimation(summoner, target_x, target_y);
        }
    }

    private boolean canSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        Unit summoner = getGame().getMap().getUnit(summoner_x, summoner_y);
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return summoner != null && target == null && getGame().getMap().isTomb(target_x, target_y);
    }

    private void onTileDestroy(int target_x, int target_y) {
        if (canDestroyTile(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Tile tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(tile.getDestroyedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitDustAriseAnimation(target_x, target_y);
        }
    }

    private boolean canDestroyTile(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isDestroyable();
    }

    private void onUnitDestroy(int target_x, int target_y) throws JSONException {
        if (canDestroyUnit(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGame().destroyUnit(unit.getX(), unit.getY());
            getAnimationDispatcher().submitUnitDestroyAnimation(unit);
            getAnimationDispatcher().submitDustAriseAnimation(unit.getX(), unit.getY());

            submitBufferGameEvent(GameEvent.create(GameEvent.CHECK_TEAM_DESTROY, unit.getTeam()));
        }
    }

    private boolean canDestroyUnit(int target_x, int target_y) {
        return getGame().getMap().getUnit(target_x, target_y) != null;
    }

    private void onUnitGainExperience(int target_x, int target_y, int experience) {
        Unit unit = getGame().getMap().getUnit(target_x, target_y);
        if (unit != null) {
            boolean level_up = unit.gainExperience(experience);
            if (level_up) {
                getAnimationDispatcher().submitUnitLevelUpAnimation(unit);
            }
        }
    }

    private void onUnitActionFinish(int target_x, int target_y) {
        if (getGame().getCurrentPlayer().isLocalPlayer()) {
            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().onUnitActionFinish(unit);
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
                    if (target.getCurrentHp() <= 0) {
                        submitBufferGameEvent(GameEvent.create(GameEvent.UNIT_DESTROY, target.getX(), target.getY()));
                    }
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

    private JSONObject createHpChange(Position position, int change) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("change", change);
        return json;
    }

}
