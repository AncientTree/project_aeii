package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Status;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.MapCanvas;

/**
 * Created by toyknight on 4/19/2015.
 */
public class UnitRenderer {

    private final int ts;
    private final MapCanvas canvas;

    private float state_time = 0f;

    public UnitRenderer(MapCanvas canvas, int ts) {
        this.ts = ts;
        this.canvas = canvas;
    }

    public void drawUnit(Batch batch, Unit unit, float screen_x, float screen_y, boolean is_static) {
        TextureRegion unit_texture;
        if (is_static) {
            unit_texture = ResourceManager.getUnitTexture(unit.getTeam(), unit.getIndex(), unit.getLevel(), 0);
        } else {
            if (unit.isStandby()) {
                batch.setShader(ResourceManager.getGrayscaleShader(0f));
                unit_texture = ResourceManager.getUnitTexture(unit.getTeam(), unit.getIndex(), unit.getLevel(), 0);
            } else {
                unit_texture = ResourceManager.getUnitTexture(unit.getTeam(), unit.getIndex(), unit.getLevel(), getCurrentFrame());
            }
        }
        batch.draw(unit_texture, screen_x, screen_y, ts, ts);
        batch.setShader(null);
        batch.flush();
    }

    public void drawUnit(Batch batch, Unit unit, int map_x, int map_y) {
        drawUnit(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnit(Batch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        int screen_x = canvas.getXOnScreen(map_x);
        int screen_y = canvas.getYOnScreen(map_y);
        drawUnit(batch, unit, screen_x + offset_x, screen_y + offset_y, false);
    }

    public void drawUnitWithInformation(Batch batch, Unit unit, int map_x, int map_y) {
        drawUnitWithInformation(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnitWithInformation(Batch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        drawUnit(batch, unit, map_x, map_y, offset_x, offset_y);
        int screen_x = canvas.getXOnScreen(map_x);
        int screen_y = canvas.getYOnScreen(map_y);
        //draw health points
        if (unit.getCurrentHp() < unit.getMaxHp()) {
            FontRenderer.drawSNumber(batch, unit.getCurrentHp(), (int) (screen_x + offset_x), (int) (screen_y + offset_y));
        }
        //draw status
        int sw = ts * ResourceManager.getStatusTexture(0).getRegionWidth() / 24;
        int sh = ts * ResourceManager.getStatusTexture(0).getRegionHeight() / 24;
        if (unit.getStatus() != null) {
            switch (unit.getStatus().getType()) {
                case Status.POISONED:
                    batch.draw(ResourceManager.getStatusTexture(0), screen_x + offset_x, screen_y + ts - sh + offset_y, sw, sh);
                    break;
                case Status.PETRIFACTED:
                    batch.draw(ResourceManager.getStatusTexture(2), screen_x + offset_x, screen_y + ts - sh + offset_y, sw, sh);
                    break;
                default:
                    //do nothing
            }
        }
        batch.flush();
    }

    private int getCurrentFrame() {
        return ((int) (state_time / 0.3f)) % 2;
    }

    public void update(float delta) {
        state_time += delta;
    }

}
