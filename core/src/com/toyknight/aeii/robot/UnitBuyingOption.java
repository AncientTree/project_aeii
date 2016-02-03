package com.toyknight.aeii.robot;

import com.toyknight.aeii.entity.Position;

/**
 * @author toyknight 2/3/2016.
 */
public class UnitBuyingOption {

    private final int unit_index;
    private final Position position;

    public UnitBuyingOption(int unit_index, Position position) {
        this.unit_index = unit_index;
        this.position = position;
    }

    public int getUnitIndex() {
        return unit_index;
    }

    public Position getPosition() {
        return position;
    }

}