package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.server.entities.Room;

/**
 * @author toyknight 8/14/2016.
 */
public class RoomManager {

    private final Object ROOM_LOCK = new Object();

    private final ObjectMap<Long, Room> rooms = new ObjectMap<Long, Room>();




}
