package com.toyknight.aeii.entity;

import java.io.Serializable;

/**
 * @author toyknight 4/3/2015.
 */
public class Tomb extends Point implements Serializable {

    private static final long serialVersionUID = 4032015L;

    private int remains = 1;

    public Tomb() {
    }

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
