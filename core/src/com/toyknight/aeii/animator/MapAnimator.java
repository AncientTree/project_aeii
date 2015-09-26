package com.toyknight.aeii.animator;

import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.screen.MapCanvas;

import java.util.HashSet;

/**
 * @author toyknight 4/21/2015.
 */
public class MapAnimator extends Animator {

    private final HashSet<Point> locations = new HashSet<Point>();
    private final MapCanvas canvas;

    public MapAnimator(MapCanvas canvas) {
        this.canvas = canvas;
    }

    protected MapCanvas getCanvas() {
        return canvas;
    }

    protected int ts() {
        return getCanvas().ts();
    }

    public MapAnimator(MapCanvas canvas, int x, int y) {
        this(canvas);
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

}
