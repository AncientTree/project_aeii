package com.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/19/2015.
 */
public class StatusBarRenderer {

    private final int ts;
    private final GameScreen screen;
    private final ShapeRenderer shape_renderer;

    public StatusBarRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
    }

    public void drawStatusBar(SpriteBatch batch, GameManager manager) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(ResourceManager.getAEIIBackgroundColor());
        shape_renderer.rect(0, 0, Gdx.app.getGraphics().getWidth(), ts);
        shape_renderer.end();

        batch.begin();
        drawSelectedTile(batch, manager);
        BorderRenderer.drawBorder(batch, 0, 0, ts, ts);
        BorderRenderer.drawBorder(batch, ts, 0, Gdx.app.getGraphics().getWidth() - ts, ts);
        batch.end();
    }

    private void drawSelectedTile(SpriteBatch batch, GameManager manager) {
        int cursor_x = screen.getCursorMapX();
        int cursor_y = screen.getCursorMapY();
        if (cursor_x >= 0 && cursor_y >= 0) {
            short tile_index = manager.getGame().getMap().getTileIndex(cursor_x, cursor_y);
            batch.draw(ResourceManager.getTileTexture(tile_index), 0, 0, ts, ts);
        }
    }

}
