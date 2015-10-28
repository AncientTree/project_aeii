package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.serializable.*;
import com.toyknight.aeii.utils.ClassRegister;

import java.io.*;
import java.util.ArrayList;

/**
 * @author toyknight 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    private Client client;

    private final Object RESPONSE_LOCK = new Object();
    private ObjectMap<Long, Response> responses;

    public static final int REQUEST = 0x1;
    public static final int RESPONSE = 0x2;

    private final Object INPUT_LOCK = new Object();
    private final Object OUTPUT_LOCK = new Object();

    private NetworkListener listener;

    private boolean running;

    private Socket server_socket;

    private String service_name;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public NetworkManager() {
        responses = new ObjectMap<Long, Response>();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        this.running = false;
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
        try {
            client.dispose();
        } catch (IOException ex) {
            Gdx.app.log(TAG, ex.toString());
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
    }

    public String getServiceName() {
        return service_name;
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

    public boolean requestAuthentication(String username, String v_string) {
        Request request = Request.getInstance(Request.AUTHENTICATION);
        request.setParameters(username, v_string);
        Response response = sendRequest(request);
        if (response == null) {
            return false;
        } else {
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

    public Object requestJoinRoom(long room_number) throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.JOIN_ROOM);
            sendLong(room_number);
        }
        synchronized (INPUT_LOCK) {
            try {
                Object response = ois.readObject();
                INPUT_LOCK.notifyAll();
                return response;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public void requestLeaveRoom() throws IOException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.LEAVE_ROOM);
        }
    }

    public void requestUpdateAllocation(String[] allocation, Integer[] types) throws IOException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.UPDATE_ALLOCATION);
            for (int team = 0; team < 4; team++) {
                sendString(allocation[team]);
                sendInteger(types[team]);
            }
        }
    }

    public void requestUpdateAlliance(Integer[] alliance) throws IOException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.UPDATE_ALLIANCE);
            for (int team = 0; team < 4; team++) {
                sendInteger(alliance[team]);
            }
        }
    }

    public boolean requestStartGame() throws IOException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.START_GAME);
        }
        synchronized (INPUT_LOCK) {
            try {
                boolean approved = ois.readBoolean();
                INPUT_LOCK.notifyAll();
                return approved;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public boolean requestStartGame(GameSave game_save) throws IOException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.RESUME_GAME);
            sendObject(game_save);
        }
        synchronized (INPUT_LOCK) {
            try {
                boolean approved = ois.readBoolean();
                INPUT_LOCK.notifyAll();
                return approved;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public void sendGameEvent(GameEvent event) {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.GAME_EVENT);
            sendObject(event);
        }
        Gdx.app.log(TAG, "Send " + event.toString());
    }

    public void requestSubmitMessage(String message) {
        synchronized (OUTPUT_LOCK) {
            sendInteger(REQUEST);
            sendInteger(Request.MESSAGE);
            sendString(message);
        }
    }

    public void sendInteger(int n) {
        boolean sent = false;
        while (!sent && isConnected()) {
            try {
                oos.writeInt(n);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                Gdx.app.log(TAG, ex.toString());
            }
        }
    }

    public void sendLong(long n) {
        boolean sent = false;
        while (!sent && isConnected()) {
            try {
                oos.writeLong(n);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                Gdx.app.log(TAG, ex.toString());
            }
        }
    }

    public void sendObject(Object obj) {
        boolean sent = false;
        while (!sent && isConnected()) {
            try {
                oos.writeObject(obj);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                Gdx.app.log(TAG, ex.toString());
            }
        }
    }

    public void sendString(String str) {
        boolean sent = false;
        while (!sent && isConnected()) {
            try {
                oos.writeUTF(str);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                Gdx.app.log(TAG, ex.toString());
            }
        }
    }

    private class ReceivingThread extends Thread {

        @Override
        public void run() {
            while (server_socket.isConnected() && isRunning()) {
                try {
                    synchronized (INPUT_LOCK) {
                        int type = ois.readInt();
                        switch (type) {
                            case REQUEST:
                                int request = ois.readInt();
                                String service_name, username, message;
                                switch (request) {
                                    case Request.START_GAME:
                                        synchronized (GameContext.RENDER_LOCK) {
                                            getListener().onGameStart(null);
                                        }
                                        break;
                                    case Request.RESUME_GAME:
                                        GameSave game_save = (GameSave) ois.readObject();
                                        synchronized (GameContext.RENDER_LOCK) {
                                            getListener().onGameStart(game_save);
                                        }
                                    case Request.GAME_EVENT:
                                        GameEvent event = (GameEvent) ois.readObject();
                                        Gdx.app.log(TAG, "Receive " + event.toString());
                                        synchronized (GameContext.RENDER_LOCK) {
                                            getListener().onReceiveGameEvent(event);
                                        }
                                        break;
                                    case Request.MESSAGE:
                                        username = ois.readUTF();
                                        message = ois.readUTF();
                                        synchronized (GameContext.RENDER_LOCK) {
                                            if (getListener() != null) {
                                                getListener().onReceiveMessage(username, message);
                                            }
                                        }
                                        break;
                                    case Request.PLAYER_JOINING:
                                        service_name = ois.readUTF();
                                        username = ois.readUTF();
                                        synchronized (GameContext.RENDER_LOCK) {
                                            if (getListener() != null) {
                                                getListener().onPlayerJoin(service_name, username);
                                            }
                                        }
                                        break;
                                    case Request.PLAYER_LEAVING:
                                        service_name = ois.readUTF();
                                        username = ois.readUTF();
                                        synchronized (GameContext.RENDER_LOCK) {
                                            if (getListener() != null) {
                                                getListener().onPlayerLeave(service_name, username);
                                            }
                                        }
                                        break;
                                    case Request.UPDATE_ALLOCATION:
                                        String[] allocation = new String[4];
                                        for (int team = 0; team < 4; team++) {
                                            allocation[team] = ois.readUTF();
                                        }
                                        Integer[] types = new Integer[4];
                                        for (int team = 0; team < 4; team++) {
                                            types[team] = ois.readInt();
                                        }
                                        synchronized (GameContext.RENDER_LOCK) {
                                            if (getListener() != null) {
                                                getListener().onAllocationUpdate(allocation, types);
                                            }
                                        }
                                        break;
                                    case Request.UPDATE_ALLIANCE:
                                        Integer[] alliance = new Integer[4];
                                        for (int team = 0; team < 4; team++) {
                                            alliance[team] = ois.readInt();
                                        }
                                        synchronized (GameContext.RENDER_LOCK) {
                                            if (getListener() != null) {
                                                getListener().onAllianceUpdate(alliance);
                                            }
                                        }
                                        break;
                                    case Request.GET_GAME:
                                        GameCore game = GameHost.getGame();
                                        synchronized (OUTPUT_LOCK) {
                                            oos.writeInt(NetworkManager.RESPONSE);
                                            oos.writeObject(game);
                                            oos.flush();
                                        }
                                        break;
                                    default:
                                        //do nothing
                                }
                                break;
                            case RESPONSE:
                                //It's a server response. Free the input stream for task thread to read the response
                                try {
                                    INPUT_LOCK.wait();
                                } catch (InterruptedException ignored) {
                                }
                                break;
                            default:
                                //do nothing
                        }
                    }
                } catch (IOException e) {
                    Gdx.app.log(TAG, e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    Gdx.app.log(TAG, e.toString());
                }
            }
            if (server_socket != null) {
                if (listener != null) {
                    synchronized (GameContext.RENDER_LOCK) {
                        listener.onDisconnect();
                    }
                }
                //disconnect();
            }
            Gdx.app.log(TAG, "Disconnected from server");
        }

    }

}
