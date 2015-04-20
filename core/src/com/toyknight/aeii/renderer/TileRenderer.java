package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.utils.TileFactory;

/**
 * Created by toyknight on 4/4/2015.
 */
public class TileRenderer {

    private final int ts;

    private float state_time = 0f;

    public TileRenderer(int ts) {
        this.ts = ts;
    }

    public void drawTile(SpriteBatch batch, int index, int x, int y) {
        batch.begin();
        int current_frame = getCurrentFrame();
        Tile tile = TileFactory.getTile(index);
        if (tile.isAnimated()) {
            if (current_frame == 0) {
                batch.draw(ResourceManager.getTileTexture(index), x, y, ts, ts);
            } else {
                batch.draw(ResourceManager.getTileTexture(tile.getAnimationTileIndex()), x, y, ts, ts);
            }
        } else {
            batch.draw(ResourceManager.getTileTexture(index), x, y, ts, ts);
        }
        batch.end();
    }

    public void drawTopTile(SpriteBatch batch, int index, int x, int y) {
        batch.begin();
        batch.draw(ResourceManager.getTopTileTexture(index), x, y, ts, ts);
        batch.end();
    }

    private int getCurrentFrame() {
        return ((int) (state_time / 0.3f)) % 2;
    }

    public void update(float delta) {
        state_time += delta;
    }

}
