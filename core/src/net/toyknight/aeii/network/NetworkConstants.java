package net.toyknight.aeii.network;

/**
 * @author toyknight on 2/25/2016.
 */
public class NetworkConstants {

    //Packet types
    public static final int REQUEST = 0x1;
    public static final int RESPONSE = 0x2;
    public static final int NOTIFICATION = 0x3;

    //Requests
    public static final int AUTHENTICATION = 0x4;
    public static final int LIST_ROOMS = 0x5;
    public static final int JOIN_ROOM = 0x6;
    public static final int START_GAME = 0x7;
    public static final int CREATE_ROOM = 0x8;
    public static final int CREATE_ROOM_SAVED = 0x9;
    public static final int LIST_MAPS = 0x91;
    public static final int UPLOAD_MAP = 0x92;
    public static final int DOWNLOAD_MAP = 0x93;
    public static final int LIST_IDLE_PLAYERS = 0x94;

    //Notifications
    public static final int PLAYER_JOINING = 0x10;
    public static final int PLAYER_LEAVING = 0x11;
    public static final int ALLOCATION_UPDATING = 0x12;
    public static final int GAME_STARTING = 0x13;
    public static final int GAME_EVENT = 0x14;
    public static final int MESSAGE = 0x15;

    //Admin operations
    public static final int SHUTDOWN = 0x100;
    public static final int DELETE_MAP = 0x101;
    public static final int UPDATE_MAP = 0x102;

    //Response codes
    public static final int CODE_NETWORK_ERROR = -1;
    public static final int CODE_SERVER_ERROR = 0x0;
    public static final int CODE_OK = 0x1;
    public static final int CODE_MAP_EXISTING = 0x10;

}
