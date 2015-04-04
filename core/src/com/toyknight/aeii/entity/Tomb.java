package com.toyknight.aeii.entity;

/**
 * Created by toyknight on 4/3/2015.
 */
public class Tomb extends Point {

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

}
