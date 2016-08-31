package net.toyknight.aeii.server;

import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.NetworkConstants;
import net.toyknight.aeii.network.entity.RoomSetting;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.entities.Room;
import net.toyknight.aeii.server.managers.MapManager;
import net.toyknight.aeii.server.utils.PacketBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author toyknight 8/16/2016.
 */
public class RequestHandler {

    private static final String TAG = "REQUEST HANDLER";

    private final ExecutorService executor;

    private final ServerContext context;

    public RequestHandler(ServerContext context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public ServerContext getContext() {
        return context;
    }

    public void doHandleRequest(Player player, JSONObject request) {
        try {
            switch (request.getInt("operation")) {
                case NetworkConstants.AUTHENTICATION:
                    onAuthenticationRequested(player, request);
                    break;
                case NetworkConstants.LIST_ROOMS:
                    onRoomListRequested(player);
                    break;
                case NetworkConstants.CREATE_ROOM:
                    onRoomCreationRequested(player, request);
                    break;
                case NetworkConstants.JOIN_ROOM:
                    onRoomJoinRequested(player, request);
                    break;
                case NetworkConstants.PLAYER_LEAVING:
                    getContext().getRoomManager().onPlayerLeaveRoom(player);
                    break;
                case NetworkConstants.ALLOCATION_UPDATING:
                    onAllocationUpdateRequested(player, request);
                    break;
                case NetworkConstants.START_GAME:
                    onGameStartRequested(player);
                    break;
                case NetworkConstants.GAME_EVENT:
                    onGameEventSubmitted(player, request);
                    break;
                case NetworkConstants.MESSAGE:
                    onMessageSubmitted(player, request);
                    break;
                case NetworkConstants.LIST_MAPS:
                    onMapListRequested(player, request);
                    break;
                case NetworkConstants.UPLOAD_MAP:
                    onMapUploadRequest(player, request);
                    break;
                case NetworkConstants.DOWNLOAD_MAP:
                    onMapDownloadRequested(player, request);
                    break;
                case NetworkConstants.LIST_IDLE_PLAYERS:
                    onIdlePlayerListRequested(player);
                    break;
                default:
                    Log.error(TAG, String.format("Illegal request from %s [undefined operation]", player.toString()));
            }
        } catch (JSONException ex) {
            Log.error(TAG, String.format("Illegal request from %s [request format error]", player.toString()), ex);
        } catch (Exception ex) {
            Log.error(TAG, String.format("Exception occurred while handling request from %s", player.toString()), ex);
        }
    }

    public void onAuthenticationRequested(Player player, JSONObject request) throws JSONException {
        String username = request.getString("username");
        String v_string = request.getString("v_string");

        player.setUsername(username);

        JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
        if (getContext().getVerificationString().equals(v_string)) {
            player.setAuthenticated(true);
            response.put("approved", true);
            response.put("service_id", player.getID());
            Log.info(TAG, String.format("%s authenticated.", player.toString()));
        } else {
            response.put("approved", false);
            Log.info(TAG, String.format("%s authentication failed.", player.toString()));
        }
        player.sendTCP(response.toString());
    }

    public void onRoomListRequested(Player player) {
        if (player.isAuthenticated()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
            JSONArray rooms = new JSONArray();
            for (RoomSnapshot snapshot : getContext().getRoomManager().getRoomSnapshots()) {
                rooms.put(snapshot.toJson());
            }
            response.put("rooms", rooms);
            player.sendTCP(response.toString());
        }
    }

    public void onRoomCreationRequested(Player player, JSONObject request) {
        if (player.isAuthenticated() && player.getRoomID() < 0) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);

            String username = player.getUsername();
            String password = request.has("password") ? request.getString("password") : null;
            int player_capacity = request.getInt("player_capacity");

            RoomSetting room_setting;
            if (request.getBoolean("new_game")) {
                //create a new game room
                Map map = new Map(request.getJSONObject("map"));
                String map_name = request.getString("map_name");
                int unit_capacity = request.getInt("unit_capacity");
                int start_gold = request.getInt("start_gold");
                room_setting = getContext().getRoomManager().createRoom(
                        map, username, map_name, password, player_capacity, unit_capacity, start_gold, player);
            } else {
                //create a saved game room
                GameCore game = new GameCore(request.getJSONObject("game"));
                String save_name = request.getString("save_name");
                room_setting = getContext().getRoomManager().createRoom(
                        game, username, save_name, password, player_capacity, player);
            }
            if (room_setting == null) {
                response.put("approved", false);
            } else {
                Log.info(TAG, String.format("%s creates game room [%d]", player.toString(), room_setting.room_id));
                response.put("room_setting", room_setting.toJson());
                response.put("approved", true);
            }
            player.sendTCP(response.toString());
        }
    }

    public void onRoomJoinRequested(Player player, JSONObject request) {
        if (player.isAuthenticated()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);

            long room_id = request.getLong("room_id");
            String password = request.getString("password");
            RoomSetting room_setting = getContext().getRoomManager().onPlayerJoinRoom(player, room_id, password);
            if (room_setting == null) {
                response.put("approved", false);
            } else {
                response.put("room_setting", room_setting.toJson());
                response.put("approved", true);
            }
            player.sendTCP(response.toString());
        }
    }

    public void onAllocationUpdateRequested(Player player, JSONObject request) {
        if (player.isAuthenticated()) {
            JSONArray types = request.getJSONArray("types");
            JSONArray alliance = request.getJSONArray("alliance");
            JSONArray allocation = request.getJSONArray("allocation");
            getContext().getRoomManager().onAllocationUpdate(player, types, alliance, allocation);
        }
    }

    public void onGameEventSubmitted(Player player, JSONObject request) {
        if (player.isAuthenticated()) {
            JSONArray events = request.getJSONArray("events");
            getContext().getRoomManager().submitGameEvents(player, events);
        }
    }

    public void onMessageSubmitted(Player player, JSONObject request) {
        if (player.isAuthenticated()) {
            Room room = getContext().getRoomManager().getRoom(player.getRoomID());
            String message = request.getString("message");
            if (room == null) {
                getContext().getNotificationSender().notifyLobbyMessage(player.getUsername(), message);
            } else {
                getContext().getNotificationSender().notifyRoomMessage(room, player.getUsername(), message);
            }
        }
    }

    public void onGameStartRequested(Player player) {
        if (player.isAuthenticated()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
            boolean approved = getContext().getRoomManager().tryStartGame(player);
            response.put("approved", approved);
            player.sendTCP(response.toString());
        }
    }

    public void onMapListRequested(Player player, JSONObject request) {
        if (getContext().getConfiguration().isMapManagerEnabled()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
            boolean symmetric = request.has("symmetric") && request.getBoolean("symmetric");
            if (request.has("author")) {
                String author = request.getString("author");
                response.put("maps", getContext().getMapManager().getSerializedMapList(author, symmetric));
            } else {
                response.put("maps", getContext().getMapManager().getSerializedAuthorList(symmetric));
            }
            player.sendTCP(response.toString());
        }
    }

    public void onMapUploadRequest(Player player, JSONObject request) {
        if (getContext().getConfiguration().isMapManagerEnabled()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
            Map map = new Map(request.getJSONObject("map"));
            String map_name = request.getString("map_name");
            try {
                getContext().getMapManager().addMap(map, map_name);
                request.put("code", NetworkConstants.CODE_OK);
            } catch (MapManager.MapExistingException ex) {
                request.put("code", NetworkConstants.CODE_MAP_EXISTING);
            } catch (Exception ex) {
                request.put("code", NetworkConstants.CODE_SERVER_ERROR);
            }
            player.sendTCP(response.toString());
        }
    }

    public void onMapDownloadRequested(Player player, JSONObject request) {
        if (getContext().getConfiguration().isMapManagerEnabled()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
            int map_id = request.getInt("id");
            boolean approved;
            try {
                Map map = getContext().getMapManager().getMap(map_id);
                response.put("map", map.toJson());
                approved = true;
            } catch (IOException ex) {
                approved = false;
            } catch (AEIIException ex) {
                approved = false;
            }
            response.put("approved", approved);
            player.sendTCP(response.toString());
        }
    }

    public void onIdlePlayerListRequested(Player player) {
        if (player.isAuthenticated()) {
            JSONObject response = PacketBuilder.create(NetworkConstants.RESPONSE);
            JSONArray players = new JSONArray();
            for (Player target : getContext().getPlayerManager().getPlayers()) {
                if (target.isAuthenticated() && target.getRoomID() < 0) {
                    players.put(target.createSnapshot().toJson());
                }
            }
            response.put("players", players);
            player.sendTCP(response.toString());
        }
    }

    public void submitRequest(Player player, String request_content) throws JSONException {
        executor.submit(new RequestProcessingTask(player, new JSONObject(request_content)));
    }

    private class RequestProcessingTask implements Runnable {

        private final Player player;
        private final JSONObject request;

        public RequestProcessingTask(Player player, JSONObject request) {
            this.player = player;
            this.request = request;
        }

        public Player getPlayer() {
            return player;
        }

        public JSONObject getRequest() {
            return request;
        }

        @Override
        public void run() {
            doHandleRequest(getPlayer(), getRequest());
        }
    }

}
