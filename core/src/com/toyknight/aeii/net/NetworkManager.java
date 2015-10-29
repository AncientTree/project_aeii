package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.serializable.*;
import com.toyknight.aeii.utils.ClassRegister;

import java.io.*;

/**
 * @author toyknight 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    private final Object RESPONSE_LOCK = new Object();
    private final ObjectMap<Long, Response> responses;

    private NetworkListener listener;

    private Client client;

    private int service_id;

    public NetworkManager() {
        responses = new ObjectMap<Long, Response>();
    }

    public void setNetworkListener(NetworkListener listener) {
        this.listener = listener;
    }

    public NetworkListener getListener() {
        return listener;
    }

    public boolean connect(ServerConfig server, String username, String v_string) throws IOException {
        responses.clear();
        client = new Client();
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

    public void disconnect() {
        if (isConnected()) {
            try {
                client.dispose();
            } catch (IOException ex) {
                Gdx.app.log(TAG, ex.toString());
            }
        }
        client = null;
        synchronized (RESPONSE_LOCK) {
            RESPONSE_LOCK.notifyAll();
        }

    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public void onReceive(Object object) {
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

    public void onReceiveNotification(Notification notification) {
        switch (notification.getType()) {
            case Notification.PLAYER_JOINING:
                int id = (Integer) notification.getParameter(0);
                String username = (String) notification.getParameter(1);
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onPlayerJoin(id, username);
                    }
                }
                break;
            case Notification.PLAYER_LEAVING:
                id = (Integer) notification.getParameter(0);
                username = (String) notification.getParameter(1);
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onPlayerLeave(id, username);
                    }
                }
                break;
            case Notification.UPDATE_ALLOCATION:
                Integer[] allocation = (Integer[]) notification.getParameter(0);
                Integer[] types = (Integer[]) notification.getParameter(1);
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onAllocationUpdate(allocation, types);
                    }
                }
            case Notification.UPDATE_ALLIANCE:
                Integer[] alliance = (Integer[]) notification.getParameter(0);
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onAllianceUpdate(alliance);
                    }
                }
                break;
            case Notification.GAME_START:
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onGameStart(null);
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

    public int getServiceID() {
        return service_id;
    }

    private Response sendRequest(Request request) {
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

    private void sendNotification(Notification notification) {
        client.sendTCP(notification);
    }

    public boolean requestAuthentication(String username, String v_string) {
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

    public Array<RoomSnapshot> requestRoomList() {
        Request request = Request.getInstance(Request.LIST_ROOMS);
        Response response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            return (Array) response.getParameter(0);
        }
    }

    public RoomConfiguration requestCreateRoom(String map_name, Map map, int capacity, int gold, int population) {
        Request request = Request.getInstance(Request.CREATE_ROOM);
        request.setParameters(map_name, map, capacity, gold, population);
        Response response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            return (RoomConfiguration) response.getParameter(0);
        }
    }

    public Object requestJoinRoom(long room_number) {
        Request request = Request.getInstance(Request.JOIN_ROOM);
        request.setParameters(room_number);
        Response response = sendRequest(request);
        if (response == null) {
            return null;
        } else {
            return response.getParameter(0);
        }
    }

    public void notifyLeaveRoom() {
        Notification notification = new Notification(Notification.PLAYER_LEAVING);
        sendNotification(notification);
    }

    public void notifyAllocationUpdate(Integer[] allocation, Integer[] types) {
        Notification notification = new Notification(Notification.UPDATE_ALLOCATION);
        notification.setParameters(allocation, types);
        sendNotification(notification);
    }

    public void notifyAllianceUpdate(Integer[] alliance) {
        Notification notification = new Notification(Notification.UPDATE_ALLIANCE);
        notification.setParameters((Object) alliance);
        sendNotification(notification);
    }

    public boolean requestStartGame() {
        Request request = Request.getInstance(Request.START_GAME);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
            return (Boolean) response.getParameter(0);
        }
    }

    public boolean requestStartGame(GameSave game_save) {
//        synchronized (OUTPUT_LOCK) {
//            sendInteger(REQUEST);
//            sendInteger(Request.RESUME_GAME);
//            sendObject(game_save);
//        }
//        synchronized (INPUT_LOCK) {
//            try {
//                boolean approved = ois.readBoolean();
//                INPUT_LOCK.notifyAll();
//                return approved;
//            } catch (IOException ex) {
//                INPUT_LOCK.notifyAll();
//                throw ex;
//            }
//        }
        return false;
    }

    public void sendGameEvent(GameEvent event) {
        Notification notification = new Notification(Notification.GAME_EVENT);
        notification.setParameters(event);
        sendNotification(notification);
        Gdx.app.log(TAG, "Send " + event.toString());
    }

    public void sendMessage(String message) {
        Notification notification = new Notification(Notification.MESSAGE);
        notification.setParameters(message);
        sendNotification(notification);
    }

}
