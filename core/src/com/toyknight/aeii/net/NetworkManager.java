package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.net.serializable.*;
import com.toyknight.aeii.net.server.ServerConfiguration;
import com.toyknight.aeii.utils.ClassRegister;

import java.io.*;

/**
 * @author toyknight 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    private static final Object RESPONSE_LOCK = new Object();

    private static final ObjectMap<Long, Response> responses = new ObjectMap<Long, Response>();

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

    public static boolean connect(ServerConfiguration server, String username, String v_string) throws IOException {
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
        new ClassRegister().register(client.getKryo());
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
        PlayerSnapshot snapshot = new PlayerSnapshot();
        snapshot.id = id;
        snapshot.username = username;
        snapshot.is_host = false;
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

    private static void onAllocationUpdate(Integer[] alliance, Integer[] allocation, Integer[] types) {
        getRoomSetting().team_allocation = allocation;
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
        if (object instanceof Response) {
            synchronized (RESPONSE_LOCK) {
                Response response = (Response) object;
                responses.put(response.getRequestID(), response);
                RESPONSE_LOCK.notifyAll();
            }
        }
        if (object instanceof Notification) {
            onReceiveNotification((Notification) object);
        }
    }

    public static void onReceiveNotification(Notification notification) {
        switch (notification.getType()) {
            case Notification.PLAYER_JOINING:
                int id = (Integer) notification.getParameter(0);
                String username = (String) notification.getParameter(1);
                onPlayerJoin(id, username);
                break;
            case Notification.PLAYER_LEAVING:
                id = (Integer) notification.getParameter(0);
                username = (String) notification.getParameter(1);
                int host = (Integer) notification.getParameter(2);
                onPlayerLeave(id, username, host);
                break;
            case Notification.UPDATE_ALLOCATION:
                Integer[] alliance = (Integer[]) notification.getParameter(0);
                Integer[] allocation = (Integer[]) notification.getParameter(1);
                Integer[] types = (Integer[]) notification.getParameter(2);
                onAllocationUpdate(alliance, allocation, types);
                break;
            case Notification.GAME_START:
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onGameStart();
                    }
                }
                break;
            case Notification.GAME_EVENT:
                GameEvent event = (GameEvent) notification.getParameter(0);
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onReceiveGameEvent(event);
                    }
                }
                Gdx.app.log(TAG, "Receive " + event.toString());
                break;
            case Notification.MESSAGE:
                username = (String) notification.getParameter(0);
                String message = (String) notification.getParameter(1);
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

    private static Response sendRequest(Request request) {
        long id = request.getID();
        client.sendTCP(request);
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

    private static void sendNotification(Notification notification) {
        client.sendTCP(notification);
    }

    public static boolean requestAuthentication(String username, String v_string) {
        Request request = Request.getInstance(Request.AUTHENTICATION);
        request.setParameters(username, v_string);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            service_id = (Integer) response.getParameter(1);
            return (Boolean) response.getParameter(0);
        }
    }

    public static Array<RoomSnapshot> requestRoomList() {
        Request request = Request.getInstance(Request.LIST_ROOMS);
        Response response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            return (Array) response.getParameter(0);
        }
    }

    public static boolean requestCreateRoom(String map_name, Map map, int capacity, int gold, int population) {
        Request request = Request.getInstance(Request.CREATE_ROOM);
        request.setParameters(map_name, map, capacity, gold, population);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            room_setting = (RoomSetting) response.getParameter(0);
            return true;
        }
    }

    public static boolean requestCreateRoom(String save_name, GameCore game, int capacity) {
        Request request = Request.getInstance(Request.CREATE_ROOM_SAVED);
        request.setParameters(save_name, game, capacity);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            room_setting = (RoomSetting) response.getParameter(0);
            return true;
        }
    }

    public static boolean requestJoinRoom(long room_number) {
        Request request = Request.getInstance(Request.JOIN_ROOM);
        request.setParameters(room_number);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            room_setting = (RoomSetting) response.getParameter(0);
            return true;
        }
    }

    public static void notifyLeaveRoom() {
        Notification notification = new Notification(Notification.PLAYER_LEAVING);
        sendNotification(notification);
    }

    public static void notifyAllocationUpdate(Integer[] alliance, Integer[] allocation, Integer[] types) {
        Notification notification = new Notification(Notification.UPDATE_ALLOCATION);
        notification.setParameters(alliance, allocation, types);
        sendNotification(notification);
    }

    public static boolean requestStartGame() {
        Request request = Request.getInstance(Request.START_GAME);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            return (Boolean) response.getParameter(0);
        }
    }

    public static void sendGameEvent(GameEvent event) {
        Notification notification = new Notification(Notification.GAME_EVENT);
        notification.setParameters(event);
        sendNotification(notification);
        Gdx.app.log(TAG, "Send " + event.toString());
    }

    public static void sendMessage(String message) {
        Notification notification = new Notification(Notification.MESSAGE);
        notification.setParameters(message);
        sendNotification(notification);
    }

}
