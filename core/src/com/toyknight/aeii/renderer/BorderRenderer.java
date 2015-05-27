package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;

/**
 * Created by toyknight on 4/6/2015.
 */
public class BorderRenderer {

    private static int border_size;
    private static TextureRegion[] borders;

    public static void init() {
        borders = new TextureRegion[8];
        Texture border_texture_sheet = ResourceManager.getBorderTexture();
        int border_size = border_texture_sheet.getHeight();
        for (int i = 0; i < 8; i++) {
            borders[i] = new TextureRegion(border_texture_sheet, border_size * i, 0, border_size, border_size);
        }
        BorderRenderer.border_size = ResourceManager.getBorderTexture().getHeight();
    }

    public static void drawBorder(Batch batch, float x, float y, float width, float height) {
        batch.draw(borders[0], x, y + height - border_size, border_size, border_size);
        batch.draw(borders[1], x + border_size, y + height - border_size, width - border_size * 2, border_size);
        batch.draw(borders[2], x + width - border_size, y + height - border_size, border_size, border_size);
        batch.draw(borders[3], x, y + border_size, border_size, height - border_size * 2);
        batch.draw(borders[4], x + width - border_size, y + border_size, border_size, height - border_size * 2);
        batch.draw(borders[5], x, y, border_size, border_size);
        batch.draw(borders[6], x + border_size, y, width - border_size * 2, border_size);
        batch.draw(borders[7], x + width - border_size, y, border_size, border_size);
        batch.flush();
    }

    public static void drawTopBottomBorder(Batch batch, float x, float y, float width, float height) {
        batch.draw(borders[1], x, y + height - border_size, width, border_size);
        batch.draw(borders[6], x, y, width, border_size);
        batch.flush();
    }

}
