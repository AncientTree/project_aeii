package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 *
 * @author toyknight
 */
public class UnitActionFinishEvent implements GameEvent {

	private final Unit unit;

	public UnitActionFinishEvent(Unit unit) {
		this.unit = unit;
	}
	
	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public void execute(GameListener listener) {
		listener.onUnitActionFinish(unit);
	}

}
