package net.toyknight.aeii.server;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.network.NetworkConstants;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.managers.MapManager;
import net.toyknight.aeii.server.managers.PlayerManager;
import net.toyknight.aeii.server.managers.RoomManager;
import net.toyknight.aeii.utils.MD5Converter;
import net.toyknight.aeii.utils.TileFactory;
import net.toyknight.aeii.utils.UnitFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author toyknight 8/13/2016.
 */
public class ServerContext {

    private static final String TAG = "SERVER CONTEXT";

    private boolean running;

    private ExecutorService notification_executor;

    private ObjectMap<String, String> configuration;

    private String verification_string;

    private Server server;

    private RequestHandler request_handler;

    private PlayerManager player_manager;

    private RoomManager room_manager;

    private MapManager map_manager;

    public RequestHandler getRequestHandler() {
        return request_handler;
    }

    public PlayerManager getPlayerManager() {
        return player_manager;
    }

    public RoomManager getRoomManager() {
        return room_manager;
    }

    public void submitNotification(int player_id, JSONObject notification) {
        Player player = getPlayerManager().getPlayer(player_id);
        if (player != null) {
            notification_executor.submit(new NotificationTask(player, notification));
        }
    }

    public void syncGameEvent(int player_id, JSONObject event) {
        Player player = getPlayerManager().getPlayer(player_id);
        if (player != null) {
            JSONObject notification = createPacket(NetworkConstants.NOTIFICATION);
            notification.put("operation", NetworkConstants.GAME_EVENT);
            notification.put("game_event", event);
            player.sendTCP(notification);
        }
    }

    public void onObjectReceived(Connection connection, Object object) {
        Player player = getPlayerManager().getPlayer(connection.getID());
        if (player != null && object instanceof String) {
            try {
                getRequestHandler().submitRequest(player, (String) object);
            } catch (JSONException ex) {
                Log.error(TAG, String.format("Illegal request from %s [request format error]", player.toString()), ex);
            }
        }
    }

    public JSONObject createPacket(int type) {
        JSONObject response = new JSONObject();
        response.put("type", type);
        return response;
    }

    public void loadConfiguration() throws IOException {
        File configuration_file = new File("server.cfg");
        FileReader configuration_reader = new FileReader(configuration_file);
        configuration = new ObjectMap<String, String>();
        PropertiesUtils.load(configuration, configuration_reader);
    }

    public ObjectMap<String, String> getConfiguration() {
        return configuration;
    }

    public void createVerificationString() {
        verification_string = new MD5Converter().toMD5(
                TileFactory.getVerificationString() + UnitFactory.getVerificationString() + GameContext.INTERNAL_VERSION);
    }

    public String getVerificationString() {
        return verification_string;
    }

    public int getPort() {
        return Integer.parseInt(configuration.get("PORT", "5438"));
    }

    public String getAdministratorToken() {
        return configuration.get("ADMIN_TOKEN", "123456");
    }

    public void initialize() throws ServerException {
        //load server configuration
        try {
            loadConfiguration();
        } catch (IOException ex) {
            throw new ServerException(TAG, "Error initializing server [exception while loading configuration]", ex);
        }
        //load game data and create verification string
        try {
            UnitFactory.loadUnitData();
            TileFactory.loadTileData();
            createVerificationString();
        } catch (AEIIException ex) {
            throw new ServerException(TAG, "Error initializing server [exception while loading game data]", ex);
        }
        //initialize managers
        notification_executor = Executors.newFixedThreadPool(128);
        request_handler = new RequestHandler(this);
        player_manager = new PlayerManager(this);
        room_manager = new RoomManager(this);
        //initialize server object
        server = new Server(90 * 1024, 90 * 1024);
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                getPlayerManager().onPlayerConnected(connection);
            }

            @Override
            public void disconnected(Connection connection) {
                getPlayerManager().onPlayerDisconnected(connection);
            }

            @Override
            public void received(Connection connection, Object object) {
                onObjectReceived(connection, object);
            }
        });
    }

    public void start() throws ServerException {
        initialize();
        try {
            server.start();
            server.bind(getPort());
            running = true;
        } catch (IOException ex) {
            throw new ServerException(TAG, "Error starting server [exception while binding port]", ex);
        }
    }

    private class NotificationTask implements Runnable {

        private final Player player;
        private final JSONObject notification;

        public NotificationTask(Player player, JSONObject notification) {
            this.player = player;
            this.notification = notification;
        }

        @Override
        public void run() {
            player.sendTCP(notification.toString());
        }

    }

}
