package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.utils.TileFactory;

/**
 * Created by toyknight on 4/4/2015.
 */
public class MapRenderer {

    private final int ts;
    private final SpriteBatch batch;
    private final TileRenderer tile_renderer;

    public MapRenderer(int ts) {
        this.ts = ts;
        this.batch = new SpriteBatch();
        this.tile_renderer = new TileRenderer(ts);
    }

    public void render(GameManager manager) {
        batch.begin();
        for (int x = 0; x < manager.getGame().getMap().getWidth(); x++) {
            for (int y = 0; y < manager.getGame().getMap().getHeight(); y++) {
                int sx = manager.getXOnScreen(x);
                int sy = manager.getYOnScreen(y);
                if (manager.isWithinPaintArea(sx, sy)) {
                    int index = manager.getGame().getMap().getTileIndex(x, y);
                    tile_renderer.renderTile(batch, index, sx, sy);
                    Tile tile = TileFactory.getTile(index);
                    if (tile.getTopTileIndex() != -1) {
                        int top_tile_index = tile.getTopTileIndex();
                        tile_renderer.renderTopTile(batch, top_tile_index, sx, sy + ts);
                    }
                }
            }
        }
        batch.end();
    }

    public void update(float delta) {
        tile_renderer.update(delta);
    }

}
