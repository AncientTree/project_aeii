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
    private final TextureRegion[][][][] unit_textures;

    private float state_time = 0f;

    public UnitRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
        int unit_count = UnitFactory.getUnitCount();
        this.unit_textures = new TextureRegion[4][unit_count][4][2];
        createUnitTextures(unit_count);
    }

    private void createUnitTextures(int unit_count) {
        for (int team = 0; team < 4; team++) {
            Texture unit_texture_sheet = ResourceManager.getUnitTextureSheet(team);
            int texture_size = unit_texture_sheet.getWidth() / unit_count;
            for (int index = 0; index < unit_count; index++) {
                for (int level = 0; level < 4; level++) {
                    unit_textures[team][index][level][0] = new TextureRegion(unit_texture_sheet, index * texture_size, level * texture_size * 2, texture_size, texture_size);
                    unit_textures[team][index][level][1] = new TextureRegion(unit_texture_sheet, index * texture_size, level * texture_size * 2 + texture_size, texture_size, texture_size);
                }
            }
        }
    }

    public void drawUnit(SpriteBatch batch, Unit unit, int map_x, int map_y) {
        drawUnit(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnit(SpriteBatch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        batch.begin();
        TextureRegion unit_texture = unit_textures[unit.getTeam()][unit.getIndex()][unit.getLevel()][getCurrentFrame()];
        int screen_x = screen.getXOnScreen(map_x);
        int screen_y = screen.getYOnScreen(map_y);
        batch.draw(unit_texture, screen_x + offset_x, screen_y + offset_y, ts, ts);
        batch.end();
    }

    public void drawUnitWithInformation(SpriteBatch batch, Unit unit, int map_x, int map_y) {
        drawUnitWithInformation(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnitWithInformation(SpriteBatch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        drawUnit(batch, unit, map_x, map_y, offset_x, offset_y);
        //draw information
    }

    private int getCurrentFrame() {
        return ((int) (state_time / 0.3f)) % 2;
    }

    public void update(float delta) {
        state_time += delta;
    }

}
