package com.toyknight.aeii.manager;

/**
 * @author toyknight 4/3/2015.
 */
public class GameEvent {

    public static final int NONE = 0x0;
    public static final int BUY = 0x1;
    public static final int MOVE = 0x2;
    public static final int HEAL = 0x3;
    public static final int OCCUPY = 0x4;
    public static final int REPAIR = 0x5;
    public static final int SUMMON = 0x6;
    public static final int ATTACK = 0x7;
    public static final int SELECT = 0x8;
    public static final int STANDBY = 0x9;
    public static final int REVERSE = 0x10;
    public static final int NEXT_TURN = 0x11;
    public static final int HP_CHANGE = 0x12;
    public static final int UNIT_DESTROY = 0x13;
    public static final int TILE_DESTROY = 0x14;
    public static final int GAIN_EXPERIENCE = 0x15;
    public static final int ACTION_FINISH = 0x16;
    public static final int CHECK_TEAM_DESTROY = 0x17;

    private final int type;
    private final Object[] params;

    public GameEvent() {
        this(NONE);
    }

    public GameEvent(int type, Object... params) {
        this.type = type;
        this.params = params;
    }

    public int getType() {
        return type;
    }

    public Object getParameter(int index) {
        return params[index];
    }

}
