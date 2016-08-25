package net.toyknight.aeii.robot;

import net.toyknight.aeii.entity.Position;

/**
 * @author toyknight 8/24/2016.
 */
public class Action {

    private final Position position;

    private final Position target;

    private final int type;

    public Action(Position position, Position target, int type) {
        this.position = position;
        this.target = target;
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public Position getTarget() {
        return target;
    }

    public int getType() {
        return type;
    }

}
