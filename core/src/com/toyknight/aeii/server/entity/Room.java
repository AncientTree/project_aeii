package com.toyknight.aeii.server.entity;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author toyknight
 */
public class Room {

    private final Object PLAYER_LOCK = new Object();

    private final long room_number;
    private final String room_name;

    private boolean game_started;

    private int capacity = 4;

    private String host_service = null;
    private final HashSet<String> players;
    private final int[] player_type;
    private final String[] team_allocation;
    private final int[] alliance_state;

    private Map map;
    private String map_name;
    private int initial_gold = 1000;
    private int max_population = 10;

    public Room(long room_number, String room_name) {
        this.room_number = room_number;
        this.room_name = room_name;
        this.game_started = false;
        this.players = new HashSet<String>();
        this.player_type = new int[4];
        Arrays.fill(player_type, Player.NONE);
        this.team_allocation = new String[4];
        Arrays.fill(team_allocation, "NONE");
        this.alliance_state = new int[4];
        Arrays.fill(alliance_state, -1);
    }

    public void addPlayer(String service_name) {
        synchronized (PLAYER_LOCK) {
            players.add(service_name);
            for (int team = 0; team < 4; team++) {
                if (getMap().getTeamAccessTable()[team] && team_allocation[team].equals("NONE")) {
                    player_type[team] = Player.LOCAL;
                    team_allocation[team] = service_name;
                    alliance_state[team] = team;
                    if (host_service == null) {
                        host_service = service_name;
                    }
                }
            }
        }
    }

    public void removePlayer(String service_name) {
        synchronized (PLAYER_LOCK) {
            players.remove(service_name);
            for (int team = 0; team < 4; team++) {
                if (!team_allocation[team].equals("NONE") && team_allocation[team].equals(service_name)) {
                    player_type[team] = Player.NONE;
                    team_allocation[team] = "NONE";
                }
            }
            if (host_service.equals(service_name)) {
                host_service = null;
                for (String player_service : players) {
                    host_service = player_service;
                    break;
                }
            }
        }
    }

    public String getHostService() {
        return host_service;
    }

    public boolean getPlayerTeamAccess(String service_name, int team) {
        return team_allocation[team] != null && team_allocation[team].equals(service_name);
    }

    public void setPlayerType(int team, int type) {
        player_type[team] = type;
    }

    public int getPlayerType(int team) {
        return player_type[team];
    }

    public String getTeamAllocation(int team) {
        return team_allocation[team];
    }

    public int getAlliance(int team) {
        return alliance_state[team];
    }

    public long getRoomNumber() {
        return room_number;
    }

    public String getRoomName() {
        return room_name;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRemaining() {
        return getCapacity() - players.size();
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    public void setMapName(String map_name) {
        this.map_name = map_name;
    }

    public String getMapName() {
        return map_name;
    }

    public void setInitialGold(int gold) {
        this.initial_gold = gold;
    }

    public int getInitialGold() {
        return initial_gold;
    }

    public void setMaxPopulation(int population) {
        this.max_population = population;
    }

    public int getMaxPopulation() {
        return max_population;
    }

    public void startGame() {
        game_started = true;
    }

    public boolean isOpen() {
        return !game_started;
    }

    public RoomSnapshot createSnapshot() {
        return new RoomSnapshot(getRoomNumber(), getRoomName(), getMapName(), getCapacity(), getRemaining());
    }

}
