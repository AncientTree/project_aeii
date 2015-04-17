package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.listener.GameListener;

/**
 *
 * @author toyknight
 */
public class TileDestroyEvent implements GameEvent {
	
	private final GameCore game;
	private final int x;
	private final int y;
	
	public TileDestroyEvent(GameCore game, int x, int y) {
		this.game = game;
		this.x = x;
		this.y = y;
	}
	
	protected GameCore getGame() {
		return game;
	}
	
	@Override
	public boolean canExecute() {
		return getGame().getMap().getTile(x, y).isDestroyable();
	}

	@Override
	public void execute(GameListener listener) {
		int tile_index = getGame().getMap().getTileIndex(x, y);
		listener.onTileDestroy(tile_index, x, y);
		short destroyed_tile_index = 
				getGame().getMap().getTile(x, y).getDestroyedTileIndex();
		getGame().getMap().setTile(destroyed_tile_index, x, y);
	}
	
}
