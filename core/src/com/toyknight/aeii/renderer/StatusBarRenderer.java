package com.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;

/**
 * Created by toyknight on 4/6/2015.
 */
public class StatusBarRenderer {

    private final int ts;
    private final SpriteBatch batch;
    private final ShapeRenderer shape_renderer;

    public StatusBarRenderer(int ts) {
        this.ts = ts;
        this.batch = new SpriteBatch();
        shape_renderer = new ShapeRenderer();
        shape_renderer.setAutoShapeType(true);
    }

    public void render(GameManager manager) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(ResourceManager.getAEIIBackgroundColor());
        shape_renderer.rect(0, 0, Gdx.app.getGraphics().getWidth(), ts);
        shape_renderer.end();

        batch.begin();
        BorderRenderer.drawBorder(batch, 0, 0, ts, ts);
        BorderRenderer.drawBorder(batch, ts, 0, Gdx.app.getGraphics().getWidth() - ts, ts);
        batch.end();
    }

}
