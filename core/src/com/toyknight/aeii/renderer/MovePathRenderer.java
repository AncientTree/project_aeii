package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.screen.MapCanvas;

/**
 * @author toyknight 4/22/2015.
 */
public class MovePathRenderer {

    private final MapCanvas canvas;

    public MovePathRenderer(MapCanvas canvas) {
        this.canvas = canvas;
    }

    private MapCanvas getCanvas() {
        return canvas;
    }

    private int ts() {
        return getCanvas().ts();
    }

    public void drawMovePath(SpriteBatch batch, Array<Point> move_path) {
        int cursor_size = ts() * 26 / 24;
        int offset = ts() / 24;
        for (int i = 0; i < move_path.size; i++) {
            if (i < move_path.size - 1) {
                Point p1 = move_path.get(i);
                Point p2 = move_path.get(i + 1);
                if (p1.x == p2.x) {
                    int x = p1.x;
                    int y = p1.y > p2.y ? p1.y : p2.y;
                    int sx = getCanvas().getXOnScreen(x);
                    int sy = getCanvas().getYOnScreen(y);
                    batch.draw(ResourceManager.getMovePathColor(), sx + ts() / 3, sy + ts() / 3, ts() / 3, ts() / 3 * 4);
                }
                if (p1.y == p2.y) {
                    int x = p1.x < p2.x ? p1.x : p2.x;
                    int y = p1.y;
                    int sx = getCanvas().getXOnScreen(x);
                    int sy = getCanvas().getYOnScreen(y);
                    batch.draw(ResourceManager.getMovePathColor(), sx + ts() / 3, sy + ts() / 3, ts() / 3 * 4, ts() / 3);
                }
            }
        }
        if (move_path.size > 0) {
            Point dest = move_path.get(move_path.size - 1);
            int sx = getCanvas().getXOnScreen(dest.x);
            int sy = getCanvas().getYOnScreen(dest.y);
            batch.draw(ResourceManager.getMoveTargetCursorTexture(), sx - offset, sy - offset, cursor_size, cursor_size);
        }
        batch.flush();
    }

}
