package com.toyknight.aeii.event;

import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 *
 * @author toyknight
 */
public class UnitSummonEvent implements GameEvent {
	
	private final BasicGame game;
	private final Unit summoner;
	private final int target_x;
	private final int target_y;
	
	public UnitSummonEvent(BasicGame game, Unit summoner, int target_x, int target_y) {
		this.game = game;
		this.summoner = summoner;
		this.target_x = target_x;
		this.target_y = target_y;
	}
	
	protected BasicGame getGame() {
		return game;
	}
	
	@Override
	public boolean canExecute() {
		return getGame().getMap().isTomb(target_x, target_y);
	}

	@Override
	public void execute(GameListener listener) {
		int skeleton = UnitFactory.getSkeletonIndex();
		getGame().getMap().removeTomb(target_x, target_y);
		getGame().addUnit(skeleton, getGame().getCurrentTeam(), target_x, target_y);
		getGame().getMap().getUnit(target_x, target_y).setStandby(true);
		listener.onSummon(summoner, target_x, target_y);
		if (summoner.gainExperience(30)) {
			listener.onUnitLevelUp(summoner);
		}
	}
	
}
