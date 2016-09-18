package net.toyknight.aeii.manager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class GameEvent {

    //common events
    public static final int STANDBY_FINISH = -0x1;
    public static final int CHECK_UNIT_DESTROY = -0x2;
    public static final int MANAGER_STATE_SYNC = 0x0;

    //skirmish events
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

    //campaign events
    public static final int CAMPAIGN_REINFORCE = 0x16;
    public static final int CAMPAIGN_MESSAGE = 0x17;
    public static final int CAMPAIGN_ATTACK = 0x18;
    public static final int CAMPAIGN_FOCUS = 0x19;
    public static final int CAMPAIGN_CLEAR = 0x20;
    public static final int CAMPAIGN_FAIL = 0x21;
    public static final int CAMPAIGN_CRYSTAL_STEAL = 0x22;
    public static final int CAMPAIGN_CREATE_UNIT = 0x23;
    public static final int CAMPAIGN_MOVE_UNIT = 0x24;
    public static final int CAMPAIGN_REMOVE_UNIT = 0x25;
    public static final int CAMPAIGN_CHANGE_TEAM = 0x26;
    public static final int CAMPAIGN_FLY_OVER = 0x27;
    public static final int CAMPAIGN_CARRY_UNIT = 0x28;
    public static final int CAMPAIGN_SHOW_OBJECTIVES = 0x29;
    public static final int CAMPAIGN_HAVENS_FURY = 0x30;
    public static final int CAMPAIGN_TILE_DESTROY = 0x31;
    public static final int CAMPAIGN_NOTIFICATION = 0x32;

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
