package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.network.entity.RoomSetting;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import net.toyknight.aeii.server.ServerContext;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.entities.Room;
import net.toyknight.aeii.server.RoomListener;
import org.json.JSONArray;
import org.json.JSONException;
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

    public boolean canJoin(Room room) {
        return room != null && !room.isGameOver() && room.getRemaining() > 0 && room.getHostID() != -1;
    }

    public boolean canStart(Room room, int player_id) {
        return room != null && room.isOpen() && room.isReadyForStart() && player_id == room.getHostID();
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
            if (host.getRoomID() < 0) {
                Room room = new Room(++current_room_id, username + "'s game", map, start_gold);
                room.setListener(this);
                room.setPassword(password);
                room.setMapName(map_name);
                room.setPlayerCapacity(player_capacity);
                room.setUnitCapacity(unit_capacity);
                room.setHostPlayer(host.getID());
                room.addPlayer(host.getID());
                host.setRoomID(room.getRoomID());
                rooms.put(room.getRoomID(), room);
                return createRoomSetting(room);
            } else {
                return null;
            }
        }
    }

    public RoomSetting createRoom(
            GameCore game, String username, String map_name, String password, int player_capacity, Player host) {
        synchronized (ROOM_LOCK) {
            if (host.getRoomID() < 0) {
                Room room = new Room(++current_room_id, username + "'s game", game);
                room.setListener(this);
                room.setPassword(password);
                room.setMapName(map_name);
                room.setPlayerCapacity(player_capacity);
                room.setHostPlayer(host.getID());
                room.addPlayer(host.getID());
                host.setRoomID(room.getRoomID());
                rooms.put(room.getRoomID(), room);
                return createRoomSetting(room);
            } else {
                return null;
            }
        }
    }

    public boolean tryStartGame(Player player) {
        long room_id = player.getRoomID();
        if (room_id >= 0) {
            Room room = getRoom(room_id);
            if (canStart(room, player.getID())) {
                room.startGame();
                getContext().getNotificationSender().notifyGameStarting(room);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void submitGameEvents(Player player, JSONArray events) throws JSONException {
        Room room = getRoom(player.getRoomID());
        if (room != null && player.getID() == room.getCurrentPlayerID()) {
            synchronized (room.GAME_LOCK) {
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.getJSONObject(i);
                    room.submitGameEvent(event, player.getID());
                }
            }
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
        synchronized (room.GAME_LOCK) {
            room_setting.manager_state = room.getManager().getState();
            Unit selected_unit = room.getManager().getSelectedUnit();
            room_setting.selected_unit_x = selected_unit == null ? -1 : selected_unit.getX();
            room_setting.selected_unit_y = selected_unit == null ? -1 : selected_unit.getY();
        }
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

    public void onAllocationUpdate(Player updater, JSONArray types, JSONArray alliance, JSONArray allocation) {
        Room room = getRoom(updater.getRoomID());
        if (room != null && room.getHostID() == updater.getID()) {
            for (int team = 0; team < 4; team++) {
                room.setPlayerType(team, types.getInt(team));
                room.setAlliance(team, alliance.getInt(team));
                room.setAllocation(team, allocation.getInt(team));
            }
            getContext().getNotificationSender().
                    notifyAllocationUpdating(room, updater.getID(), alliance, allocation, types);
        }
    }

    public RoomSetting onPlayerJoinRoom(Player player, long room_id, String password) {
        Room room = getRoom(room_id);
        if (canJoin(room) && player.getRoomID() < 0 && room.checkPassword(password)) {
            room.addPlayer(player.getID());
            player.setRoomID(room_id);
            getContext().getNotificationSender().notifyPlayerJoining(room, player.getID(), player.getUsername());
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
                    getContext().getNotificationSender().
                            notifyPlayerLeaving(room, player.getID(), player.getUsername(), room.getHostID());
                    getContext().getNotificationSender().notifyAllocationUpdating(room, -1);
                }
            }
        }
    }

    public RoomSetting onPlayerReconnect(Player player, int previous_id, long room_id) {
        Room room = getRoom(room_id);
        if (canJoin(room) && player.getRoomID() < 0) {
            boolean success = room.restorePlayer(previous_id, player.getID());
            if (success) {
                player.setRoomID(room_id);
                getContext().getNotificationSender().notifyPlayerReconnecting(room, player.getID(), player.getUsername());
                getContext().getNotificationSender().notifyAllocationUpdating(room, player.getID());
                return createRoomSetting(room);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void onGameEventExecuted(Room room, JSONObject event, int submitter) {
        for (int player_id : room.getPlayers()) {
            if (player_id != submitter) {
                getContext().getNotificationSender().syncGameEvent(player_id, event);
            }
        }
    }

    @Override
    public void onCheatingDetected(Room room, int player_id, Throwable cause) {
        getContext().getPlayerManager().disconnectPlayer(player_id, "/cheating", 5000);
        Log.info(TAG, String.format("Cheating detected in room [%d] by player [%d]", room.getRoomID(), player_id));
    }

}
