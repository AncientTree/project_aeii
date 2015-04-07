package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.renderer.MapRenderer;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameScreen extends Stage implements Screen {

    private final int ts;
    private final AEIIApplication context;

    private final SpriteBatch batch;
    private final MapRenderer map_renderer;
    private final ShapeRenderer shape_renderer;
    private final GameManager manager;

    private int viewport_x;
    private int viewport_y;

    public GameScreen(AEIIApplication context) {
        this.context = context;
        this.ts = context.getTileSize();

        this.batch = new SpriteBatch();
        this.map_renderer = new MapRenderer(this, ts);
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
        this.manager = new GameManager();
    }

    @Override
    public void draw() {
        map_renderer.render(getGame().getMap());
        this.drawStatusBar();

        super.draw();
    }

    private void drawStatusBar() {
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

    @Override
    public void act(float delta) {
        map_renderer.update(delta);
        super.act(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {

    }


    public void setGame(BasicGame game) {
        this.manager.setGame(game);
        this.locateViewport(0, 0);
    }

    public BasicGame getGame() {
        return manager.getGame();
    }

    public int getXOnScreen(int map_x) {
        int sx = viewport_x / ts;
        sx = sx > 0 ? sx : 0;
        int x_offset = sx * ts - viewport_x;
        return (map_x - sx) * ts + x_offset;
    }

    public int getYOnScreen(int map_y) {
        int screen_height = Gdx.graphics.getHeight();
        int sy = viewport_y / ts;
        sy = sy > 0 ? sy : 0;
        int y_offset = sy * ts - viewport_y;
        return screen_height - ((map_y - sy) * ts + y_offset) - ts;
    }

    public boolean isWithinPaintArea(int sx, int sy) {
        int viewport_width = Gdx.graphics.getWidth();
        int viewport_height = Gdx.graphics.getHeight() - ts;
        return -ts < sx && sx < viewport_width && 0 < sy && sy < viewport_height + ts;
    }

    public void locateViewport(int map_x, int map_y) {
        int viewport_width = Gdx.graphics.getWidth();
        int viewport_height = Gdx.graphics.getHeight() - ts;
        int center_sx = map_x * ts;
        int center_sy = map_y * ts;
        int map_width = getGame().getMap().getWidth() * ts;
        int map_height = getGame().getMap().getHeight() * ts;
        if (viewport_width < map_width) {
            viewport_x = center_sx - (viewport_width - ts) / 2;
            if (viewport_x < 0) {
                viewport_x = 0;
            }
            if (viewport_x > map_width - viewport_width) {
                viewport_x = map_width - viewport_width;
            }
        } else {
            viewport_x = (map_width - viewport_width) / 2;
        }
        if (viewport_height < map_height) {
            viewport_y = center_sy - (viewport_height - ts) / 2;
            if (viewport_y < 0) {
                viewport_y = 0;
            }
            if (viewport_y > map_height - viewport_height) {
                viewport_y = map_height - viewport_height;
            }
        } else {
            viewport_y = (map_height - viewport_height) / 2;
        }
    }

    public int getViewportX() {
        return viewport_x;
    }

    public int getViewportY() {
        return viewport_y;
    }

}
