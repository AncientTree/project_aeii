package com.toyknight.aeii.animator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.screen.GameScreen;

import java.util.HashSet;

/**
 * Created by toyknight on 4/21/2015.
 */
public class MapAnimator extends Animator {

    private final HashSet<Point> locations = new HashSet();

    public MapAnimator() {
    }

    public MapAnimator(int x, int y) {
        if (x >= 0 && y >= 0) {
            this.addLocation(x, y);
        }
    }

    public final void addLocation(int x, int y) {
        this.locations.add(new Point(x, y));
    }

    public final boolean hasLocation(int x, int y) {
        return locations.contains(new Point(x, y));
    }

    public void render(SpriteBatch batch, GameScreen screen) {
    }

}
