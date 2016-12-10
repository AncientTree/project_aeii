package net.toyknight.aeii.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * @author toyknight 12/9/2016.
 */
public class TextureUtil {

    private TextureUtil() {
    }

    public static TextureRegion[] createFrames(Texture sheet, int cols, int rows) {
        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / cols,
                sheet.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return frames;
    }

    public static Animation createAnimation(Texture sheet, int cols, int rows, float frame_duration) {
        TextureRegion[] frames = createFrames(sheet, cols, rows);
        return new Animation(frame_duration, frames);
    }

    public static TextureRegionDrawable createDrawable(Texture texture) {
        return createDrawable(texture, texture.getWidth(), texture.getHeight());
    }

    public static TextureRegionDrawable createDrawable(Texture texture, int width, int height) {
        return createDrawable(new TextureRegion(texture), width, height);
    }

    public static TextureRegionDrawable createDrawable(TextureRegion texture, int width, int height) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinWidth(width);
        drawable.setMinHeight(height);
        return drawable;
    }

    public static void setBatchAlpha(Batch batch, float alpha) {
        Color color = batch.getColor();
        batch.setColor(color.r, color.g, color.b, alpha);
    }

    public static Pixmap createColoredPixmap(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return pixmap;
    }

}
