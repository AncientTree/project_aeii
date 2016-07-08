package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Unit;
/**
 * @author toyknight 5/26/2015.
 */
public class HpChangeAnimator extends UnitAnimator {

    private final int[] y_offset = {2, 0, -1, -1, -2, -2, -2, -2, -1, -1, 0, 1, 2, 4, 6, 4, 3, 4, 6, 6, 6, 6};

    private final ObjectMap<Position, Integer> change_map;

    private int current_frame = 0;

    public HpChangeAnimator(GameContext context, ObjectMap<Position, Integer> change_map, ObjectSet<Unit> units) {
        super(context);
        int index = 0;
        for (Unit unit : units) {
            addUnit(unit, Integer.toString(index++));
        }
        this.change_map = change_map;
    }

    public HpChangeAnimator(GameContext context, Unit unit, int change) {
        super(context);
        addUnit(unit, Integer.toString(0));
        this.change_map = new ObjectMap<Position, Integer>();
        this.change_map.put(new Position(unit.getX(), unit.getY()), change);
    }

    @Override
    public void render(Batch batch) {
        int dy = (ts() - getContext().getFontRenderer().getLCharHeight()) / 2;
        for (int index = 0; index < getUnitCount(); index++) {
            Unit unit = getUnit(Integer.toString(index));
            int change = change_map.get(new Position(unit.getX(), unit.getY()));
            getCanvas().getRenderer().drawUnitWithInformation(batch, unit, unit.getX(), unit.getY());
            int sx = getCanvas().getXOnScreen(unit.getX());
            int sy = getCanvas().getYOnScreen(unit.getY());
            if (getCanvas().isWithinPaintArea(sx, sy)) {
                int dx = (ts() - getContext().getFontRenderer().getLNumberWidth(Math.abs(change), true)) / 2;
                if (change > 0) {
                    getContext().getFontRenderer().drawPositiveLNumber(
                            batch, change, sx + dx, sy + dy - y_offset[current_frame] * ts() / 24);
                }
                if (change < 0) {
                    getContext().getFontRenderer().drawNegativeLNumber(
                            batch, Math.abs(change), sx + dx, sy + dy - y_offset[current_frame] * ts() / 24);
                }
                batch.flush();
            }
        }
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
        current_frame = (int) (getStateTime() / (1f / 30));
        if (current_frame > 21) {
            current_frame = 21;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 30 * 21;
    }

}
