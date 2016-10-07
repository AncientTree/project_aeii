package net.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.TileValidator;
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

    private final GameManager manager;

    private final Queue<JSONObject> event_queue;

    private boolean check_event_value = false;

    public GameEventExecutor(GameManager manager) {
        this.manager = manager;
        this.event_queue = new LinkedList<JSONObject>();
    }

    public GameManager getManager() {
        return manager;
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    public AnimationDispatcher getAnimationDispatcher() {
        return getManager().getAnimationDispatcher();
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
    }

    public void dispatchGameEvents() throws CheatingException {
        if (getGame().isGameOver()) {
            getManager().onGameEventFinished();
        } else {
            if (event_queue.size() > 0) {
                try {
                    JSONObject event = event_queue.poll();
                    if (event.getInt("type") >= 0x16 && getGame().getType() != GameCore.CAMPAIGN) {
                        throw new CheatingException("Invalid game event!", getGame().getCurrentTeam());
                    } else {
                        executeGameEvent(event);
                        getManager().onGameEventExecuted(event);
                    }
                } catch (JSONException ex) {
                    throw new CheatingException("Invalid game event!", getGame().getCurrentTeam());
                }
                if (event_queue.isEmpty()) {
                    getManager().onGameEventFinished();
                }
            }
        }
    }

    public void executeGameEvent(JSONObject event) throws JSONException, CheatingException {
        switch (event.getInt("type")) {
            case GameEvent.STANDBY_FINISH:
                int target_x = event.getJSONArray("parameters").getInt(0);
                int target_y = event.getJSONArray("parameters").getInt(1);
                getManager().fireUnitStandbyEvent(target_x, target_y);
                getManager().fireStateChangeEvent();
                break;
            case GameEvent.CHECK_UNIT_DESTROY:
                int team = event.getJSONArray("parameters").getInt(0);
                onCheckUnitDestroy(team);
                break;
            case GameEvent.MANAGER_STATE_SYNC:
                int manager_state = event.getJSONArray("parameters").getInt(0);
                getManager().syncState(manager_state, -1, -1);
                break;
            case GameEvent.ATTACK:
                int attacker_x = event.getJSONArray("parameters").getInt(0);
                int attacker_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                int attack_damage = event.getJSONArray("parameters").getInt(4);
                boolean counter = event.getJSONArray("parameters").getBoolean(5);
                onAttack(attacker_x, attacker_y, target_x, target_y, attack_damage, counter);
                break;
            case GameEvent.BUY:
                int index = event.getJSONArray("parameters").getInt(0);
                team = event.getJSONArray("parameters").getInt(1);
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
            case GameEvent.CAMPAIGN_REINFORCE:
                team = event.getJSONArray("parameters").getInt(0);
                int from_x = event.getJSONArray("parameters").getInt(1);
                int from_y = event.getJSONArray("parameters").getInt(2);
                JSONArray reinforcements = event.getJSONArray("parameters").getJSONArray(3);
                onCampaignReinforce(team, from_x, from_y, reinforcements);
                break;
            case GameEvent.CAMPAIGN_MESSAGE:
                JSONArray messages = event.getJSONArray("parameters").getJSONArray(0);
                onCampaignMessage(messages);
                break;
            case GameEvent.CAMPAIGN_ATTACK:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                attack_damage = event.getJSONArray("parameters").getInt(2);
                onCampaignAttack(target_x, target_y, attack_damage);
                break;
            case GameEvent.CAMPAIGN_FOCUS:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                getManager().fireMapFocusEvent(target_x, target_y, true);
                break;
            case GameEvent.CAMPAIGN_CLEAR:
                getManager().getGame().setGameOver(true);
                getManager().getContext().getCampaignContext().getCurrentCampaign().getCurrentStage().setCleared(true);
                getManager().getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_STAGE_CLEAR"), 1.0f);
                break;
            case GameEvent.CAMPAIGN_FAIL:
                getManager().getGame().setGameOver(true);
                getManager().getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_STAGE_FAIL"), 1.0f);
                break;
            case GameEvent.CAMPAIGN_CRYSTAL_STEAL:
                int map_x = event.getJSONArray("parameters").getInt(0);
                int map_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                onCampaignCrystalSteal(map_x, map_y, target_x, target_y);
                break;
            case GameEvent.CAMPAIGN_CREATE_UNIT:
                index = event.getJSONArray("parameters").getInt(0);
                team = event.getJSONArray("parameters").getInt(1);
                map_x = event.getJSONArray("parameters").getInt(2);
                map_y = event.getJSONArray("parameters").getInt(3);
                onCampaignCreateUnit(index, team, map_x, map_y);
                break;
            case GameEvent.CAMPAIGN_MOVE_UNIT:
                unit_x = event.getJSONArray("parameters").getInt(0);
                unit_y = event.getJSONArray("parameters").getInt(1);
                target_x = event.getJSONArray("parameters").getInt(2);
                target_y = event.getJSONArray("parameters").getInt(3);
                onCampaignMoveUnit(unit_x, unit_y, target_x, target_y);
                break;
            case GameEvent.CAMPAIGN_REMOVE_UNIT:
                unit_x = event.getJSONArray("parameters").getInt(0);
                unit_y = event.getJSONArray("parameters").getInt(1);
                onCampaignRemoveUnit(unit_x, unit_y);
                break;
            case GameEvent.CAMPAIGN_CHANGE_TEAM:
                unit_x = event.getJSONArray("parameters").getInt(0);
                unit_y = event.getJSONArray("parameters").getInt(1);
                team = event.getJSONArray("parameters").getInt(2);
                onCampaignChangeTeam(unit_x, unit_y, team);
                break;
            case GameEvent.CAMPAIGN_FLY_OVER:
                index = event.getJSONArray("parameters").getInt(0);
                team = event.getJSONArray("parameters").getInt(1);
                int start_x = event.getJSONArray("parameters").getInt(2);
                int start_y = event.getJSONArray("parameters").getInt(3);
                target_x = event.getJSONArray("parameters").getInt(4);
                target_y = event.getJSONArray("parameters").getInt(5);
                onCampaignFlyOver(index, team, start_x, start_y, target_x, target_y);
                break;
            case GameEvent.CAMPAIGN_CARRY_UNIT:
                int carrier_x = event.getJSONArray("parameters").getInt(0);
                int carrier_y = event.getJSONArray("parameters").getInt(1);
                int target_index = event.getJSONArray("parameters").getInt(2);
                int target_team = event.getJSONArray("parameters").getInt(3);
                int dest_x = event.getJSONArray("parameters").getInt(4);
                int dest_y = event.getJSONArray("parameters").getInt(5);
                onCampaignCarryUnit(carrier_x, carrier_y, target_index, target_team, dest_x, dest_y);
                break;
            case GameEvent.CAMPAIGN_SHOW_OBJECTIVES:
                getManager().fireCampaignObjectiveRequestEvent();
                break;
            case GameEvent.CAMPAIGN_HAVENS_FURY:
                team = event.getJSONArray("parameters").getInt(0);
                target_x = event.getJSONArray("parameters").getInt(1);
                target_y = event.getJSONArray("parameters").getInt(2);
                int damage = event.getJSONArray("parameters").getInt(3);
                onCampaignHavensFury(team, target_x, target_y, damage);
                break;
            case GameEvent.CAMPAIGN_TILE_DESTROY:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                short destroyed_index = (short) event.getJSONArray("parameters").getInt(2);
                onCampaignTileDestroy(target_x, target_y, destroyed_index);
                break;
            case GameEvent.CAMPAIGN_NOTIFICATION:
                JSONArray notifications = event.getJSONArray("parameters").getJSONArray(0);
                onCampaignNotification(notifications);
                break;
            case GameEvent.CAMPAIGN_LEVEL_UP:
                target_x = event.getJSONArray("parameters").getInt(0);
                target_y = event.getJSONArray("parameters").getInt(1);
                onCampaignLevelUp(target_x, target_y);
            default:
                //do nothing
        }
    }

    private void onCheckUnitDestroy(int team) {
        ObjectSet<Unit> destroyed_units = new ObjectSet<Unit>();
        ObjectSet<Position> destroy_positions = new ObjectSet<Position>();
        for (Unit unit : getGame().getMap().getUnits().toArray()) {
            if ((team < 0 || unit.getTeam() == team) && unit.getCurrentHp() <= 0) {
                destroyed_units.add(unit);
                destroy_positions.add(getGame().getMap().getPosition(unit));
                getGame().destroyUnit(unit.getX(), unit.getY());
            }
        }
        if (destroyed_units.size > 0 && destroy_positions.size > 0) {
            getManager().getAnimationDispatcher().submitUnitSparkAnimation(destroyed_units);
            getManager().getAnimationDispatcher().submitDustAriseAnimation(destroy_positions);
            for (Unit unit : destroyed_units) {
                getManager().fireUnitDestroyEvent(unit);
            }
        }
    }

    private void onAttack(int attacker_x, int attacker_y, int target_x, int target_y, int attack_damage, boolean counter)
            throws CheatingException {
        if (canAttack(attacker_x, attacker_y, target_x, target_y, attack_damage)) {
            getManager().fireMapFocusEvent(target_x, target_y, false);

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
            if (!counter) {
                getManager().fireAttackEvent(attacker, defender);
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
                int expected_damage = getManager().getUnitToolkit().getDamage(attacker, defender, false);
                return base_check && Math.abs(attack_damage - expected_damage) <= 2;
            } else {
                return base_check;
            }
        } else {
            return base_check;
        }
    }

    private void onBuy(int index, int team, int target_x, int target_y) throws CheatingException {
        if (canBuy(index, team, target_x, target_y)) {
            getManager().fireMapFocusEvent(target_x, target_y, false);
            int price = getGame().getUnitPrice(index, team);
            getGame().getCurrentPlayer().changeGold(-price);

            if (UnitFactory.isCommander(index)) {
                getGame().restoreCommander(team, target_x, target_y);
            } else {
                getGame().createUnit(index, team, target_x, target_y);
            }
        } else {
            throw new CheatingException("buying check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canBuy(int index, int team, int map_x, int map_y) {
        if (getGame().getCurrentPlayer().getType() == Player.REMOTE && check_event_value) {
            return getManager().canBuy(index, team, map_x, map_y);
        } else {
            return getGame().canBuy(index, team);
        }
    }

    private void onNextTurn() throws JSONException {
        getGame().nextTurn();
        for (Unit unit : getGame().getMap().getUnits()) {
            if (unit.getTeam() == getGame().getCurrentTeam()) {
                if (getGame().getMap().getTile(unit).isTemple() && Status.isDebuff(unit.getStatus())) {
                    unit.clearStatus();
                } else {
                    unit.updateStatus();
                }
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
            getManager().fireMapFocusEvent(target_x, target_y, false);

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
            getManager().fireMapFocusEvent(target_x, target_y, false);

            Array<Position> path = new Array<Position>();
            for (int i = 0; i < move_path.length(); i++) {
                JSONObject step = move_path.getJSONObject(i);
                path.add(getGame().getMap().getPosition(step.getInt("x"), step.getInt("y")));
            }

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);

            getGame().moveUnit(unit_x, unit_y, target_x, target_y);
            unit.setCurrentMovementPoint(movement_point);
            getAnimationDispatcher().submitUnitMoveAnimation(unit, path);

            getManager().fireMoveEvent(unit, target_x, target_y);
        } else {
            throw new CheatingException("moving check failed!", getGame().getCurrentTeam());
        }
    }

    private boolean canMove(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        Position destination = getGame().getMap().getPosition(target_x, target_y);
        boolean base_check = unit != null && getGame().canUnitMove(unit, target_x, target_y);
        if (getGame().getCurrentPlayer().getType() == Player.REMOTE && check_event_value) {
            ObjectSet<Position> movable_positions = getManager().getPositionGenerator().createMovablePositions(unit);
            return base_check && movable_positions.contains(destination);
        } else {
            return base_check;
        }
    }

    private void onOccupy(int target_x, int target_y, int team) throws CheatingException {
        if (canOccupy(target_x, target_y)) {
            getManager().fireMapFocusEvent(target_x, target_y, false);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getCapturedTileIndex(team), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_OCCUPIED"), 0.5f);

            onCheckTeamDestroy(target_tile.getTeam());

            getManager().fireOccupyEvent(target_x, target_y, team);
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
            getManager().fireMapFocusEvent(target_x, target_y, false);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getRepairedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_REPAIRED"), 0.5f);

            getManager().fireRepairEvent(target_x, target_y);
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
            getManager().fireMapFocusEvent(origin_x, origin_y, false);

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
            getManager().fireMapFocusEvent(target_x, target_y, false);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getManager().setSelectedUnit(unit);
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
            getManager().fireMapFocusEvent(target_x, target_y, false);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGame().standbyUnit(target_x, target_y);

            //deal with tombs
            if (getGame().getMap().isTomb(unit.getX(), unit.getY())) {
                getGame().getMap().removeTomb(unit.getX(), unit.getY());
                if (!unit.hasAbility(Ability.NECROMANCER)) {
                    unit.attachStatus(new Status(Status.POISONED, 1));
                }
            }

            //deal with auras
            ObjectSet<Position> aura_positions =
                    getManager().getPositionGenerator().createPositionsWithinRange(target_x, target_y, 0, 2);

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
            getManager().fireMapFocusEvent(target_x, target_y, false);

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
            getManager().fireMapFocusEvent(target_x, target_y, false);

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
            getManager().fireMapFocusEvent(target_x, target_y, false);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            if (destroyer_team >= 0) {
                getGame().getStatistics().addDestroy(destroyer_team, unit.getPrice());
            }
            getGame().destroyUnit(unit.getX(), unit.getY());
            getAnimationDispatcher().submitUnitSparkAnimation(unit);
            getAnimationDispatcher().submitDustAriseAnimation(unit.getX(), unit.getY());

            onCheckTeamDestroy(unit.getTeam());

            getManager().fireUnitDestroyEvent(unit);
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

    private void onCampaignReinforce(int team, int from_x, int from_y, JSONArray reinforcements) throws JSONException {
        Array<Unit> reinforcement_units = new Array<Unit>();
        for (int i = 0; i < reinforcements.length(); i++) {
            JSONObject reinforcement = reinforcements.getJSONObject(i);
            int index = reinforcement.getInt("index");
            int head = reinforcement.getInt("head");
            int map_x = reinforcement.getInt("x");
            int map_y = reinforcement.getInt("y");
            if (getGame().getMap().getUnit(map_x, map_y) == null) {
                if (UnitFactory.isCommander(index)) {
                    if (getGame().isCommanderAlive(team)) {
                        getGame().createUnit(index, team, map_x, map_y);
                    } else {
                        getGame().restoreCommander(team, map_x, map_y);
                    }
                } else {
                    getGame().createUnit(index, team, map_x, map_y);
                }
                if (head >= 0) {
                    getGame().getMap().getUnit(map_x, map_y).setHead(head);
                }
                reinforcement_units.add(getGame().getMap().getUnit(map_x, map_y));
            }
        }
        if (reinforcement_units.size > 0) {
            Unit first_unit = reinforcement_units.first();
            getManager().fireMapFocusEvent(first_unit.getX(), first_unit.getY(), true);
            getAnimationDispatcher().submitReinforceAnimation(reinforcement_units, from_x, from_y);
        }
    }

    private void onCampaignMessage(JSONArray messages) throws JSONException {
        for (int i = 0; i < messages.length(); i++) {
            JSONObject json = messages.getJSONObject(i);
            Message message = new Message(json.getInt("portrait"), json.getString("message"));
            getManager().submitCampaignMessage(message);
        }
        getManager().fireCampaignMessageSubmitEvent();
    }

    private void onCampaignAttack(int target_x, int target_y, int damage) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (target == null) {
            getAnimationDispatcher().submitUnitAttackAnimation(target_x, target_y);
        } else {
            getAnimationDispatcher().submitUnitAttackAnimation(target, damage);
        }
    }

    private void onCampaignCrystalSteal(int map_x, int map_y, int target_x, int target_y) {
        getManager().getAnimationDispatcher().submitCrystalStealAnimation(map_x, map_y, target_x, target_y);
        while (map_x != target_x) {
            map_x = map_x < target_x ? map_x + 1 : map_x - 1;
            addCrystalStealUnit(map_x, map_y, target_x, target_y);
        }
        while (map_y != target_y) {
            map_y = map_y < target_y ? map_y + 1 : map_y - 1;
            addCrystalStealUnit(map_x, map_y, target_x, target_y);
        }
    }

    private void addCrystalStealUnit(int map_x, int map_y, int target_x, int target_y) {
        if (getGame().getMap().isWithinMap(map_x, map_y)) {
            if (UnitToolkit.getRange(map_x, map_y, target_x, target_y) == 0) {
                getGame().createUnit(0, 1, map_x, map_y);
            }
            if (UnitToolkit.getRange(map_x, map_y, target_x, target_y) == 1) {
                getGame().createUnit(UnitFactory.getCrystalIndex(), 1, map_x, map_y);
            }
            if (UnitToolkit.getRange(map_x, map_y, target_x, target_y) == 2) {
                getGame().createUnit(0, 1, map_x, map_y);
            }
        }
    }

    private void onCampaignCreateUnit(int index, int team, int map_x, int map_y) {
        if (getGame().getMap().getUnit(map_x, map_y) == null) {
            if (UnitFactory.isCommander(index)) {
                getGame().restoreCommander(team, map_x, map_y);
            } else {
                getGame().createUnit(index, team, map_x, map_y);
            }
        }
    }

    private void onCampaignMoveUnit(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (unit != null) {
            getGame().moveUnit(unit_x, unit_y, target_x, target_y);
            Array<Position> move_path = new Array<Position>();
            move_path.add(getGame().getMap().getPosition(unit_x, unit_y));
            while (unit_x != target_x) {
                unit_x = unit_x < target_x ? unit_x + 1 : unit_x - 1;
                move_path.add(getGame().getMap().getPosition(unit_x, unit_y));
            }
            while (unit_y != target_y) {
                unit_y = unit_y < target_y ? unit_y + 1 : unit_y - 1;
                move_path.add(getGame().getMap().getPosition(unit_x, unit_y));
            }
            getAnimationDispatcher().submitUnitMoveAnimation(unit, move_path);
        }
    }

    private void onCampaignRemoveUnit(int unit_x, int unit_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (unit != null) {
            getGame().getMap().removeUnit(unit_x, unit_y);
            getGame().updatePopulation(unit.getTeam());
        }
    }

    private void onCampaignChangeTeam(int unit_x, int unit_y, int team) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        if (unit != null) {
            getManager().fireMapFocusEvent(unit_x, unit_y, false);
            unit.setTeam(team);
            getAnimationDispatcher().submitUnitSparkAnimation(unit);
        }
    }

    private void onCampaignFlyOver(int index, int team, int start_x, int start_y, int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        if (target != null) {
            getGame().getMap().removeUnit(target_x, target_y);
            Unit flier = getGame().createUnit(index, team, target_x, target_y);
            getAnimationDispatcher().submitFlyOverAnimation(flier, target, start_x, start_y);
        }
    }

    private void onCampaignCarryUnit(
            int carrier_x, int carrier_y, int target_index, int target_team, int dest_x, int dest_y) {
        Unit carrier = getGame().getMap().getUnit(carrier_x, carrier_y);
        if (carrier != null) {
            Unit target = UnitFactory.createUnit(target_index, target_team);
            getGame().getMap().removeUnit(carrier_x, carrier_y);
            getGame().updatePopulation(carrier.getTeam());
            getAnimationDispatcher().submitUnitCarryAnimation(carrier, target, dest_x, dest_y);
        }
    }

    private void onCampaignHavensFury(int team, int target_x, int target_y, int damage) {
        Unit target;
        if ((target = getGame().getMap().getUnit(target_x, target_y)) == null) {
            ObjectSet<Unit> units = getGame().getMap().getUnits(team);
            int max_price = Integer.MIN_VALUE;
            int max_hp = Integer.MIN_VALUE;
            for (Unit unit : units) {
                if (!unit.isCommander()) {
                    if (unit.getCurrentHp() > max_hp
                            || (unit.getCurrentHp() == max_hp && unit.getPrice() > max_price)) {
                        target = unit;
                        max_price = unit.getPrice();
                        max_hp = unit.getCurrentHp();
                    }
                }
            }
            if (target == null && units.size > 0) {
                target = units.first();
            }
        }
        if (target != null) {
            getManager().fireMapFocusEvent(target.getX(), target.getY(), true);
            getAnimationDispatcher().submitHavensFuryAnimation(target);
            if (damage != 0) {
                onHpChange(target, -damage);
            }
        }
    }

    private void onCampaignTileDestroy(int target_x, int target_y, short destroyed_index) {
        if (getGame().getMap().isWithinMap(target_x, target_y)) {
            getManager().fireMapFocusEvent(target_x, target_y, false);

            getGame().setTile(destroyed_index, target_x, target_y);
            TileValidator.validate(getGame().getMap(), target_x, target_y);
            getAnimationDispatcher().submitDustAriseAnimation(target_x, target_y);
        }
    }

    private void onCampaignNotification(JSONArray notifications) {
        for (int i = 0; i < notifications.length(); i++) {
            String message = notifications.getString(i);
            getAnimationDispatcher().submitMessageAnimation(message, 1f);
        }
    }

    public void onCampaignLevelUp(int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(target_x, target_y);
        if (unit != null && unit.getLevel() < 3) {
            getManager().fireMapFocusEvent(target_x, target_y, false);
            unit.gainExperience(unit.getLevelUpExperience() - unit.getCurrentExperience());
            getAnimationDispatcher().submitUnitLevelUpAnimation(unit);
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

    private void onHpChange(Unit target, int change) {
        if (target != null) {
            JSONArray hp_changes = new JSONArray();
            JSONObject hp_change = new JSONObject();
            change = UnitToolkit.validateHpChange(target, change);
            hp_change.put("x", target.getX());
            hp_change.put("y", target.getY());
            hp_change.put("change", change);
            hp_changes.put(hp_change);
            onHpChange(hp_changes);
        }
    }

    private void onCheckTeamDestroy(int team) {
        if (getGame().getType() == GameCore.SKIRMISH) {
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

}
