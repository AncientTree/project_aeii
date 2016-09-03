package net.toyknight.aeii.robot;

import net.toyknight.aeii.entity.Position;

/**
 * @author toyknight 8/24/2016.
 */
public class Action {

    private final Position position;

    private final Position target;

    private final int type;

    private boolean moved = false;
    private boolean acted = false;

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

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public boolean isMoved() {
        return moved;
    }

    public void setActed(boolean acted) {
        this.acted = acted;
    }

    public boolean isActed() {
        return acted;
    }

}
