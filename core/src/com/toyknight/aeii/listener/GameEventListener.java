package com.toyknight.aeii.listener;

import com.toyknight.aeii.entity.Unit;

/**
 * Created by toyknight on 4/23/2015.
 */
public interface GameEventListener {

    public void onUnitMoved(Unit unit, int start_x, int start_y);

    public void onUnitMoveReversed(int origin_x, int origin_y);

}
