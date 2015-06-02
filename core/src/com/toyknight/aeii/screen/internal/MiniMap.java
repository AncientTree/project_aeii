package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

import java.util.Set;

/**
 * Created by toyknight on 6/1/2015.
 */
public class MiniMap extends Table {

    private final int ts;
    private final int sts;

    private final GameScreen screen;

    private float state_time;

    public MiniMap(final GameScreen screen) {
        this.screen = screen;
        this.ts = screen.getContext().getTileSize();
        this.sts = ts / 24 * 10;
    }

    private Map getMap() {
        return screen.getGame().getMap();
    }

    public void updateBounds() {
        this.state_time = 0f;
        int width = getMap().getWidth() * sts + 10;
        int height = getMap().getHeight() * sts + 10;
        this.setBounds(
                (screen.getViewportWidth() - width) / 2,
                (screen.getViewportHeight() - height) / 2 + ts,
                width, height);
    }

    public void update(float delta) {
        state_time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(ResourceManager.getBorderDarkColor(), x, y, width, height);
        batch.draw(ResourceManager.getBorderLightColor(), x + 1, y + 1, width - 2, height - 2);
        batch.draw(ResourceManager.getBorderDarkColor(), x + 4, y + 4, width - 8, height - 8);
        for (int map_x = 0; map_x < getMap().getWidth(); map_x++) {
            for (int map_y = 0; map_y < getMap().getHeight(); map_y++) {
                Tile tile = getMap().getTile(map_x, map_y);
                batch.draw(
                        ResourceManager.getSTileTexture(tile.getMiniMapIndex()),
                        x + map_x * sts + 5, y + height - 5 - map_y * sts - sts, sts, sts);
            }
        }
        Set<Point> unit_positions = getMap().getUnitPositionSet();
        for (Point position : unit_positions) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (((int) (state_time / 0.3f)) % 2 == 0) {
                batch.draw(
                        ResourceManager.getMiniIcon(unit.getTeam()),
                        x + unit.getX() * sts + 5, y + height - 5 - unit.getY() * sts - sts, sts, sts);
            }
        }
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
