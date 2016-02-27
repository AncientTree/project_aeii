package com.toyknight.aeii.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.network.entity.*;
import com.toyknight.aeii.network.server.ServerConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * @author toyknight 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    private static final Object RESPONSE_LOCK = new Object();

    private static final ObjectMap<Long, JSONObject> responses = new ObjectMap<Long, JSONObject>();

    private static NetworkListener listener;

    private static Client client;

    private static int service_id;

    private static RoomSetting room_setting;

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

    public static RoomSetting getRoomSetting() {
        return room_setting;
    }

    public static boolean connect(ServerConfiguration server, String username, String v_string)
            throws IOException, JSONException {
        responses.clear();
        client = new Client(65536, 65536);
        client.addListener(new Listener() {
            @Override
            public void disconnected(Connection connection) {
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onDisconnect();
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
        return requestAuthentication(username, v_string);
    }

    public static void disconnect() {
        if (isConnected()) {
            client.close();
        }
        client = null;
        service_id = -1;
        synchronized (RESPONSE_LOCK) {
            RESPONSE_LOCK.notifyAll();
        }
    }

    public static boolean isConnected() {
        return client != null && client.isConnected();
    }

    private static void onPlayerJoin(int id, String username) {
        PlayerSnapshot snapshot = new PlayerSnapshot(id, username, false);
        getRoomSetting().players.add(snapshot);
        if (listener != null) {
            synchronized (GameContext.RENDER_LOCK) {
                listener.onPlayerJoin(id, username);
            }
        }
    }

    private static void onPlayerLeave(int id, String username, int host) {
        int index = -1;
        for (int i = 0; i < getRoomSetting().players.size; i++) {
            PlayerSnapshot player = getRoomSetting().players.get(i);
            player.is_host = (player.id == host);
            if (id == player.id && index < 0) {
                index = i;
            }
        }
        if (index >= 0) {
            getRoomSetting().players.removeIndex(index);
        }
        getRoomSetting().host = host;
        if (listener != null) {
            synchronized (GameContext.RENDER_LOCK) {
                listener.onPlayerLeave(id, username);
            }
        }
    }

    private static void onAllocationUpdate(int[] alliance, int[] allocation, int[] types) {
        getRoomSetting().allocation = allocation;
        if (!getRoomSetting().started) {
            for (int team = 0; team < 4; team++) {
                getRoomSetting().game.getPlayer(team).setAlliance(alliance[team]);
                getRoomSetting().game.getPlayer(team).setType(types[team]);
            }
        }
        if (listener != null) {
            synchronized (GameContext.RENDER_LOCK) {
                listener.onAllocationUpdate();
            }
        }
    }

    public static void onReceive(Object object) {
        try {
            if (object instanceof String) {
                JSONObject packet = new JSONObject((String) object);
                switch (packet.getInt("type")) {
                    case NetworkConstants.RESPONSE:
                        synchronized (RESPONSE_LOCK) {
                            responses.put(packet.getLong("request_id"), packet);
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
                onPlayerJoin(id, username);
                break;
            case NetworkConstants.PLAYER_LEAVING:
                id = notification.getInt("player_id");
                username = notification.getString("username");
                int host = notification.getInt("host_id");
                onPlayerLeave(id, username, host);
                break;
            case NetworkConstants.UPDATE_ALLOCATION:
                int[] types = new int[4];
                int[] alliance = new int[4];
                int[] allocation = new int[4];
                for (int team = 0; team < 4; team++) {
                    types[team] = notification.getJSONArray("types").getInt(team);
                    alliance[team] = notification.getJSONArray("alliance").getInt(team);
                    allocation[team] = notification.getJSONArray("allocation").getInt(team);
                }
                onAllocationUpdate(alliance, allocation, types);
                break;
            case NetworkConstants.GAME_START:
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onGameStart();
                    }
                }
                break;
            case NetworkConstants.GAME_EVENT:
                JSONObject event = notification.getJSONObject("game_event");
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
        long id = request.getLong("request_id");
        client.sendTCP(request.toString());
        synchronized (RESPONSE_LOCK) {
            while (responses.get(id) == null && isConnected()) {
                try {
                    RESPONSE_LOCK.wait();
                } catch (InterruptedException ex) {
                    //do nothing
                }
            }
            if (isConnected()) {
                return responses.get(id);
            } else {
                return null;
            }
        }
    }

    private static void sendNotification(JSONObject notification) throws JSONException {
        client.sendTCP(notification.toString());
    }

    public static boolean requestAuthentication(String username, String v_string) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.AUTHENTICATION);
        request.put("username", username);
        request.put("v_string", v_string);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            service_id = response.getInt("service_id");
            return response.getBoolean("approved");
        }
    }

    public static Array<RoomSnapshot> requestRoomList() throws JSONException {
        JSONObject request = createRequest(NetworkConstants.LIST_ROOMS);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return new Array<RoomSnapshot>();
        } else {
            Array<RoomSnapshot> snapshots = new Array<RoomSnapshot>();
            for (int i = 0; i < response.getJSONArray("rooms").length(); i++) {
                snapshots.add(new RoomSnapshot(response.getJSONArray("rooms").getJSONObject(i)));
            }
            return snapshots;
        }
    }

    public static boolean requestCreateRoom(String map_name, Map map, int capacity, int start_gold, int max_population)
            throws JSONException {
        JSONObject request = createRequest(NetworkConstants.CREATE_ROOM);
        request.put("map_name", map_name);
        request.put("map", map.toJson());
        request.put("capacity", capacity);
        request.put("start_gold", start_gold);
        request.put("max_population", max_population);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            if (response.getBoolean("approved")) {
                room_setting = new RoomSetting(response.getJSONObject("room_setting"));
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean requestCreateRoom(String save_name, GameCore game, int capacity) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.CREATE_ROOM_SAVED);
        request.put("save_name", save_name);
        request.put("game", game.toJson());
        request.put("capacity", capacity);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            if (response.getBoolean("approved")) {
                room_setting = new RoomSetting(response.getJSONObject("room_setting"));
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean requestJoinRoom(long room_number) throws JSONException {
        JSONObject request = createRequest(NetworkConstants.JOIN_ROOM);
        request.put("room_number", room_number);
        JSONObject response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            if (response.getBoolean("approved")) {
                room_setting = new RoomSetting(response.getJSONObject("room_setting"));
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean requestStartGame() throws JSONException {
        JSONObject request = createRequest(NetworkConstants.START_GAME);
        JSONObject response = sendRequest(request);
        return response != null && response.getBoolean("approved");
    }

    public static void notifyLeaveRoom() throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.PLAYER_LEAVING);
        sendNotification(notification);
    }

    public static void notifyAllocationUpdate(int[] alliance, int[] allocation, int[] types)
            throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.UPDATE_ALLOCATION);
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

    public static void sendGameEvent(JSONObject event) throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.GAME_EVENT);
        notification.put("game_event", event);
        sendNotification(notification);
    }

    public static void sendMessage(String message) throws JSONException {
        JSONObject notification = createNotification(NetworkConstants.MESSAGE);
        notification.put("message", message);
        sendNotification(notification);
    }

    private static JSONObject createRequest(int operation) throws JSONException {
        JSONObject packet = new JSONObject();
        packet.put("request_id", System.currentTimeMillis());
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
