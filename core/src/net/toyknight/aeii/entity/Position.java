package net.toyknight.aeii.entity;

/**
 * @author toyknight 4/3/2015.
 */
public class Position {

    public int x;
    public int y;

    public Position(Position position) {
        this(position.x, position.y);
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
