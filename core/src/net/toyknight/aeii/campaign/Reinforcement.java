package net.toyknight.aeii.campaign;

/**
 * @author toyknight 6/29/2016.
 */
public class Reinforcement {

    private final int index;
    private final int head;
    private final int map_x;
    private final int map_y;

    public Reinforcement(int index, int map_x, int map_y) {
        this(index, -1, map_x, map_y);
    }

    public Reinforcement(int index, int head, int map_x, int map_y) {
        this.index = index;
        this.head = head;
        this.map_x = map_x;
        this.map_y = map_y;
    }

    public int getIndex() {
        return index;
    }

    public int getHead() {
        return head;
    }

    public int getMapX() {
        return map_x;
    }

    public int getMapY() {
        return map_y;
    }

}
