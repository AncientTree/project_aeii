package net.toyknight.aeii.server;

import com.esotericsoftware.minlog.Log;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.NetworkConstants;
import net.toyknight.aeii.network.entity.RoomSetting;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import net.toyknight.aeii.server.entities.Player;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public void handleRequest(Player player, JSONObject request) {
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
                case NetworkConstants.UPDATE_ALLOCATION:
                    onAllocationUpdateRequested(player, request);
                    break;
                default:
                    Log.error(TAG, String.format("Illegal request from %s [undefined operation]", player.toString()));
            }
        } catch (JSONException ex) {
            Log.error(TAG, String.format("Illegal request from %s [request format error]", player.toString()), ex);
        }
    }

    public void onAuthenticationRequested(Player player, JSONObject request) throws JSONException {
        String username = request.getString("username");
        String v_string = request.getString("v_string");

        player.setUsername(username);

        JSONObject response = getContext().createPacket(NetworkConstants.RESPONSE);
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
            JSONObject response = getContext().createPacket(NetworkConstants.RESPONSE);
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
            JSONObject response = getContext().createPacket(NetworkConstants.RESPONSE);

            int host = player.getID();
            String username = player.getUsername();
            String password = request.has("password") ? request.getString("password") : null;
            int player_capacity = request.getInt("player_capacity");

            if (request.getBoolean("new_game")) {
                //create a new game room
                Map map = new Map(request.getJSONObject("map"));
                String map_name = request.getString("map_name");
                int unit_capacity = request.getInt("unit_capacity");
                int start_gold = request.getInt("start_gold");
                RoomSetting room_setting = getContext().getRoomManager().createRoom(
                        map, username, map_name, password, player_capacity, unit_capacity, start_gold, player);
                response.put("room_setting", room_setting.toJson());
                response.put("approved", true);
                Log.info(TAG, String.format("%s creates a new game [%d]", player.toString(), room_setting.room_id));
            } else {
                //create a saved game room
                GameCore game = new GameCore(request.getJSONObject("game"));
                String save_name = request.getString("save_name");
                RoomSetting room_setting = getContext().getRoomManager().createRoom(
                        game, username, save_name, password, player_capacity, player);
                response.put("room_setting", room_setting.toJson());
                response.put("approved", true);
                Log.info(TAG, String.format("%s creates a saved game [%d]", player.toString(), room_setting.room_id));
            }
            player.sendTCP(response.toString());
        }
    }

    public void onRoomJoinRequested(Player player, JSONObject request) {
        if (player.isAuthenticated()) {
            JSONObject response = getContext().createPacket(NetworkConstants.RESPONSE);

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
            handleRequest(getPlayer(), getRequest());
        }
    }

}
