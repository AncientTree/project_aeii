package com.toyknight.aeii.animator;

import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.utils.UnitFactory;

/**
 * Created by toyknight on 4/19/2015.
 */
public class UnitAnimator extends MapAnimator {

    private final Unit unit;

    public UnitAnimator(Unit unit, int x, int y) {
        super(x, y);
        if (unit != null) {
            this.unit = UnitFactory.cloneUnit(unit);
        } else {
            this.unit = null;
        }
    }

    public Unit getUnit() {
        return unit;
    }

}
