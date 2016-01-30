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
    public static final int BUY = 0x7;
    public static final int OCCUPY = 0x8;
    public static final int REPAIR = 0x9;

    private final int type;

    private final int[] parameters;

    public Operation(int type, int... parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public int getType() {
        return type;
    }

    public int getParameter(int index) {
        return parameters[index];
    }

}
