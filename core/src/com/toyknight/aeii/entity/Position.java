package com.toyknight.aeii.entity;

import java.io.Serializable;

/**
 * @author toyknight 4/3/2015.
 */
public class Position implements Serializable {

    private static final long serialVersionUID = 4032015L;

    public int x;
    public int y;

    public Position() {
        this(0, 0);
    }


    public Position(Position p) {
        this(p.x, p.y);
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position pt = (Position) obj;
            return (x == pt.x) && (y == pt.y);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.x;
        hash = 89 * hash + this.y;
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }

}
