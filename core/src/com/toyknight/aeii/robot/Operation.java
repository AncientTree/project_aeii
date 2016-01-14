package com.toyknight.aeii.robot;

/**
 * @author toyknight 1/12/2016.
 */
public class Operation {

    public static final int SELECT = 0x0;
    public static final int MOVE = 0x1;
    public static final int ATTACK = 0x2;
    public static final int SUMMON = 0x3;
    public static final int HEAL = 0x4;
    public static final int STANDBY = 0x5;
    public static final int END_TURN = 0x6;

    private final int type;
    private final int map_x;
    private final int map_y;

    public Operation(int type, int map_x, int map_y) {
        this.type = type;
        this.map_x = map_x;
        this.map_y = map_y;
    }

    public int getType() {
        return type;
    }

    public int getMapX() {
        return map_x;
    }

    public int getMapY() {
        return map_y;
    }

}
