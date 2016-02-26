package com.toyknight.aeii.network;

/**
 * @author toyknight on 2/25/2016.
 */
public class NetworkConstants {

    //Packet types
    public static final int REQUEST = 0x1;
    public static final int RESPONSE = 0x2;
    public static final int NOTIFICATION = 0x3;

    //Requests
    public static final int AUTHENTICATION = 0x0;
    public static final int LIST_ROOMS = 0x1;
    public static final int JOIN_ROOM = 0x2;
    public static final int START_GAME = 0x3;
    public static final int CREATE_ROOM = 0x4;
    public static final int CREATE_ROOM_SAVED = 0x5;

    //Notifications
    public static final int PLAYER_JOINING = 0x1;
    public static final int PLAYER_LEAVING = 0x2;
    public static final int UPDATE_ALLOCATION = 0x3;
    public static final int GAME_START = 0x5;
    public static final int GAME_EVENT = 0x6;
    public static final int MESSAGE = 0x7;

}
