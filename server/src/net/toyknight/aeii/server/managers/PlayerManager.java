package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.network.NetworkConstants;
import net.toyknight.aeii.server.ServerContext;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.utils.PacketBuilder;
import org.json.JSONObject;

/**
 * @author toyknight 8/16/2016.
 */
public class PlayerManager {

    private static final String TAG = "PLAYER_MANAGER";

    private final ServerContext context;

    private final Object PLAYER_LOCK = new Object();

    private final ObjectMap<Integer, Player> players = new ObjectMap<Integer, Player>();

    public PlayerManager(ServerContext context) {
        this.context = context;
    }

    public ServerContext getContext() {
        return context;
    }

    public void addPlayer(Connection connection) {
        synchronized (PLAYER_LOCK) {
            players.put(connection.getID(), new Player(connection));
        }
    }

    public Player removePlayer(int id) {
        synchronized (PLAYER_LOCK) {
            return players.remove(id);
        }
    }

    public Player getPlayer(int id) {
        synchronized (PLAYER_LOCK) {
            return players.get(id, null);
        }
    }

    public ObjectSet<Player> getPlayers() {
        synchronized (PLAYER_LOCK) {
            ObjectSet<Player> player_set = new ObjectSet<Player>();
            for (Player player : players.values()) {
                player_set.add(player);
            }
            return player_set;
        }
    }

    public void disconnectPlayer(int player_id, String message, long delay) {
        Player player = getPlayer(player_id);
        if (player != null) {
            getContext().submitTask(new DisconnectingTask(player, message, delay));
        }
    }

    public void onPlayerConnected(Connection connection) {
        addPlayer(connection);
    }

    public void onPlayerDisconnected(Connection connection) {
        Player player = removePlayer(connection.getID());
        if (player != null && player.getRoomID() >= 0) {
            Log.info(TAG, String.format("%s disconnected", player.toString()));
            getContext().getRoomManager().onPlayerLeaveRoom(player);
        }
    }

    private class DisconnectingTask implements Runnable {

        private final Player player;
        private final String message;
        private final long delay;

        public DisconnectingTask(Player player, String message, long delay) {
            this.player = player;
            this.message = message;
            this.delay = delay;
        }

        @Override
        public void run() {
            JSONObject notification = PacketBuilder.create(NetworkConstants.NOTIFICATION, NetworkConstants.MESSAGE);
            notification.put("username", "Server");
            notification.put("message", message);
            player.sendTCP(notification.toString());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
            player.getConnection().close();
        }

    }

}
