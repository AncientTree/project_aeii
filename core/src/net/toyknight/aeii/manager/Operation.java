package net.toyknight.aeii.manager;

/**
 * @author toyknight 1/12/2016.
 */
public class Operation {

    public static final int SELECT = 0x0;
    public static final int SELECT_FINISH = 0x1;
    public static final int MOVE = 0x2;
    public static final int MOVE_FINISH = 0x3;
    public static final int MOVE_REVERSE = 0x4;
    public static final int MOVE_REVERSE_FINISH = 0x5;
    public static final int ATTACK = 0x6;
    public static final int COUNTER = 0x7;
    public static final int SUMMON = 0x8;
    public static final int HEAL = 0x9;
    public static final int REPAIR = 0x10;
    public static final int OCCUPY = 0x11;
    public static final int ACTION_FINISH = 0x12;
    public static final int BUY = 0x13;
    public static final int STANDBY = 0x14;
    public static final int NEXT_TURN = 0x15;
    public static final int TURN_START = 0x16;

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
