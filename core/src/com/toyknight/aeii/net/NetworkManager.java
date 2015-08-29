package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.server.entity.RoomSnapshot;
import com.toyknight.aeii.server.entity.Server;
import com.toyknight.aeii.utils.Language;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by toyknight on 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    public static final int REQUEST = 0x1;
    public static final int RESPONSE = 0x2;

    private final Object INPUT_LOCK = new Object();
    private final Object OUTPUT_LOCK = new Object();

    private final Queue<NetworkTask> task_queue;
    private FutureTask<Boolean> task_executor;
    private NetworkTask current_task;

    private NetworkListener listener;

    private boolean running;

    private Socket server_socket;

    private String service_name;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public NetworkManager() {
        this.task_queue = new LinkedList();
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

    public void connect(Server server, String username) throws IOException {
        if (server_socket != null) {
            disconnect();
        }

        SocketHints hints = new SocketHints();
        server_socket = Gdx.net.newClientSocket(Net.Protocol.TCP, server.getAddress(), server.getPort(), hints);
        oos = new ObjectOutputStream(server_socket.getOutputStream());
        ois = new ObjectInputStream(server_socket.getInputStream());

        service_name = ois.readUTF();
        oos.writeUTF(username);
        oos.flush();

        new ReceivingThread().start();
    }

    public void disconnect() {
        if (server_socket != null) {
            server_socket.dispose();
            server_socket = null;
        }
    }

    public boolean isConnected() {
        return server_socket.isConnected();
    }

    public String getServiceName() {
        return service_name;
    }

    public void postTask(NetworkTask task) {
        if (current_task == null) {
            startTask(task);
        } else {
            task_queue.add(task);
        }
    }

    private void startTask(NetworkTask task) {
        current_task = task;
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return current_task.doTask();
            }
        };
        task_executor = new FutureTask<Boolean>(callable);
        new Thread(task_executor).start();
    }

    public void updateTasks() {
        if (current_task == null) {
            if (!task_queue.isEmpty()) {
                startTask(task_queue.poll());
            }
        } else {
            if (task_executor.isDone()) {
                try {
                    boolean success = task_executor.get();
                    if (success) {
                        current_task.onFinish();
                    } else {
                        current_task.onFail(Language.getText("MSG_ERR_AEA"));
                    }
                } catch (InterruptedException e) {
                    current_task.onFail(Language.getText("MSG_ERR_RI"));
                    current_task = null;
                } catch (ExecutionException e) {
                    System.err.println(e.toString());
                    current_task.onFail(e.getMessage());
                } finally {
                    current_task = null;
                }
            }
        }
    }

    public ArrayList<RoomSnapshot> requestOpenRoomList() throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.LIST_ROOMS);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                ArrayList list = (ArrayList) ois.readObject();
                INPUT_LOCK.notifyAll();
                return list;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            } catch (ClassNotFoundException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public boolean requestJoinRoom(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.JOIN_ROOM);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                boolean success = ois.readBoolean();
                INPUT_LOCK.notifyAll();
                return success;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public String requestHost(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_HOST);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                String host = ois.readUTF();
                INPUT_LOCK.notifyAll();
                return host;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public Map requestMap(long room_number) throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_MAP);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                Map map_data = (Map) ois.readObject();
                INPUT_LOCK.notifyAll();
                return map_data;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            } catch (ClassNotFoundException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public int[] requestPlayerType(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_PLAYER_TYPE);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                int[] type = new int[4];
                for (int team = 0; team < 4; team++) {
                    type[team] = ois.readInt();
                }
                INPUT_LOCK.notifyAll();
                return type;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public String[] requestTeamAllocation(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_TEAM_ALLOCATION);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                String[] allocation = new String[4];
                for (int team = 0; team < 4; team++) {
                    allocation[team] = ois.readUTF();
                }
                INPUT_LOCK.notifyAll();
                return allocation;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public int[] requestAlliance(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_ALLIANCE);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                int[] alliance = new int[4];
                for (int team = 0; team < 4; team++) {
                    alliance[team] = ois.readInt();
                }
                INPUT_LOCK.notifyAll();
                return alliance;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public int requestInitialGold(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_INITIAL_GOLD);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                int gold = ois.readInt();
                INPUT_LOCK.notifyAll();
                return gold;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public int requestMaxPopulation(long room_number) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GET_MAX_POPULATION);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                int population = ois.readInt();
                INPUT_LOCK.notifyAll();
                return population;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    private class ReceivingThread extends Thread {

        @Override
        public void run() {
            while (server_socket.isConnected()) {
                try {
                    synchronized (INPUT_LOCK) {
                        int type = ois.readInt();
                        switch (type) {
                            case REQUEST:
                                int request = ois.readInt();
                                processRequest(request);
                                break;
                            case RESPONSE:
                                //It's a server response. Free the input stream for task thread to read the response
                                try {
                                    INPUT_LOCK.wait();
                                } catch (InterruptedException e) {
                                }
                                break;
                            default:
                                //do nothing
                        }
                    }
                } catch (IOException e) {
                    Gdx.app.log(TAG, e.getMessage());
                    break;
                }
            }
            if (server_socket != null) {
                disconnect();
                if (listener != null) {
                    listener.onDisconnect();
                }
            }
            Gdx.app.log(TAG, "Disconnected from server");
        }

        private void processRequest(int request) {
            switch (request) {

                default:
                    //do nothing
            }
        }

    }

}
