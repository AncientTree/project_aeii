package com.toyknight.aeii.net;

import java.io.Serializable;

/**
 * @author toyknight 10/28/2015.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 10282015L;

    public static final int PLAYER_JOINING = 0x1;

    public static final int PLAYER_LEAVING = 0x2;

    public static final int UPDATE_ALLOCATION = 0x3;

    public static final int UPDATE_ALLIANCE = 0x4;

    public static final int GAME_START = 0x5;

    public static final int GAME_EVENT = 0x6;

    public static final int MESSAGE = 0x7;

    public static final int PLAYER_RECONNECTING = 0x8;

    private final int type;

    private Object[] params;

    public Notification() {
        this(-1);
    }

    public Notification(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setParameters(Object... params) {
        this.params = params;
    }

    public Object getParameter(int index) {
        return params[index];
    }

}
