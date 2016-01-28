package com.toyknight.aeii.robot;

import com.toyknight.aeii.manager.GameManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 1/28/2016.
 */
public class OperationExecutor {

    private final GameManager manager;

    private final Queue<Operation> operation_queue;

    private float current_delay;

    public OperationExecutor(GameManager manager) {
        this.manager = manager;
        this.operation_queue = new LinkedList<Operation>();
    }

    public GameManager getManager() {
        return manager;
    }

    public void operate(float delta) {
        if (isOperating()) {
            if (current_delay < 0.25f) {
                current_delay += delta;
            } else {
                current_delay = 0f;
                executeOperation(operation_queue.poll());
            }
        }
    }

    public void reset() {
        operation_queue.clear();
        current_delay = 0f;
    }

    public boolean isOperating() {
        return operation_queue.size() > 0;
    }

    public void submitOperation(int type, int... parameters) {
        Operation operation = new Operation(type, parameters);
        operation_queue.add(operation);
    }

    private void executeOperation(Operation operation) {
        int map_x;
        int map_y;
        int unit_index;
        switch (operation.getType()) {
            case Operation.SELECT:
                map_x = operation.getParameter(0);
                map_y = operation.getParameter(1);
                getManager().doSelect(map_x, map_y);
                break;
            case Operation.MOVE:
                map_x = operation.getParameter(0);
                map_y = operation.getParameter(1);
                getManager().doMove(map_x, map_y);
                break;
            case Operation.ATTACK:
                map_x = operation.getParameter(0);
                map_y = operation.getParameter(1);
                getManager().doAttack(map_x, map_y);
                break;
            case Operation.HEAL:
                map_x = operation.getParameter(0);
                map_y = operation.getParameter(1);
                getManager().doHeal(map_x, map_y);
                break;
            case Operation.STANDBY:
                getManager().doStandbySelectedUnit();
                break;
            case Operation.SUMMON:
                map_x = operation.getParameter(0);
                map_y = operation.getParameter(1);
                getManager().doSummon(map_x, map_y);
                break;
            case Operation.END_TURN:
                getManager().doEndTurn();
                break;
            case Operation.BUY:
                unit_index = operation.getParameter(0);
                map_x = operation.getParameter(1);
                map_y = operation.getParameter(2);
                getManager().doBuyUnit(unit_index, map_x, map_y);
                break;
            default:
                //do nothing
        }
    }

}
