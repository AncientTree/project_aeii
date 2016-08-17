package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.NetworkConstants;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.network.entity.RoomSetting;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import net.toyknight.aeii.server.ServerContext;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.entities.Room;
import net.toyknight.aeii.server.RoomListener;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author toyknight 8/14/2016.
 */
public class RoomManager implements RoomListener {

    public static final String TAG = "ROOM MANAGER";

    private final Object ROOM_LOCK = new Object();

    private final ObjectMap<Long, Room> rooms = new ObjectMap<Long, Room>();

    private final ServerContext context;

    private long current_room_id = 0;

    public RoomManager(ServerContext context) {
        this.context = context;
    }

    public ServerContext getContext() {
        return context;
    }

    public boolean isRoomAvailable(Room room) {
        return room != null && !room.isGameOver() && room.getRemaining() > 0 && room.getHostID() != -1;
    }

    public Room getRoom(long room_id) {
        synchronized (ROOM_LOCK) {
            return rooms.get(room_id, null);
        }
    }

    public void removeRoom(long room_id) {
        synchronized (ROOM_LOCK) {
            rooms.remove(room_id);
        }
        Log.info(TAG, String.format("Room [%d] is disposed", room_id));
    }

    public RoomSetting createRoom(
            Map map,
            String username,
            String map_name,
            String password,
            int player_capacity,
            int unit_capacity,
            int start_gold,
            Player host) {
        synchronized (ROOM_LOCK) {
            Room room = new Room(++current_room_id, username + "'s game", map, start_gold);
            room.setPassword(password);
            room.setMapName(map_name);
            room.setPlayerCapacity(player_capacity);
            room.setUnitCapacity(unit_capacity);
            room.setHostPlayer(host.getID());
            room.addPlayer(host.getID());
            host.setRoomID(room.getRoomID());
            rooms.put(room.getRoomID(), room);
            return createRoomSetting(room);
        }
    }

    public RoomSetting createRoom(
            GameCore game, String username, String map_name, String password, int player_capacity, Player host) {
        synchronized (ROOM_LOCK) {
            Room room = new Room(++current_room_id, username + "'s game", game);
            room.setMapName(map_name);
            room.setPassword(password);
            room.setPlayerCapacity(player_capacity);
            room.setHostPlayer(host.getID());
            room.addPlayer(host.getID());
            host.setRoomID(room.getRoomID());
            rooms.put(room.getRoomID(), room);
            return createRoomSetting(room);
        }
    }

    public Array<RoomSnapshot> getRoomSnapshots() {
        synchronized (ROOM_LOCK) {
            Array<RoomSnapshot> snapshots = new Array<RoomSnapshot>();
            for (Room room : rooms.values()) {
                snapshots.add(room.createSnapshot());
            }
            return snapshots;
        }
    }

    public RoomSetting createRoomSetting(Room room) {
        RoomSetting room_setting = new RoomSetting();
        room_setting.room_id = room.getRoomID();
        room_setting.started = !room.isOpen();
        room_setting.host = room.getHostID();
        room_setting.allocation = room.getAllocation();
        room_setting.start_gold = room.getStartGold();
        room_setting.max_population = room.getUnitCapacity();
        ObjectSet<Integer> players = room.getPlayers();
        room_setting.players = new Array<PlayerSnapshot>();
        room_setting.game = room.getGameCopy();
        for (int id : players) {
            Player player = getContext().getPlayerManager().getPlayer(id);
            if (player != null) {
                PlayerSnapshot snapshot = player.createSnapshot();
                snapshot.is_host = room.getHostID() == id;
                room_setting.players.add(snapshot);
            }
        }
        return room_setting;
    }

    public void notifyAllocationUpdate(
            Room room, int updater, JSONArray alliance, JSONArray allocation, JSONArray types) {
        JSONObject notification = getContext().createPacket(NetworkConstants.NOTIFICATION);
        notification.put("operation", NetworkConstants.UPDATE_ALLOCATION);
        notification.put("types", types);
        notification.put("alliance", alliance);
        notification.put("allocation", allocation);

        for (int player_id : room.getPlayers()) {
            if (player_id != updater) {
                getContext().submitNotification(player_id, notification);
            }
        }
    }

    public void notifyPlayerJoin(Room room, int joiner, String username) {
        JSONObject notification = getContext().createPacket(NetworkConstants.NOTIFICATION);
        notification.put("operation", NetworkConstants.PLAYER_JOINING);
        notification.put("player_id", joiner);
        notification.put("username", username);

        for (int player_id : room.getPlayers()) {
            if (player_id != joiner) {
                getContext().submitNotification(player_id, notification);
            }
        }
    }

    public void notifyPlayerLeave(Room room, int leaver, String username, int host_id) {
        JSONObject notification = getContext().createPacket(NetworkConstants.NOTIFICATION);
        notification.put("operation", NetworkConstants.PLAYER_LEAVING);
        notification.put("player_id", leaver);
        notification.put("username", username);
        notification.put("host_id", host_id);

        for (int player_id : room.getPlayers()) {
            if (player_id != leaver) {
                getContext().submitNotification(player_id, notification);
            }
        }
    }

    public void onAllocationUpdate(Player updater, JSONArray types, JSONArray alliance, JSONArray allocation) {
        Room room = getRoom(updater.getRoomID());
        if (room != null && room.getHostID() == updater.getID()) {
            for (int team = 0; team < 4; team++) {
                room.setPlayerType(team, types.getInt(team));
                room.setAlliance(team, alliance.getInt(team));
                room.setAllocation(team, allocation.getInt(team));
            }
            notifyAllocationUpdate(room, updater.getID(), alliance, allocation, types);
        }
    }

    public RoomSetting onPlayerJoinRoom(Player player, long room_id, String password) {
        Room room = getRoom(room_id);
        if (isRoomAvailable(room) && player.getRoomID() < 0 && room.checkPassword(password)) {
            room.addPlayer(player.getID());
            player.setRoomID(room_id);
            notifyPlayerJoin(room, player.getID(), player.getUsername());
            return createRoomSetting(room);
        } else {
            return null;
        }
    }

    public void onPlayerLeaveRoom(Player player) {
        long room_id = player.getRoomID();
        if (room_id >= 0) {
            Room room = getRoom(room_id);
            if (room != null) {
                room.removePlayer(player.getID());
                player.setRoomID(-1);
                Log.info(TAG, String.format("%s leaves room [%d]", player.toString(), room_id));
                if (room.getPlayerCapacity() == room.getRemaining()) {
                    room.dispose();
                    removeRoom(room_id);
                } else {
                    notifyPlayerLeave(room, player.getID(), player.getUsername(), room.getHostID());
                    JSONArray types = new JSONArray();
                    JSONArray alliance = new JSONArray();
                    JSONArray allocation = new JSONArray();
                    for (int team = 0; team < 4; team++) {
                        types.put(room.getPlayerType(team));
                        alliance.put(room.getAlliance(team));
                        allocation.put(room.getAllocation(team));
                    }
                    notifyAllocationUpdate(room, -1, alliance, allocation, types);
                }
            }
        }
    }

    @Override
    public void onGameEventExecuted(Room room, JSONObject event, int submitter) {
        for (int player_id : room.getPlayers()) {
            if (player_id != submitter) {
                getContext().syncGameEvent(player_id, event);
            }
        }
    }

    @Override
    public void onCheatingDetected(Room room, int player_id, Throwable cause) {

    }

}
