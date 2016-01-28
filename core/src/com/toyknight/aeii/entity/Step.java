package com.toyknight.aeii.entity;

/**
 * @author toyknight 4/17/2015.
 */
public class Step {

    private final int movement_point;
    private final Position position;

    public Step(Position position, int movement_point) {
        this.movement_point = movement_point;
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public int getMovementPoint() {
        return movement_point;
    }

}
