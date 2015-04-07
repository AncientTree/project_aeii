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
    private final TextureRegionDrawable[] tile_texture;
    private final TextureRegionDrawable[] top_tile_texture;

    private float state_time = 0f;
    private int current_frame = 0;

    public TileRenderer(int ts) {
        this.ts = ts;
        tile_texture = new TextureRegionDrawable[TileFactory.getTileCount()];
        for (int i = 0; i < tile_texture.length; i++) {
            Texture texture = ResourceManager.getTileTexture(i);
            tile_texture[i] = new TextureRegionDrawable(new TextureRegion(texture, texture.getWidth(), texture.getHeight()));
        }
        top_tile_texture = new TextureRegionDrawable[ResourceManager.getTopTileCount()];
        for (int i = 0; i < top_tile_texture.length; i++) {
            Texture texture = ResourceManager.getTopTileTexture(i);
            top_tile_texture[i] = new TextureRegionDrawable(new TextureRegion(texture, texture.getWidth(), texture.getHeight()));
        }
    }

    public void renderTile(SpriteBatch batch, int index, int x, int y) {
        Tile tile = TileFactory.getTile(index);
        if (tile.isAnimated()) {
            if (current_frame == 0) {
                tile_texture[index].draw(batch, x, y, ts, ts);
            } else {
                tile_texture[tile.getAnimationTileIndex()].draw(batch, x, y, ts, ts);
            }
        } else {
            tile_texture[index].draw(batch, x, y, ts, ts);
        }
    }

    public void renderTopTile(SpriteBatch batch, int index, int x, int y) {
        top_tile_texture[index].draw(batch, x, y, ts, ts);
    }

    public void update(float delta) {
        if (state_time < 0.3f) {
            state_time += delta;
        } else {
            current_frame = current_frame == 0 ? 1 : 0;
            state_time = 0f;
        }
    }

}
