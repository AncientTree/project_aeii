package com.toyknight.aeii.entity;

/**
 * @author toyknight 4/3/2015.
 */
public class Tomb extends Position {

    private int remains = 1;

    public Tomb(int x, int y) {
        super(x, y);
    }

    public Tomb(Tomb tomb) {
        this(tomb.x, tomb.y);
    }

    public void update() {
        if (remains >= 0) {
            remains--;
        }
    }

    public int getRemains() {
        return remains;
    }

    public void setRemains(int remains) {
        this.remains = remains;
    }

}
