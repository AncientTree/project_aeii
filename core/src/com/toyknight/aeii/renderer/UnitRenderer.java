package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.UnitFactory;

/**
 * Created by toyknight on 4/19/2015.
 */
public class UnitRenderer {

    private final int ts;
    private final GameScreen screen;

    private float state_time = 0f;

    public UnitRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
    }

    public void drawUnit(SpriteBatch batch, Unit unit, int map_x, int map_y) {
        drawUnit(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnit(SpriteBatch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        TextureRegion unit_texture;
        if (unit.isStandby()) {
            batch.setShader(ResourceManager.getGrayscaleShader());
            unit_texture = ResourceManager.getUnitTexture(unit.getPackage(), unit.getTeam(), unit.getIndex(), unit.getLevel(), 0);
        } else {
            unit_texture = ResourceManager.getUnitTexture(unit.getPackage(), unit.getTeam(), unit.getIndex(), unit.getLevel(), getCurrentFrame());
        }
        batch.begin();
        int screen_x = screen.getXOnScreen(map_x);
        int screen_y = screen.getYOnScreen(map_y);
        batch.draw(unit_texture, screen_x + offset_x, screen_y + offset_y, ts, ts);
        batch.end();
        batch.setShader(null);
    }

    public void drawUnitWithInformation(SpriteBatch batch, Unit unit, int map_x, int map_y) {
        drawUnitWithInformation(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnitWithInformation(SpriteBatch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        drawUnit(batch, unit, map_x, map_y, offset_x, offset_y);
        if (unit.getCurrentHp() < unit.getMaxHp()) {
            int screen_x = screen.getXOnScreen(map_x);
            int screen_y = screen.getYOnScreen(map_y);
            FontRenderer.drawSNumber(batch, unit.getCurrentHp(), screen_x, screen_y);
        }
        //draw buff
    }

    private int getCurrentFrame() {
        return ((int) (state_time / 0.3f)) % 2;
    }

    public void update(float delta) {
        state_time += delta;
    }

}
