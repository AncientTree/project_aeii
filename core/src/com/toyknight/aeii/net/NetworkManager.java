package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.toyknight.aeii.utils.Language;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by toyknight on 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    public static final int TYPE_INTEGER = 0x1;
    public static final int TYPE_STRING = 0x2;
    public static final int TYPE_OBJECT = 0x3;

    private final Object TASK_LOCK = new Object();

    private final Queue<NetworkTask> task_queue;

    private NetworkListener listener;

    private boolean running;

    private Socket server_socket;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public NetworkManager() {
        this.task_queue = new LinkedList();
        new TaskDispatchingThread().start();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        synchronized (TASK_LOCK) {
            this.running = false;
            TASK_LOCK.notifyAll();
        }
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

    public void postTask(NetworkTask task) {
        synchronized (TASK_LOCK) {
            task_queue.add(task);
            TASK_LOCK.notifyAll();
        }
    }

    public void sendInteger(int n) throws IOException {
        oos.writeInt(n);
        oos.flush();
    }

    public void sendString(String string) throws IOException {
        oos.writeUTF(string);
    }

    public void sendObject(Object obj) throws IOException {
        oos.writeObject(obj);
    }

    private class TaskDispatchingThread extends Thread {

        @Override
        public void run() {
            NetworkManager.this.running = true;
            while (isRunning()) {
                NetworkTask task;
                synchronized (TASK_LOCK) {
                    task = task_queue.poll();
                    if (task == null) {
                        try {
                            TASK_LOCK.wait();
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                }
                try {
                    task.doTask();
                    Gdx.app.postRunnable(new NetworkTaskFutureEvent(task, null, NetworkTaskFutureEvent.SUCCESS));
                } catch (IOException ex) {
                    Gdx.app.postRunnable(new NetworkTaskFutureEvent(task, ex.getMessage(), NetworkTaskFutureEvent.FAILURE));
                } catch (GdxRuntimeException ex) {
                    Gdx.app.postRunnable(new NetworkTaskFutureEvent(task, Language.getText("MSG_ERR_CCS"), NetworkTaskFutureEvent.FAILURE));
                }


            }
        }

    }

    private class ReceivingThread extends Thread {

        @Override
        public void run() {
            while (server_socket.isConnected()) {
                try {
                    int input_type = ois.readInt();
                    read(input_type);
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

        private void read(int type) throws IOException {
            switch (type) {
                case TYPE_INTEGER:
                    break;
                case TYPE_STRING:
                    break;
                case TYPE_OBJECT:
                    break;
                default:
                    //do nothing
            }
        }

    }

}
