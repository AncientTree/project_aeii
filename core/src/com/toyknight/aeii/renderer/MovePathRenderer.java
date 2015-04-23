package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.screen.GameScreen;

import java.util.ArrayList;

/**
 * Created by toyknight on 4/22/2015.
 */
public class MovePathRenderer {

    private final int ts;
    private final GameScreen screen;

    private final int cursor_size;
    private final int offset;

    public MovePathRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
        this.cursor_size = ts / 24 * 26;
        this.offset = ts / 24;
    }

    public void drawMovePath(SpriteBatch batch, ShapeRenderer shape_renderer, ArrayList<Point> move_path) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(ResourceManager.getMovePathColor());
        for (int i = 0; i < move_path.size(); i++) {
            if (i < move_path.size() - 1) {
                Point p1 = move_path.get(i);
                Point p2 = move_path.get(i + 1);
                if (p1.x == p2.x) {
                    int x = p1.x;
                    int y = p1.y > p2.y ? p1.y : p2.y;
                    int sx = screen.getXOnScreen(x);
                    int sy = screen.getYOnScreen(y);
                    shape_renderer.rect(sx + ts / 3, sy + ts / 3, ts / 3, ts / 3 * 4);
                }
                if (p1.y == p2.y) {
                    int x = p1.x < p2.x ? p1.x : p2.x;
                    int y = p1.y;
                    int sx = screen.getXOnScreen(x);
                    int sy = screen.getYOnScreen(y);
                    shape_renderer.rect(sx + ts / 3, sy + ts / 3, ts / 3 * 4, ts / 3);
                }
            } else {
                batch.begin();
                Point dest = move_path.get(i);
                int sx = screen.getXOnScreen(dest.x);
                int sy = screen.getYOnScreen(dest.y);
                batch.draw(ResourceManager.getMoveTargetCursorTexture(), sx - offset, sy - offset, cursor_size, cursor_size);
                batch.end();
            }
        }
        shape_renderer.end();
    }

}
