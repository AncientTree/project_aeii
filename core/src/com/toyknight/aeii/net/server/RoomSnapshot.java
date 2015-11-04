package com.toyknight.aeii.net.server;

import java.io.Serializable;

/**
 * @author toyknight 8/27/2015.
 */
public class RoomSnapshot implements Serializable {

    private static final long serialVersionUID = 9112015L;

    public long room_number;
    public boolean open;
    public String room_name;
    public String map_name;
    public int capacity;
    public int remaining;

    @Override
    public String toString() {
        String str = room_name + " | Map: " + map_name + " | (" + (capacity - remaining) + "/" + capacity + ")";
        if (!open) {
            str += " - Started";
        }
        return str;
    }

}
