package com.toyknight.aeii.manager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class GameEvent {

    private static final String TAG = "Event";

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
    public static final int CHECK_UNIT_DESTROY = 0x17;
    public static final int CHECK_TEAM_DESTROY = 0x18;

    public static JSONObject create(int type, Object... params) throws JSONException {
        JSONObject event = new JSONObject();
        event.put("type", type);
        JSONArray parameters = new JSONArray();
        for (Object param : params) {
            parameters.put(param);
        }
        event.put("parameters", parameters);
        return event;
    }

}
