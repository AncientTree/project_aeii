package net.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.TextureUtil;

/**
 * @author toyknight 4/6/2015.
 */
public class BorderRenderer {

    private static int border_size;
    private static TextureRegion[] borders;

    public static void initialize() {
        Texture border_texture_sheet = AER.resources.getBorderTexture();
        borders = TextureUtil.createFrames(border_texture_sheet, 8, 1);
        border_size = AER.ts * AER.resources.getBorderTexture().getHeight() / 48;
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

    public static void drawRoundedBackground(
            ShapeRenderer shape_renderer, float x, float y, float width, float height, float radius) {
        // Central rectangle
        shape_renderer.setColor(36 / 256f, 42 / 256f, 69 / 256f, 1f);

        shape_renderer.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius);

        // Four side rectangles, in clockwise order
        shape_renderer.rect(x + radius, y, width - 2 * radius, radius);
        shape_renderer.rect(x + width - radius, y + radius, radius, height - 2 * radius);
        shape_renderer.rect(x + radius, y + height - radius, width - 2 * radius, radius);
        shape_renderer.rect(x, y + radius, radius, height - 2 * radius);

        // Four arches, clockwise too
        shape_renderer.arc(x + radius, y + radius, radius, 180f, 90f);
        shape_renderer.arc(x + width - radius, y + radius, radius, 270f, 90f);
        shape_renderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f);
        shape_renderer.arc(x + radius, y + height - radius, radius, 90f, 90f);
    }

}
