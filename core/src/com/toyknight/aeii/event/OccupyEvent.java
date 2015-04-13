package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.entity.SkirmishGame;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 *
 * @author toyknight
 */
public class OccupyEvent implements GameEvent {

	private final BasicGame game;
	private final Unit conqueror;
	private final int x;
	private final int y;

	public OccupyEvent(BasicGame game, Unit conqueror, int x, int y) {
		this.game = game;
		this.conqueror = conqueror;
		this.x = x;
		this.y = y;
	}
	
	protected BasicGame getGame() {
		return game;
	}
	
	@Override
	public boolean canExecute() {
		return getGame().getMap().getTile(x, y).isCapturable();
	}

	@Override
	public void execute(GameListener listener) {
		Tile tile = getGame().getMap().getTile(x, y);
		getGame().setTile(tile.getCapturedTileIndex(conqueror.getTeam()), x, y);
		getGame().standbyUnit(conqueror.getX(), conqueror.getY());
        listener.onOccupy();
		if(getGame() instanceof SkirmishGame) {
			((SkirmishGame)getGame()).onOccupy(x, y);
		}
	}
	
}
