package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Unit;

/**
 * @author toyknight 6/24/2016.
 */
public class ReinforceAnimator extends UnitAnimator {

    private final int from_x;
    private final int from_y;

    private final int unit_count;

    private final ObjectMap<String, Position> animation_positions;

    private int current_index = 0;

    private float offset = 0f;

    public ReinforceAnimator(GameContext context, Array<Unit> reinforcements, int from_x, int from_y) {
        super(context);
        this.from_x = from_x;
        this.from_y = from_y;
        unit_count = reinforcements.size;
        animation_positions = new ObjectMap<String, Position>();
        for (int i = 0; i < reinforcements.size; i++) {
            Unit unit = reinforcements.get(i);
            addUnit(unit, "unit" + i);
            if (from_x >= 0 && from_y >= 0) {
                animation_positions.put("unit" + i, new Position(from_x, from_y));
            }
        }
    }

    private boolean checkStartPosition() {
        return from_x >= 0 && from_y >= 0;
    }

    @Override
    public void render(Batch batch) {
        if (checkStartPosition()) {
            for (int i = 0; i <= current_index; i++) {
                if (i < getUnitCount()) {
                    Position position = animation_positions.get("unit" + i);
                    Unit unit = getUnit("unit" + i);
                    if (unit.isAt(position.x, position.y)) {
                        getCanvas().getRenderer().drawUnitWithInformation(batch, unit, position.x, position.y);
                    } else {
                        getCanvas().getRenderer().drawUnitWithInformation(batch, unit,
                                position.x, position.y,
                                getOffsetX(position.x, unit.getX()),
                                getOffsetY(position.x, position.y, unit.getX(), unit.getY()));
                    }
                }
            }
        } else {
            int n = (int) (getStateTime() / 0.5f) - 1;
            if (n >= 0) {
                for (int i = 0; i <= n; i++) {
                    if (i < unit_count) {
                        Unit unit = getUnit("unit" + i);
                        getCanvas().getRenderer().drawUnitWithInformation(batch, unit, unit.getX(), unit.getY());
                    }
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        if (checkStartPosition()) {
            offset += ts() * 6 / (1f / delta);
            if (offset > ts()) {
                offset = 0f;
                if (current_index < getUnitCount()) {
                    current_index++;
                }
                for (int i = 0; i < current_index; i++) {
                    Position position = animation_positions.get("unit" + i);
                    Unit unit = getUnit("unit" + i);
                    if (position.x == unit.getX()) {
                        position.y = getNextY(position.x, position.y, unit.getX(), unit.getY());
                    } else {
                        position.x = getNextX(position.x, unit.getX());
                    }
                }
            }
        } else {
            addStateTime(delta);
        }
    }

    @Override
    public boolean isAnimationFinished() {
        if (checkStartPosition()) {
            for (String key : animation_positions.keys()) {
                Position position = animation_positions.get(key);
                if (!getUnit(key).isAt(position.x, position.y)) {
                    return false;
                }
            }
            return true;
        } else {
            return (int) (getStateTime() / 0.5f) - 1 >= unit_count;
        }
    }

    private int getNextX(int x, int target_x) {
        if (x < target_x) {
            return x + 1;
        }
        if (x > target_x) {
            return x - 1;
        }
        return x;
    }

    private float getOffsetX(int x, int target_x) {
        if (x < target_x) {
            return offset;
        }
        if (x > target_x) {
            return -offset;
        }
        return 0f;
    }

    private int getNextY(int x, int y, int target_x, int target_y) {
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

    private float getOffsetY(int x, int y, int target_x, int target_y) {
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

}
