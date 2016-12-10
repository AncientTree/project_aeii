package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.UnitToolkit;

/**
 * @author toyknight 6/28/2016.
 */
public class CrystalStealAnimator extends UnitAnimator {

    private final int map_x;
    private final int map_y;
    private final int target_x;
    private final int target_y;

    private final Unit unit1;
    private int x1;
    private int y1;

    private final Unit unit2;
    private int x2;
    private int y2;

    private final Unit unit3;
    private int x3;
    private int y3;

    private float offset = 0f;

    public CrystalStealAnimator(int map_x, int map_y, int target_x, int target_y) {
        this.map_x = map_x;
        this.map_y = map_y;
        this.target_x = target_x;
        this.target_y = target_y;
        unit1 = AER.units.createUnit(0, 1);
        x1 = map_x;
        y1 = map_y;
        unit2 = AER.units.createUnit(AER.units.getCrystalIndex(), 1);
        x2 = map_x;
        y2 = map_y;
        unit3 = AER.units.createUnit(0, 1);
        x3 = map_x;
        y3 = map_y;
        while (map_x != target_x) {
            map_x = map_x < target_x ? map_x + 1 : map_x - 1;
            if (UnitToolkit.getRange(map_x, map_y, target_x, target_y) <= 2) {
                addLocation(map_x, map_y);
            }
        }
        while (map_y != target_y) {
            map_y = map_y < target_y ? map_y + 1 : map_y - 1;
            if (UnitToolkit.getRange(map_x, map_y, target_x, target_y) <= 2) {
                addLocation(map_x, map_y);
            }
        }
    }

    @Override
    public void render(Batch batch) {
        if (getCanvas().getMap().isWithinMap(x1, y1)) {
            CanvasRenderer.drawUnit(batch, unit1, x1, y1, getOffsetX(x1), getOffsetY(x1, y1));
        }
        if ((x1 != map_x || y1 != map_y) && getCanvas().getMap().isWithinMap(x2, y2)) {
            CanvasRenderer.drawUnit(batch, unit2, x2, y2, getOffsetX(x2), getOffsetY(x2, y2));
        }
        if ((x2 != map_x || y2 != map_y) && getCanvas().getMap().isWithinMap(x3, y3)) {
            CanvasRenderer.drawUnit(batch, unit3, x3, y3, getOffsetX(x3), getOffsetY(x3, y3));
        }
    }

    @Override
    public void update(float delta) {
        offset += ts() * 2 / (1f / delta);
        if (offset > ts()) {
            offset = 0f;
            if (x1 == target_x) {
                y1 = getNextY(x1, y1);
            } else {
                x1 = getNextX(x1);
            }
            if (UnitToolkit.getRange(x1, y1, x2, y2) > 1) {
                if (x2 == target_x) {
                    y2 = getNextY(x2, y2);
                } else {
                    x2 = getNextX(x2);
                }
            }
            if (UnitToolkit.getRange(x2, y2, x3, y3) > 1) {
                if (x3 == target_x) {
                    y3 = getNextY(x3, y3);
                } else {
                    x3 = getNextX(x3);
                }
            }
        }
    }

    private int getNextX(int x) {
        if (x < target_x) {
            return x + 1;
        }
        if (x > target_x) {
            return x - 1;
        }
        return x;
    }

    private float getOffsetX(int x) {
        if (x < target_x) {
            return offset;
        }
        if (x > target_x) {
            return -offset;
        }
        return 0f;
    }

    private int getNextY(int x, int y) {
        if (x == target_x) {
            if (y < target_y) {
                return y + 1;
            }
            if (y > target_y) {
                return y - 1;
            }
            return y;
        } else {
            return y;
        }
    }

    private float getOffsetY(int x, int y) {
        if (x == target_x) {
            if (y < target_y) {
                return -offset;
            }
            if (y > target_y) {
                return offset;
            }
            return 0f;
        } else {
            return 0f;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return x1 == target_x && y1 == target_y;
    }

}
