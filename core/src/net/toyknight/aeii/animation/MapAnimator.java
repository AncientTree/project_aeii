package net.toyknight.aeii.animation;

import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.screen.MapCanvas;

/**
 * @author toyknight 4/21/2015.
 */
public class MapAnimator extends Animator {

    private final ObjectSet<Position> locations = new ObjectSet<Position>();

    public MapAnimator(GameContext context) {
        super(context);
    }

    public MapAnimator(GameContext context, int x, int y) {
        this(context);
        if (x >= 0 && y >= 0) {
            this.addLocation(x, y);
        }
    }

    protected MapCanvas getCanvas() {
        return getContext().getCanvasRenderer().getCanvas();
    }

    protected int ts() {
        return getCanvas().ts();
    }

    public final void addLocation(int x, int y) {
        if (getCanvas().getMap().isWithinMap(x, y)) {
            addLocation(getCanvas().getMap().getPosition(x, y));
        }
    }

    public final void addLocation(Position position) {
        locations.add(position);
    }

    public final ObjectSet<Position> getLocations() {
        return locations;
    }

    public final boolean hasLocation(int x, int y) {
        return locations.contains(new Position(x, y));
    }

}
