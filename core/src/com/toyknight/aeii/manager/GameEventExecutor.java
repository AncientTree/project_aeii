package com.toyknight.aeii.manager;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.record.Recorder;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 11/1/2015.
 */
public class GameEventExecutor {

    private final GameManager game_manager;

    private final Queue<GameEvent> event_queue;

    private final AnimationDispatcher animation_dispatcher;

    public GameEventExecutor(GameManager game_manager, AnimationDispatcher dispatcher) {
        this.game_manager = game_manager;
        this.animation_dispatcher = dispatcher;
        this.event_queue = new LinkedList<GameEvent>();
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

    public void clearGameEvents() {
        event_queue.clear();
    }

    public boolean isProcessing() {
        return event_queue.size() > 0;
    }

    public void submitGameEvent(GameEvent event) {
        event_queue.add(event);
    }

    public void dispatchGameEvents() {
        if (event_queue.size() > 0) {
            executeGameEvent(event_queue.poll());
        }
    }

    public void executeGameEvent(GameEvent event) {
        executeGameEvent(event, true);
    }

    public void executeGameEvent(GameEvent event, boolean record) {
        switch (event.getType()) {
            case GameEvent.ATTACK:
                int attacker_x = (Integer) event.getParameter(0);
                int attacker_y = (Integer) event.getParameter(1);
                int target_x = (Integer) event.getParameter(2);
                int target_y = (Integer) event.getParameter(3);
                int damage = (Integer) event.getParameter(4);
                int experience = (Integer) event.getParameter(5);
                onAttack(attacker_x, attacker_y, target_x, target_y, damage, experience);
                break;
            case GameEvent.BUY:
                int index = (Integer) event.getParameter(0);
                int team = (Integer) event.getParameter(1);
                target_x = (Integer) event.getParameter(2);
                target_y = (Integer) event.getParameter(3);
                int price = (Integer) event.getParameter(4);
                onBuy(index, team, target_x, target_y, price);
                break;
            case GameEvent.END_TURN:
                break;
            case GameEvent.HEAL:
                break;
            case GameEvent.MOVE:
                break;
            case GameEvent.OCCUPY:
                break;
            case GameEvent.REPAIR:
                break;
            case GameEvent.REVERSE:
                break;
            case GameEvent.SELECT:
                break;
            case GameEvent.STANDBY:
                break;
            case GameEvent.SUMMON:
                break;
            case GameEvent.TILE_DESTROY:
                break;
            case GameEvent.UNIT_DESTROY:
                break;
            default:
                //do nothing
        }
        if (record) {
            Recorder.submitGameEvent(event);
        }
    }

    private void onAttack(int attacker_x, int attacker_y, int target_x, int target_y, int damage, int experience) {
        if (canAttack(attacker_x, attacker_y, target_x, target_y)) {
            getGameManager().requestFocus(target_x, target_y);

            Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            if (attacker != null) {
                if (defender == null) {
                    getAnimationDispatcher().submitUnitAttackAnimation(attacker, target_x, target_y);
                } else {
                    defender.changeCurrentHp(-damage);
                    UnitToolkit.attachAttackStatus(attacker, defender);
                    getAnimationDispatcher().submitUnitAttackAnimation(attacker, defender, damage);
                    if (defender.getCurrentHp() <= 0) {
                        //update statistics
                        getGame().getStatistics().addDestroy(attacker.getTeam(), defender.getPrice());
                        //destroy defender
                        getGame().destroyUnit(defender.getX(), defender.getY());
                        //submit animation
                        getAnimationDispatcher().submitUnitDestroyAnimation(defender);
                        getAnimationDispatcher().submitDustAriseAnimation(defender.getX(), defender.getY());
                        getGame().updateGameStatus();
                    }
                }
                boolean level_up = attacker.gainExperience(experience);
                if (level_up) {
                    getAnimationDispatcher().submitUnitLevelUpAnimation(attacker);
                }

                if (getGame().getCurrentPlayer().isLocalPlayer()) {
                    if (defender == null) {
                        getGameManager().onUnitActionFinished(attacker);
                    } else {
                        if (attacker.getTeam() == getGame().getCurrentTeam() &&
                                (!getUnitToolkit().canCounter(defender, attacker) || defender.getCurrentHp() <= 0)) {
                            getGameManager().onUnitActionFinished(attacker);
                        }
                        if (attacker.getTeam() != getGame().getCurrentTeam()) {
                            getGameManager().onUnitActionFinished(defender);
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

    private void onBuy(int index, int team, int target_x, int target_y, int price) {
        if (canBuy(team, price)) {
            getGameManager().requestFocus(target_x, target_y);

            if (index == UnitFactory.getCommanderIndex()) {
                getGame().restoreCommander(team, target_x, target_y);
            } else {
                getGame().createUnit(index, team, target_x, target_y);
            }
            getGame().getCurrentPlayer().changeGold(-price);
            getGameManager().setSelectedUnit(getGame().getMap().getUnit(target_x, target_y));

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().beginMovePhase();
            }
        }
    }

    private boolean canBuy(int team, int price) {
        return getGame().isPlayerAvailable(team) && getGame().getPlayer(team).getGold() > price;
    }

}
