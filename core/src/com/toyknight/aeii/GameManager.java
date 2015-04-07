package com.toyknight.aeii;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.renderer.MapRenderer;
import com.toyknight.aeii.renderer.StatusBarRenderer;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameManager extends Stage {

    private final int ts;
    private final MapRenderer map_renderer;
    private final StatusBarRenderer status_bar_renderer;

    private int viewport_x;
    private int viewport_y;

    private BasicGame game;

    public GameManager(int ts) {
        this.ts = ts;
        this.map_renderer = new MapRenderer(ts);
        this.status_bar_renderer = new StatusBarRenderer(ts);
        this.viewport_x = 0;
        this.viewport_y = 0;
    }

    public void setGame(BasicGame game) {
        this.game = game;
        locateViewport(0, 0);
    }

    public BasicGame getGame() {
        return game;
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

    @Override
    public void draw() {
        map_renderer.render(this);
        status_bar_renderer.render(this);
        super.draw();
    }

    @Override
    public void act(float delta) {
        map_renderer.update(delta);
        super.act(delta);
    }

}
