package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;

/**
 * Created by toyknight on 4/6/2015.
 */
public class BorderRenderer {

    private static int border_size;
    private static TextureRegionDrawable[] borders;

    public static void init() {
        borders = new TextureRegionDrawable[8];
        Texture border_texture_sheet = ResourceManager.getBorderTexture();
        int border_size = border_texture_sheet.getHeight();
        for (int i = 0; i < 8; i++) {
            TextureRegion border = new TextureRegion(border_texture_sheet, border_size * i, 0, border_size, border_size);
            borders[i] = new TextureRegionDrawable(border);
        }
        BorderRenderer.border_size = ResourceManager.getBorderTexture().getHeight();
    }

    public static void drawBorder(SpriteBatch batch, float x, float y, float width, float height) {
        borders[0].draw(batch, x, y + height - border_size, border_size, border_size);
        borders[1].draw(batch, x + border_size, y + height - border_size, width - border_size * 2, border_size);
        borders[2].draw(batch, x + width - border_size, y + height - border_size, border_size, border_size);
        borders[3].draw(batch, x, y + border_size, border_size, height - border_size * 2);
        borders[4].draw(batch, x + width - border_size, y + border_size, border_size, height - border_size * 2);
        borders[5].draw(batch, x, y, border_size, border_size);
        borders[6].draw(batch, x + border_size, y, width - border_size * 2, border_size);
        borders[7].draw(batch, x + width - border_size, y, border_size, border_size);
    }

}
