package net.toyknight.aeii.manager;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Player;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.network.entity.RoomSetting;
import org.json.JSONException;

/**
 * @author toyknight 6/7/2016.
 */
public class RoomManager {

    private long room_number;

    private boolean started;

    private int host;

    private GameCore game;

    private Array<PlayerSnapshot> players;

    private int[] allocation;

    private int start_gold;

    private int max_population;

    public RoomManager() {
        this.allocation = new int[4];
    }

    public void initialize(RoomSetting setting) {
        this.room_number = setting.room_id;
        this.started = setting.started;
        this.host = setting.host;
        this.game = setting.game;
        this.players = setting.players;
        this.allocation = setting.allocation;
        this.start_gold = setting.start_gold;
        this.max_population = setting.max_population;
    }

    public void setGame(GameCore game) {
        this.game = game;
        for (int team = 0; team < 4; team++) {
            allocation[team] = -1;
            game.getPlayer(team).setAlliance(team + 1);
            game.getPlayer(team).setType(Player.NONE);
        }
        started = false;
        host = -1;
        players = new Array<PlayerSnapshot>();
        allocation = new int[4];
        start_gold = Rule.GOLD_PRESET[0];
        max_population = Rule.POPULATION_PRESET[0];
    }

    public long getRoomNumber() {
        return room_number;
    }

    public GameCore getGame() {
        return game;
    }

    public Array<PlayerSnapshot> getPlayers() {
        return players;
    }

    public int[] getAllocations() {
        return allocation;
    }

    public int getAllocation(int team) {
        return allocation[team];
    }

    public int getStartGold() {
        return start_gold;
    }

    public int getMaxPopulation() {
        return max_population;
    }

    public String getUsername(int id) {
        for (PlayerSnapshot player : players) {
            if (player.id == id) {
                return player.username;
            }
        }
        return "";
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean hasTeamAccess(int team) {
        if (NetworkManager.isConnected()) {
            return getGame().getMap().hasTeamAccess(team) && NetworkManager.getServiceID() == getAllocation(team);
        } else {
            return getGame().getMap().hasTeamAccess(team);
        }
    }

    public boolean isHost() {
        return !NetworkManager.isConnected() || host == NetworkManager.getServiceID();
    }

    public boolean isStarted() {
        return started;
    }

    public void updateAllocation(int team, int service_id) {
        allocation[team] = service_id;
        getGame().getPlayer(team).setType(Player.LOCAL);
    }

    public void updatePlayerType(int team, int type) {
        getGame().getPlayer(team).setType(type);
        if (NetworkManager.isConnected()) {
            switch (type) {
                case Player.NONE:
                    allocation[team] = -1;
                    break;
                case Player.LOCAL:
                    if (allocation[team] == -1) {
                        allocation[team] = NetworkManager.getServiceID();
                    }
                    break;
                case Player.ROBOT:
                    allocation[team] = NetworkManager.getServiceID();
                    break;
                default:
                    //do nothing
            }
        }
    }

    public void updateAlliance(int team, int alliance) {
        getGame().getPlayer(team).setAlliance(alliance);
    }

    public void updateStartGold(int start_gold) {
        this.start_gold = start_gold;
        for (int team = 0; team < 4; team++) {
            getGame().getPlayer(team).setGold(start_gold);
        }
    }

    public void updateMaxPopulation(int max_population) {
        getGame().getRule().setValue(Rule.Entry.UNIT_CAPACITY, max_population);
    }

    public void trySubmitUpdates() throws JSONException {
        if (NetworkManager.isConnected() && isHost()) {
            NetworkManager.notifyAllocationUpdate(getAlliances(), getAllocations(), getPlayerTypes());
        }
    }

    public GameCore getArrangedGame() {
        if (started) {
            for (int team = 0; team < 4; team++) {
                if (getGame().getPlayer(team).getType() != Player.NONE) {
                    getGame().getPlayer(team).setType(Player.REMOTE);
                }
            }
        } else {
            for (int team = 0; team < 4; team++) {
                if (!hasTeamAccess(team) && getGame().getPlayer(team).getType() != Player.NONE) {
                    getGame().getPlayer(team).setType(Player.REMOTE);
                }
            }
        }
        return getGame();
    }

    public void onPlayerJoin(int id, String username) {
        PlayerSnapshot snapshot = new PlayerSnapshot(id, username, false);
        getPlayers().add(snapshot);
    }

    public void onPlayerLeave(int id, int host) {
        int index = -1;
        for (int i = 0; i < getPlayers().size; i++) {
            PlayerSnapshot player = getPlayers().get(i);
            player.is_host = (player.id == host);
            if (id == player.id && index < 0) {
                index = i;
            }
        }
        if (index >= 0) {
            getPlayers().removeIndex(index);
        }
        this.host = host;
    }

    public void onAllocationUpdate(int[] alliance, int[] allocation, int[] types) {
        System.arraycopy(allocation, 0, this.allocation, 0, allocation.length);
        if (!isStarted()) {
            for (int team = 0; team < 4; team++) {
                getGame().getPlayer(team).setAlliance(alliance[team]);
                getGame().getPlayer(team).setType(types[team]);
            }
        }
    }

    private int[] getPlayerTypes() {
        int[] player_type = new int[4];
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            player_type[team] = player.getType();
        }
        return player_type;
    }

    private int[] getAlliances() {
        int[] alliance = new int[4];
        for (int team = 0; team < 4; team++) {
            alliance[team] = getGame().getPlayer(team).getAlliance();
        }
        return alliance;
    }

}
