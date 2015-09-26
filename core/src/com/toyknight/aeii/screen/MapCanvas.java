package com.toyknight.aeii.screen;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.renderer.UnitRenderer;

/**
 * @author toyknight 7/8/2015.
 */
public interface MapCanvas {

    Map getMap();

    UnitRenderer getUnitRenderer();

    boolean isWithinPaintArea(int sx, int sy);

    int getViewportWidth();

    int getViewportHeight();

    int getXOnScreen(int map_x);

    int getYOnScreen(int map_y);

    int getCursorMapX();

    int getCursorMapY();

    int ts();

}
