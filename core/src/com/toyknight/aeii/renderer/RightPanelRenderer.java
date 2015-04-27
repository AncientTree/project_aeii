package com.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/26/2015.
 */
public class RightPanelRenderer {

    private final int ts;
    private final GameScreen screen;
    private final ShapeRenderer shape_renderer;

    public RightPanelRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
    }

    public void drawStatusBar(SpriteBatch batch, GameManager manager) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(ResourceManager.getAEIIBackgroundColor());
        shape_renderer.rect(Gdx.graphics.getWidth() - screen.getRightPanelWidth(), 0, screen.getRightPanelWidth(), Gdx.graphics.getHeight());
        shape_renderer.end();

        batch.begin();
        BorderRenderer.drawBorder(batch, Gdx.graphics.getWidth() - screen.getRightPanelWidth(), 0, screen.getRightPanelWidth(), Gdx.graphics.getHeight());
        batch.end();
    }

}
