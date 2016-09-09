package net.toyknight.aeii.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.manager.GameEvent;
import net.toyknight.aeii.network.entity.MapSnapshot;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.network.entity.RoomSetting;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * @author toyknight 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    private static NetworkListener listener;

    private static ServerConfiguration current_server;

    private static Client client;

    private static final Object RESPONSE_LOCK = new Object();

    private static JSONObject response;

    private static JSONArray event_queue;

    private static int service_id;

    private NetworkManager() {
    }

    public static void setNetworkListener(NetworkListener listener) {
        NetworkManager.listener = listener;
    }

    public static NetworkListener getListener() {
        return listener;
    }

    public static int getServiceID() {
        return service_id;
    }

    private static void tryConnect(ServerConfiguration server) throws IOException {
        current_server = server;
        client = new Client(90 * 1024, 90 * 1024);
        client.addListener(new Listener() {
            @Override
            public void disconnected(Connection connection) {
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onDisconnect();
                    }
                    synchronized (RESPONSE_LOCK) {
                        RESPONSE_LOCK.notifyAll();
                    }
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                onReceive(object);
            }
        });
        client.start();
        client.connect(5000, server.getAddress(), server.getPort());
    }

    public static boolean connect(ServerConfiguration server, String username, String v_string)
            throws AEIIException, IOException, JSONException {
        tryConnect(server);
        return (username == null || v_string == null) || requestAuthentication(username, v_string);
    }

    public static boolean connect(ServerConfiguration server) throws AEIIException, IOException, JSONException {
        return connect(server, null, null);
    }

    public static void disconnect() {
        if (isConnected()) {
            client.close();
        }
        client = null;
        current_server = null;
        service_id = -1;
    }

    public static boolean isConnected() {
        return client != null && client.isConnected();
    }

    public static void onReceive(Object object) {
        try {
            if (object instanceof String) {
                JSONObject packet = new JSONObject((String) object);
                switch (packet.getInt("type")) {
                    case NetworkConstants.RESPONSE:
                        synchronized (RESPONSE_LOCK) {
                            response = packet;
                            RESPONSE_LOCK.notifyAll();
                        }
                        break;
                    case NetworkConstants.NOTIFICATION:
                        onReceiveNotification(packet);
                        break;
                }
            }
        } catch (JSONException ex) {
            Gdx.app.log(TAG, "While receiving packet [" + ex.toString() + "]");
        }
    }

    public static void onReceiveNotification(JSONObject notification) throws JSONException {
        switch (notification.getInt("operation")) {
            case NetworkConstants.PLAYER_JOINING:
                int id = notification.getInt("player_id");
                String username = notification.getString("username");
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onPlayerJoin(id, username);
                    }
                }
                break;
            case NetworkConstants.PLAYER_LEAVING:
                id = notification.getInt("player_id");
                username = notification.getString("username");
                int host = notification.getInt("host_id");
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onPlayerLeave(id, username, host);
                    }
                }
                break;
            case NetworkConstants.PLAYER_RECONNECTING:
                id = notification.getInt("player_id");
                username = notification.getString("username");
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onPlayerReconnect(id, username);
                    }
                }
                break;
            case NetworkConstants.ALLOCATION_UPDATING:
                int[] types = new int[4];
                int[] alliance = new int[4];
                int[] allocation = new int[4];
                for (int team = 0; team < 4; team++) {
                    types[team] = notification.getJSONArray("types").getInt(team);
                    alliance[team] = notification.getJSONArray("alliance").getInt(team);
                    allocation[team] = notification.getJSONArray("allocation").getInt(team);
                }
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onAllocationUpdate(alliance, allocation, types);
                    }
                }
                break;
            case NetworkConstants.GAME_STARTING:
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onGameStart();
                    }
                }
                break;
            case NetworkConstants.GAME_EVENT:
                JSONObject event = notification.getJSONObject("game_event");
                event.put("remote", true);
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onReceiveGameEvent(event);
                    }
                }
                break;
            case NetworkConstants.MESSAGE:
                username = notification.getString("username");
                String message = notification.getString("message");
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onReceiveMessage(username, message);
                    }
                }
                break;
            default:
                //do nothing
        }
    }

    private static JSONObject sendRequest(JSONObject request) throws JSONException {
        response = null;
        client.sendTCP(request.toString());
        synchronized (RESPONSE_LOCK) {
            if (response == null) {
                try {
                    RESPONSE_LOCK.wait(10000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return isConnected() ? response : null;
    }

    private static void sendNotification(JSONObject notification) throws JSONException {
        client.sendTCP(notification.toString());
    }

    public static boolean requestAuthentication(String username, String v_string) throws JSONException, AEIIException {
        JSONObject request = createRequest(NetworkConstants.AUTHENTICATION);
        request.put("username", username);
        request.put("v_string", v_string);
        JSONObject response = sendRequest(request);
        if (response == null) {
            throw new AEIIException("Connection timeout");
        } else {
            boolean approved = response.getBoolean("approved");
            if (approved) {
                service_id = response.getInt("service_id");
            }
            return approved;
        }
    }

    public static Array<RoomSnapshot> requestRoomList() throws JSONException, AEIIException {
        JSONObject request = createRequest(NetworkConstants.LIST_ROOMS);
        JSONObject response = sendRequest(request);
        if (response == null) {
            throw new AEIIException("Connection timeout");
        } else {
            Array<RoomSnapshot> snapshots = new Array<RoomSnapshot>();
            for (int i = 0; i < response.getJSONArray("rooms").length(); i++) {
                snapshots.add(new RoomSnapshot(response.getJSONArray("rooms").getJSONObject(i)));
            }
            return snapshots;
        }
    }

    public static Array<PlayerSnapshot> requestIdlePlayerList() throws JSONException, AEIIException {
        JSONObject request = createRequest(NetworkConstants.LIST_IDLE_PLAYERS);
        JSONObject response = sendRequest(request);
        if (response == null) {
            throw new AEIIException("Connection timeout");
        } else {
            Array<PlayerSnapshot> snapshots = new Array<PlayerSnapshot>();
            for (int i = 0; i < response.getJSONArray("players").length(); i++) {
                snapshots.add(new PlayerSnapshot(response.getJSONArray("players").getJSONObject(i)));
            }
            return snapshots;
        }
    }

    public static RoomSetting requestCreateRoom(
            String map_name, Map map, int player_capacity, int start_gold, int unit_capacity, String password)
            throws JSONException {
        JSONObject request = createRequest(NetworkConstants.CREATE_ROOM);
        request.put("new_game", true);
        request.put("map_name", map_name);
        request.put("map", map.toJson());
        request.put("player_capacity", player_capacity);
        request.put("unit_capacity", unit_capacity);
        request.put("start_gold", start_gold);
        if (password.length() > 0) {
            request.put("password", password);
        }
        JSONObject response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            if (response.getBoolean("approved")) {
                return new RoomSetting(response.getJSONObject("room_setting"));
            } else {
                return null;
            }
        }
    }

    public static RoomSetting requestCreateRoom(
            GameCore game, int player_capacity, String password) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.CREATE_ROOM_SAVED);
        int player_count = game.getMap().getPlayerCount();
        String save_name = String.format("(%d) saved game", player_count);
        request.put("new_game", false);
        request.put("game", game.toJson());
        request.put("save_name", save_name);
        request.put("player_capacity", player_capacity);
        if (password.length() > 0) {
            request.put("password", password);
        }
        JSONObject response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            if (response.getBoolean("approved")) {
                return new RoomSetting(response.getJSONObject("room_setting"));
            } else {
                return null;
            }
        }
    }

    public static RoomSetting requestJoinRoom(long room_number, String password) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.JOIN_ROOM);
        request.put("room_id", room_number);
        request.put("password", password);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            if (response.getBoolean("approved")) {
                return new RoomSetting(response.getJSONObject("room_setting"));
            } else {
                return null;
            }
        }
    }

    public static RoomSetting requestReconnect(long room_id, int previous_id, String v_string, String username)
            throws IOException {
        if (current_server == null) {
            return null;
        } else {
            tryConnect(current_server);
            JSONObject request = createRequest(NetworkConstants.RECONNECT);
            request.put("room_id", room_id);
            request.put("previous_id", previous_id);
            request.put("v_string", v_string);
            request.put("username", username);
            JSONObject response = sendRequest(request);
            if (response == null) {
                return null;
            } else {
                if (response.getBoolean("approved")) {
                    service_id = response.getInt("service_id");
                    return new RoomSetting(response.getJSONObject("room_setting"));
                } else {
                    return null;
                }
            }
        }
    }

    public static boolean requestStartGame() throws JSONException {
        JSONObject request = createRequest(NetworkConstants.START_GAME);
        JSONObject response = sendRequest(request);
        return response != null && response.getBoolean("approved");
    }

    public static Array<MapSnapshot> requestMapList(String author, boolean symmetric) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.LIST_MAPS);
        if (author != null) {
            request.put("author", author);
        }
        request.put("symmetric", symmetric);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            JSONArray json_list = response.getJSONArray("maps");
            Array<MapSnapshot> map_list = new Array<MapSnapshot>();
            for (int i = 0; i < json_list.length(); i++) {
                MapSnapshot snapshot = new MapSnapshot(json_list.getJSONObject(i));
                map_list.add(snapshot);
            }
            return map_list;
        }
    }

    public static Map requestDownloadMap(int map_id) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.DOWNLOAD_MAP);
        request.put("id", map_id);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            if (response.getBoolean("approved")) {
                return new Map(response.getJSONObject("map"));
            } else {
                return null;
            }
        }
    }

    public static int requestUploadMap(Map map, String map_name) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.UPLOAD_MAP);
        request.put("map", map.toJson());
        request.put("map_name", map_name);
        JSONObject response = sendRequest(request);
        return response == null ? NetworkConstants.CODE_NETWORK_ERROR : response.getInt("code");
    }

    public static void notifyLeaveRoom() throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.PLAYER_LEAVING);
        sendNotification(notification);
    }

    public static void notifyAllocationUpdate(int[] alliance, int[] allocation, int[] types) throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.ALLOCATION_UPDATING);
        JSONArray types_json = new JSONArray();
        JSONArray alliance_json = new JSONArray();
        JSONArray allocation_json = new JSONArray();
        for (int team = 0; team < 4; team++) {
            types_json.put(types[team]);
            alliance_json.put(alliance[team]);
            allocation_json.put(allocation[team]);
        }
        notification.put("types", types_json);
        notification.put("alliance", alliance_json);
        notification.put("allocation", allocation_json);
        sendNotification(notification);
    }

    public static void resetEventQueue() {
        event_queue = new JSONArray();
    }

    public static void submitGameEvent(JSONObject event) {
        boolean remote = event.has("remote") && event.getBoolean("remote");
        if (!remote) {
            event_queue.put(event);
        }
    }

    public static void syncGameEvent(int manager_state) {
        if (event_queue.length() > 0) {
            JSONObject state_sync_event = GameEvent.create(GameEvent.MANAGER_STATE_SYNC, manager_state);
            event_queue.put(state_sync_event);
            JSONObject notification = createNotification(NetworkConstants.GAME_EVENT);
            notification.put("events", event_queue);
            sendNotification(notification);
            event_queue = new JSONArray();
        }
    }

    public static void sendMessage(String message) throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.MESSAGE);
        notification.put("message", message);
        sendNotification(notification);
    }

    private static JSONObject createRequest(int operation) throws JSONException {
        JSONObject packet = new JSONObject();
        packet.put("type", NetworkConstants.REQUEST);
        packet.put("operation", operation);
        return packet;
    }

    private static JSONObject createNotification(int operation) throws JSONException {
        JSONObject packet = new JSONObject();
        packet.put("type", NetworkConstants.NOTIFICATION);
        packet.put("operation", operation);
        return packet;
    }

}
