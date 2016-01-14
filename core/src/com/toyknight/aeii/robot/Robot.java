package com.toyknight.aeii.robot;

import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 1/12/2016.
 */
public class Robot {

    private final GameManager manager;

    private final ObjectSet<String> target_units;

    private final Queue<Operation> operation_queue;

    private float current_delay = 0f;

    public Robot(GameManager manager) {
        this.manager = manager;
        this.target_units = new ObjectSet<String>();
        this.operation_queue = new LinkedList<Operation>();
    }

    public void reset() {
        target_units.clear();
    }

    public GameManager getManager() {
        return manager;
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    public boolean isOperating() {
        return operation_queue.size() > 0;
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

    public void calculate() {
        int current_team = getGame().getCurrentTeam();

        Unit commander = null;
        for (Point position : getGame().getMap().getUnitPositionSet()) {
            Unit unit = getGame().getMap().getUnit(position);
            if (unit.isCommander() && unit.getTeam() == current_team) {
                commander = unit;
                break;
            }
        }
        int target_x = getGame().getMap().getWidth() / 2;
        int target_y = getGame().getMap().getHeight() / 2;
        if (commander != null) {
            submitOperation(Operation.SELECT, commander.getX(), commander.getY());
            if (!commander.isAt(target_x, target_y)) {
                Point next_position =
                        getManager().getMovementGenerator().getNextPositionToTarget(commander, target_x, target_y);
                System.out.println(next_position.x + " " + next_position.y);
                if (getGame().getMap().isWithinMap(next_position.x, next_position.y)) {
                    submitOperation(Operation.MOVE, next_position.x, next_position.y);
                }
            }
            submitOperation(Operation.STANDBY);
        }
        submitOperation(Operation.END_TURN);
    }

    private void submitOperation(int type, int x, int y) {
        Operation operation = new Operation(type, x, y);
        operation_queue.add(operation);
    }

    private void submitOperation(int type) {
        submitOperation(type, -1, -1);
    }

    private boolean isTargetUnit(String unit_code) {
        return target_units.contains(unit_code);
    }

    private void executeOperation(Operation operation) {
        int map_x = operation.getMapX();
        int map_y = operation.getMapY();
        switch (operation.getType()) {
            case Operation.SELECT:
                getManager().doSelect(map_x, map_y);
                break;
            case Operation.MOVE:
                getManager().doMove(map_x, map_y);
                break;
            case Operation.ATTACK:
                getManager().doAttack(map_x, map_y);
                break;
            case Operation.HEAL:
                getManager().doHeal(map_x, map_y);
                break;
            case Operation.STANDBY:
                getManager().doStandbySelectedUnit();
                break;
            case Operation.SUMMON:
                getManager().doSummon(map_x, map_y);
                break;
            case Operation.END_TURN:
                getManager().doEndTurn();
                break;
            default:
                //do nothing
        }
    }


}
