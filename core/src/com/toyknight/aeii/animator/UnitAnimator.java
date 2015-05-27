package com.toyknight.aeii.animator;

import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.utils.UnitFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by toyknight on 4/19/2015.
 */
public class UnitAnimator extends MapAnimator {

    private final Map<String, Unit> units = new HashMap();

    public void addUnit(Unit unit, String key) {
        if (unit != null) {
            units.put(key, UnitFactory.cloneUnit(unit));
            this.addLocation(unit.getX(), unit.getY());
        }
    }

    public Unit getUnit(String key) {
        return units.get(key);
    }

    public int getUnitCount() {
        return units.values().size();
    }

}
