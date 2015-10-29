package com.toyknight.aeii.manager.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.animator.HpChangeAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;

import java.io.Serializable;

/**
 * @author toyknight 5/26/2015.
 */
public class HpChangeEvent implements GameEvent, Serializable {

    private static final long serialVersionUID = 5262015L;

    private final ObjectMap<Point, Integer> change_map;

    public HpChangeEvent() {
        this(new ObjectMap<Point, Integer>());
    }

    public HpChangeEvent(ObjectMap<Point, Integer> change_map) {
        this.change_map = new ObjectMap<Point, Integer>(change_map);
    }

    public Point getFocus(GameCore game) {
        return null;
    }

    @Override
    public boolean canExecute(GameCore game) {
        return change_map.size > 0;
    }

    @Override
    public void execute(GameManager manager) {
        int change_count = 0;
        ObjectSet<Unit> units = new ObjectSet<Unit>();
        for (Point position : change_map.keys()) {
            Unit unit = manager.getGame().getMap().getUnit(position.x, position.y);
            if (unit != null) {
                int change = validateChange(unit, change_map.get(position));
                if (change != 0) {
                    change_count++;
                    change_map.put(position, change);
                    unit.changeCurrentHp(change);
                    units.add(unit);
                }
            }
        }
        if (change_count > 0) {
            manager.submitAnimation(new HpChangeAnimator(change_map, units));
        }
    }

    private int validateChange(Unit unit, int change) {
        int origin_hp = unit.getCurrentHp();
        int changed_hp = origin_hp + change;
        if (changed_hp > unit.getMaxHp()) {
            changed_hp = unit.getMaxHp();
        }
        if (changed_hp < 0) {
            changed_hp = 0;
        }
        return changed_hp - origin_hp;
    }

}
