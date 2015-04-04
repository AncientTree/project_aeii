package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Qua
 */
public class MapHpChangeEvent implements GameEvent {

    private final BasicGame game;
    private final ConcurrentHashMap<Point, Integer> hp_change_map;

    public MapHpChangeEvent(BasicGame game, HashMap<Point, Integer> hp_change_map) {
        this.game = game;
        this.hp_change_map = new ConcurrentHashMap(hp_change_map);
    }

    @Override
    public boolean canExecute() {
        return !hp_change_map.isEmpty();
    }

    @Override
    public void execute(GameListener listener) {
        for (Point position : hp_change_map.keySet()) {
            Unit unit = game.getMap().getUnit(position.x, position.y);
            int actual_change = validateHpChange(unit, hp_change_map.get(position));
            if (actual_change != 0) {
                hp_change_map.put(position, actual_change);
                int changed_hp = unit.getCurrentHp() + actual_change;
                unit.setCurrentHp(changed_hp);
            } else {
                hp_change_map.remove(position);
            }
        }
        listener.onMapHpChange(hp_change_map);
    }

    private int validateHpChange(Unit unit, int change) {
        if (unit.getCurrentHp() + change <= 0) {
            return -unit.getCurrentHp();
        }
        if (unit.getCurrentHp() + change >= unit.getMaxHp()) {
            return unit.getMaxHp() - unit.getCurrentHp();
        }
        return change;
    }

}
