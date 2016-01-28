package com.toyknight.aeii.robot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Position;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.Random;

/**
 * @author toyknight 1/12/2016.
 */
public class Robot {

    private final int team;

    private final Random random;

    private final GameManager manager;

    private final ObjectSet<String> target_units;

    private final Runnable calculate_task = new Runnable() {
        @Override
        public void run() {
            calculating = true;
            doCalculate();
            calculating = false;
        }
    };

    private boolean calculating;

    public Robot(GameManager manager, int team) {
        this.team = team;
        this.manager = manager;
        this.random = new Random();
        this.target_units = new ObjectSet<String>();
    }

    public void reset() {
        calculating = false;
        target_units.clear();
        //TODO: Add target units from rule.
    }

    public GameManager getManager() {
        return manager;
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    public int getTeam() {
        return team;
    }

    public boolean isCalculating() {
        return calculating;
    }

    public void calculate() {
        if (!isCalculating()) {
            new Thread(calculate_task, "robot-thread").start();
        }
    }

    private void doCalculate() {
        int unit_to_buy = calculateUnitToBuy();
        Position buying_position = getUnitBuyingPosition(unit_to_buy);
        if (unit_to_buy >= 0 && buying_position != null) {
            getManager().getOperationExecutor().submitOperation(
                    Operation.BUY, unit_to_buy, buying_position.x, buying_position.y);
            doUnitAction(getBoughtUnit(unit_to_buy, buying_position.x, buying_position.y));
        } else {
            Unit next_unit = getNextUnit(getTeam());
            if (next_unit == null) {
                getManager().getOperationExecutor().submitOperation(Operation.END_TURN);
            } else {
                doUnitAction(next_unit);
            }
        }
    }

    private void doUnitAction(Unit unit) {
        ObjectSet<Position> movable_positions = getManager().getMovementGenerator().createMovablePositions(unit);
        for (Position position : movable_positions) {
            if (UnitToolkit.getRange(unit.getX(), unit.getY(), position.x, position.y) >= 0) {
                getManager().getOperationExecutor().submitOperation(Operation.SELECT, unit.getX(), unit.getY());
                getManager().getOperationExecutor().submitOperation(Operation.MOVE, position.x, position.y);
                getManager().getOperationExecutor().submitOperation(Operation.STANDBY);
                break;
            }
        }
    }

    private int calculateUnitToBuy() {
        int unit_to_buy = -1;
        int highest_buying_score = 0;
        Array<Integer> available_units = getGame().getRule().getAvailableUnits();
        for (int unit_index : available_units) {
            int buying_score = getUnitBuyingScore(unit_index);
            if (buying_score > highest_buying_score) {
                highest_buying_score = buying_score;
                unit_to_buy = unit_index;
            }
        }
        return unit_to_buy;
    }

    private int getUnitBuyingScore(int unit_index) {
        //TODO: Needs to be improved.
        int price = getGame().getUnitPrice(unit_index, team);
        if (price >= 0 && getGame().getPlayer(team).getGold() >= price) {
            if (unit_index == 0) {
                return 100;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    private Position getUnitBuyingPosition(int unit_index) {
        //TODO: Needs to be improved.
        if (unit_index >= 0) {
            for (int x = 0; x < getGame().getMap().getWidth(); x++) {
                for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                    Tile tile = getGame().getMap().getTile(x, y);
                    if (tile.isCastle() && tile.getTeam() == team && getManager().canBuy(unit_index, team, x, y)) {
                        return getGame().getMap().getPosition(x, y);
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private Unit getBoughtUnit(int unit_index, int map_x, int map_y) {
        if (UnitFactory.getCommanderIndex() == unit_index) {
            Unit unit = getGame().getCommander(team);
            unit.setCurrentHp(unit.getMaxHp());
            getGame().resetUnit(unit);
            unit.clearStatus();
            unit.setX(map_x);
            unit.setY(map_y);
            return unit;
        } else {
            Unit unit = UnitFactory.createUnit(unit_index, team);
            unit.setX(map_x);
            unit.setY(map_y);
            return unit;
        }
    }

    private Unit getNextUnit(int team) {
        Array<Unit> units = new Array<Unit>();

        ObjectMap.Keys<Position> unit_positions = getGame().getMap().getUnitPositionSet();
        for (Position position : unit_positions) {
            Unit unit = getGame().getMap().getUnit(position);
            if (unit.getTeam() == team && !unit.isStandby()) {
                units.add(unit);
            }
        }

        if (units.size > 0) {
            return units.get(random.nextInt(units.size));
        } else {
            return null;
        }
    }

    private boolean isTargetUnit(Unit unit) {
        return target_units.contains(unit.getUnitCode());
    }

}
