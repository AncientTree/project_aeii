package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.screen.MapCanvas;

/**
 * @author toyknight 4/20/2015.
 */
public class AlphaRenderer {

    private final MapCanvas canvas;

    private final TextureRegion move_alpha;
    private final TextureRegion attack_alpha;

    public AlphaRenderer(MapCanvas canvas) {
        this.canvas = canvas;
        Texture alpha_texture = ResourceManager.getAlphaTexture();
        int size = alpha_texture.getHeight();
        move_alpha = new TextureRegion(alpha_texture, size, 0, size, size);
        attack_alpha = new TextureRegion(alpha_texture, 0, 0, size, size);
    }

    private MapCanvas getCanvas() {
        return canvas;
    }

    private int ts() {
        return getCanvas().ts();
    }

    public void drawMoveAlpha(SpriteBatch batch, ObjectSet<Point> movable_positions) {
        for (Point point : movable_positions) {
            int screen_x = getCanvas().getXOnScreen(point.x);
            int screen_y = getCanvas().getYOnScreen(point.y);
            batch.draw(move_alpha, screen_x, screen_y, ts(), ts());
        }
        batch.flush();
    }

    public void drawAttackAlpha(SpriteBatch batch, ObjectSet<Point> attackable_positions) {
        for (Point point : attackable_positions) {
            int screen_x = getCanvas().getXOnScreen(point.x);
            int screen_y = getCanvas().getYOnScreen(point.y);
            batch.draw(attack_alpha, screen_x, screen_y, ts(), ts());
        }
        batch.flush();
    }

}
