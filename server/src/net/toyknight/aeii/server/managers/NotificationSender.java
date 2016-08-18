package net.toyknight.aeii.server.managers;

import static net.toyknight.aeii.network.NetworkConstants.*;

import net.toyknight.aeii.server.ServerContext;
import net.toyknight.aeii.server.entities.Player;
import net.toyknight.aeii.server.entities.Room;
import net.toyknight.aeii.server.utils.PacketBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author toyknight 8/17/2016.
 */
public class NotificationSender {

    private final ServerContext context;

    public NotificationSender(ServerContext context) {
        this.context = context;
    }

    public ServerContext getContext() {
        return context;
    }

    public void notifyAllocationUpdating(Room room, int updater) {
        JSONArray types = new JSONArray();
        JSONArray alliance = new JSONArray();
        JSONArray allocation = new JSONArray();
        for (int team = 0; team < 4; team++) {
            types.put(room.getPlayerType(team));
            alliance.put(room.getAlliance(team));
            allocation.put(room.getAllocation(team));
        }
        notifyAllocationUpdating(room, updater, alliance, allocation, types);
    }

    public void notifyAllocationUpdating(
            Room room, int updater, JSONArray alliance, JSONArray allocation, JSONArray types) {
        for (int player_id : room.getPlayers()) {
            if (player_id != updater) {
                JSONObject notification = PacketBuilder.create(NOTIFICATION, ALLOCATION_UPDATING);
                notification.put("types", types);
                notification.put("alliance", alliance);
                notification.put("allocation", allocation);
                submitNotification(player_id, notification);
            }
        }
    }

    public void notifyPlayerJoining(Room room, int joiner, String username) {
        for (int player_id : room.getPlayers()) {
            if (player_id != joiner) {
                JSONObject notification = PacketBuilder.create(NOTIFICATION, PLAYER_JOINING);
                notification.put("player_id", joiner);
                notification.put("username", username);
                submitNotification(player_id, notification);
            }
        }
    }

    public void notifyPlayerLeaving(Room room, int leaver, String username, int host_id) {
        for (int player_id : room.getPlayers()) {
            if (player_id != leaver) {
                JSONObject notification = PacketBuilder.create(NOTIFICATION, PLAYER_LEAVING);
                notification.put("player_id", leaver);
                notification.put("username", username);
                notification.put("host_id", host_id);
                submitNotification(player_id, notification);
            }
        }
    }

    public void notifyGameStarting(Room room) {
        int host_id = room.getHostID();
        for (int player_id : room.getPlayers()) {
            if (player_id != host_id) {
                JSONObject notification = PacketBuilder.create(NOTIFICATION, GAME_STARTING);
                submitNotification(player_id, notification);
            }
        }
    }

    public void submitNotification(int player_id, JSONObject notification) {
        Player player = getContext().getPlayerManager().getPlayer(player_id);
        if (player != null) {
            getContext().submitTask(new NotificationTask(player, notification));
        }
    }

    public void syncGameEvent(int player_id, JSONObject event) {
        Player player = getContext().getPlayerManager().getPlayer(player_id);
        if (player != null) {
            JSONObject notification = PacketBuilder.create(NOTIFICATION, GAME_EVENT);
            notification.put("game_event", event);
            player.sendTCP(notification.toString());
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
