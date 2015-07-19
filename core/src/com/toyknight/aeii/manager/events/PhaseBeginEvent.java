package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * Created by toyknight on 7/7/2015.
 */
public class PhaseBeginEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 07072015;

    public static final int PHASE_MOVE = 0x1;
    public static final int PHASE_REMOVE = 0x2;
    public static final int PHASE_ATTACK = 0x3;
    public static final int PHASE_SUMMON = 0x4;
    public static final int PHASE_HEAL = 0x5;

    private final int phase;

    public PhaseBeginEvent(int phase) {
        this.phase = phase;
    }

    @Override
    public Point getFocus() {
        return null;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return true;
    }

    @Override
    public void execute(GameManager manager) {
        GameCore game = manager.getGame();
        Unit selected_unit = manager.getSelectedUnit();
        switch (phase) {
            case PHASE_MOVE:
                if (game.isUnitAccessible(selected_unit)) {
                    manager.createMovablePositions();
                    manager.setState(GameManager.STATE_MOVE);
                }
                break;
            case PHASE_REMOVE:
                if (manager.isActionPhase()) {
                    manager.createMovablePositions();
                    manager.setState(GameManager.STATE_REMOVE);
                }
                break;
            case PHASE_ATTACK:
                if (game.isUnitAccessible(selected_unit) && manager.getState() == GameManager.STATE_ACTION) {
                    manager.createAttackablePositions(selected_unit);
                    manager.setState(GameManager.STATE_ATTACK);
                }
                break;
            case PHASE_SUMMON:
                if (game.isUnitAccessible(selected_unit) && manager.getState() == GameManager.STATE_ACTION) {
                    manager.createAttackablePositions(selected_unit);
                    manager.setState(GameManager.STATE_SUMMON);
                }
                break;
            case PHASE_HEAL:
                if (game.isUnitAccessible(selected_unit) && manager.getState() == GameManager.STATE_ACTION) {
                    manager.createAttackablePositions(selected_unit);
                    manager.setState(GameManager.STATE_HEAL);
                }
                break;
            default:
                //do nothing
        }
    }
}
