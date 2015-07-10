package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * Created by toyknight on 7/7/2015.
 */
public class PhaseCancelEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 07072015;

    public static final int PHASE_MOVE = 0x1;
    public static final int PHASE_ACTION = 0x2;

    private final int phase;

    public PhaseCancelEvent(int phase) {
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
        switch (phase) {
            case PHASE_MOVE:
                if (manager.getState() == GameManager.STATE_MOVE) {
                    manager.setState(GameManager.STATE_SELECT);
                }
                break;
            case PHASE_ACTION:
                int state = manager.getState();
                if (state == GameManager.STATE_ATTACK || state == GameManager.STATE_SUMMON || state == GameManager.STATE_HEAL) {
                    manager.setState(GameManager.STATE_ACTION);
                }
                break;
            default:
                //do nothing
        }
    }

}
