package net.toyknight.aeii.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.managers.*;
import net.toyknight.aeii.utils.MD5Converter;
import net.toyknight.aeii.utils.TileFactory;
import net.toyknight.aeii.utils.UnitFactory;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author toyknight 8/13/2016.
 */
public class ServerContext {

    private static final String TAG = "SERVER CONTEXT";

    private boolean running;

    private ExecutorService executor;

    private ServerConfiguration configuration;

    private String verification_string;

    private Server server;

    private NotificationSender notification_sender;

    private RequestHandler request_handler;

    private PlayerManager player_manager;

    private RoomManager room_manager;

    private MapManager map_manager;

    private DatabaseManager database_manager;

    public NotificationSender getNotificationSender() {
        return notification_sender;
    }

    public RequestHandler getRequestHandler() {
        return request_handler;
    }

    public PlayerManager getPlayerManager() {
        return player_manager;
    }

    public RoomManager getRoomManager() {
        return room_manager;
    }

    public MapManager getMapManager() {
        return map_manager;
    }

    public DatabaseManager getDatabaseManager() {
        return database_manager;
    }

    public void submitTask(Runnable task) {
        executor.submit(task);
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

    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    public void createVerificationString() {
        verification_string = new MD5Converter().toMD5(
                TileFactory.getVerificationString() + UnitFactory.getVerificationString() + GameContext.INTERNAL_VERSION);
    }

    public String getVerificationString() {
        return verification_string;
    }

    public void initialize() throws ServerException {
        //load server configuration
        try {
            configuration = new ServerConfiguration();
            configuration.initialize();
        } catch (Exception ex) {
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
        executor = Executors.newFixedThreadPool(128);
        notification_sender = new NotificationSender(this);
        request_handler = new RequestHandler(this);
        player_manager = new PlayerManager(this);
        room_manager = new RoomManager(this);
        if (getConfiguration().isMapManagerEnabled()) {
            try {
                database_manager = new DatabaseManager();
                database_manager.connect(
                        getConfiguration().getDatabaseHost(),
                        getConfiguration().getDatabaseName(),
                        getConfiguration().getDatabaseUsername(),
                        getConfiguration().getDatabasePassword());
            } catch (Exception ex) {
                throw new ServerException(TAG, "Error initializing server [exception while connecting to DB]", ex);
            }
            map_manager = new MapManager(this);
        }
        //initialize server object
        server = new Server(90 * 1024, 90 * 1024);
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                if (running) {
                    getPlayerManager().onPlayerConnected(connection);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                if (running) {
                    getPlayerManager().onPlayerDisconnected(connection);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                if (running) {
                    onObjectReceived(connection, object);
                }
            }
        });
    }

    public void index() throws ServerException {
        //load server configuration
        try {
            configuration = new ServerConfiguration();
            configuration.initialize();
        } catch (Exception ex) {
            throw new ServerException(TAG, "Error indexing [exception while loading configuration]", ex);
        }
        //load game data and create verification string
        try {
            UnitFactory.loadUnitData();
            TileFactory.loadTileData();
            createVerificationString();
        } catch (AEIIException ex) {
            throw new ServerException(TAG, "Error indexing [exception while loading game data]", ex);
        }
        try {
            database_manager = new DatabaseManager();
            database_manager.connect(
                    getConfiguration().getDatabaseHost(),
                    getConfiguration().getDatabaseName(),
                    getConfiguration().getDatabaseUsername(),
                    getConfiguration().getDatabasePassword());
        } catch (Exception ex) {
            throw new ServerException(TAG, "Error indexing [exception while connecting to DB]", ex);
        }
        map_manager = new MapManager(this);
        map_manager.index();
    }

    public void start() throws ServerException {
        initialize();
        try {
            server.start();
            server.bind(getConfiguration().getPort());
            running = true;
        } catch (IOException ex) {
            throw new ServerException(TAG, "Error starting server [exception while binding port]", ex);
        }
    }

}
