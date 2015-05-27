package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.screen.GameScreen;

import java.util.HashSet;

/**
 * Created by toyknight on 4/20/2015.
 */
public class AlphaRenderer {

    private final int ts;
    private final GameScreen screen;
    private final TextureRegion move_alpha;
    private final TextureRegion attack_alpha;

    public AlphaRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
        Texture alpha_texture = ResourceManager.getAlphaTexture();
        int size = alpha_texture.getHeight();
        move_alpha = new TextureRegion(alpha_texture, size, 0, size, size);
        attack_alpha = new TextureRegion(alpha_texture, 0, 0, size, size);
    }

    public void drawMoveAlpha(SpriteBatch batch, HashSet<Point> movable_positions) {
        for (Point point : movable_positions) {
            int screen_x = screen.getXOnScreen(point.x);
            int screen_y = screen.getYOnScreen(point.y);
            batch.draw(move_alpha, screen_x, screen_y, ts, ts);
        }
        batch.flush();
    }

    public void drawAttackAlpha(SpriteBatch batch, HashSet<Point> attackable_positions) {
        for (Point point : attackable_positions) {
            int screen_x = screen.getXOnScreen(point.x);
            int screen_y = screen.getYOnScreen(point.y);
            batch.draw(attack_alpha, screen_x, screen_y, ts, ts);
        }
        batch.flush();
    }

}
