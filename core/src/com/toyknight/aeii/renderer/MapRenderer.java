package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.TileFactory;

/**
 * Created by toyknight on 4/4/2015.
 */
public class MapRenderer {

    private final int ts;
    private final GameScreen screen;
    private final TileRenderer tile_renderer;

    public MapRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
        this.tile_renderer = new TileRenderer(ts);
    }

    public void drawMap(SpriteBatch batch, Map map) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int sx = screen.getXOnScreen(x);
                int sy = screen.getYOnScreen(y);
                if (screen.isWithinPaintArea(sx, sy)) {
                    int index = map.getTileIndex(x, y);
                    tile_renderer.drawTile(batch, index, sx, sy);
                    Tile tile = TileFactory.getTile(index);
                    if (tile.getTopTileIndex() != -1) {
                        int top_tile_index = tile.getTopTileIndex();
                        tile_renderer.drawTopTile(batch, top_tile_index, sx, sy + ts);
                    }
                }
            }
        }
    }

    public void update(float delta) {
        tile_renderer.update(delta);
    }

}
