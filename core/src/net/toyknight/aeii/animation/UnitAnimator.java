package net.toyknight.aeii.animation;

import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 4/19/2015.
 */
public class UnitAnimator extends MapAnimator {

    private final ObjectMap<String, Unit> units = new ObjectMap<String, Unit>();

    public void addUnit(Unit unit, String key) {
        if (unit != null) {
            units.put(key, AER.units.cloneUnit(unit));
            this.addLocation(unit.getX(), unit.getY());
        }
    }

    public Unit getUnit(String key) {
        return units.get(key);
    }

    public ObjectMap.Values<Unit> getUnits() {
        return units.values();
    }

    public int getUnitCount() {
        return units.size;
    }

}
