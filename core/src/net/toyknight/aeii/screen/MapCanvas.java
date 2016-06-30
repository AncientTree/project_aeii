package net.toyknight.aeii.screen;

import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.renderer.UnitRenderer;

/**
 * @author toyknight 7/8/2015.
 */
public interface MapCanvas {

    Map getMap();

    UnitRenderer getUnitRenderer();

    void focus(int map_x, int map_y, boolean focus_viewport);

    boolean isWithinPaintArea(int sx, int sy);

    int getViewportWidth();

    int getViewportHeight();

    int getXOnScreen(int map_x);

    int getYOnScreen(int map_y);

    int getCursorMapX();

    int getCursorMapY();

    void setOffsetX(float offset_x);

    void setOffsetY(float offset_y);

    int ts();

}
