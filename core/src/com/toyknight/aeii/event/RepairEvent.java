package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 *
 * @author toyknight
 */
public class RepairEvent implements GameEvent {

	private final BasicGame game;
	private final Unit repairer;
	private final int x;
	private final int y;

	public RepairEvent(BasicGame game, Unit repairer, int x, int y) {
		this.game = game;
		this.repairer = repairer;
		this.x = x;
		this.y = y;
	}
	
	protected BasicGame getGame() {
		return game;
	}
	
	@Override
	public boolean canExecute() {
		return getGame().getMap().getTile(x, y).isRepairable();
	}

	@Override
	public void execute(GameListener listener) {
		Tile tile = getGame().getMap().getTile(x, y);
		getGame().setTile(tile.getRepairedTileIndex(), x, y);
		getGame().standbyUnit(repairer.getX(), repairer.getY());
		listener.onRepair();
	}

}
