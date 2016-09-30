package net.toyknight.aeii.server.managers;

import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.entity.MapSnapshot;
import net.toyknight.aeii.server.ServerContext;
import net.toyknight.aeii.utils.MapFactory;
import org.json.JSONArray;

import java.io.*;
import java.sql.SQLException;

/**
 * @author by toyknight 6/10/2016.
 */
public class MapManager {

    private final ServerContext context;

    private final Object CHANGE_LOCK = new Object();

    public MapManager(ServerContext context) {
        this.context = context;
    }

    public ServerContext getContext() {
        return context;
    }

    private void writeMap(File map_file, Map map) throws IOException {
        FileOutputStream fos = new FileOutputStream(map_file);
        DataOutputStream dos = new DataOutputStream(fos);
        MapFactory.writeMap(map, dos);
        dos.close();
        fos.close();
    }

    private Map readMap(File map_file) throws IOException, AEIIException {
        FileInputStream fis = new FileInputStream(map_file);
        DataInputStream dis = new DataInputStream(fis);
        Map map = MapFactory.createMap(dis);
        dis.close();
        fis.close();
        return map;
    }

    public Map getMap(int map_id) throws IOException, AEIIException {
        synchronized (CHANGE_LOCK) {
            File map_file = new File("maps/m" + map_id);
            return readMap(map_file);
        }
    }

    public void addMap(Map map, String map_name) throws IOException, SQLException, MapExistingException {
        synchronized (CHANGE_LOCK) {
            String filename = map_name + ".aem";
            if (getContext().getDatabaseManager().isMapExisting(filename, map.getAuthor())) {
                throw new MapExistingException();
            } else {
                int map_id = getContext().getDatabaseManager().addMap(
                        getCapacity(map), filename, map.getAuthor().trim().toLowerCase(), MapFactory.isSymmetric(map));
                File map_file = new File("maps/m" + map_id);
                writeMap(map_file, map);
            }
        }
    }

    public boolean removeMap(int map_id) throws SQLException {
        synchronized (CHANGE_LOCK) {
            File map_file = new File("maps/m" + map_id);
            return getContext().getDatabaseManager().removeMap(map_id) && map_file.delete();
        }
    }

    public boolean updateMap(int map_id, String author, String filename) throws IOException, AEIIException {
        synchronized (CHANGE_LOCK) {
            File map_file = new File("maps/m" + map_id);
            try {
                if (author != null) {
                    Map map = readMap(map_file);
                    map.setAuthor(author);
                    writeMap(map_file, map);
                    author = author.trim().toLowerCase();
                    getContext().getDatabaseManager().changeMapAuthor(map_id, author);
                }
                if (filename != null) {
                    getContext().getDatabaseManager().changeMapFilename(map_id, filename);
                }
                return true;
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    public JSONArray getSerializedAuthorList(boolean symmetric) {
        JSONArray list = new JSONArray();
        try {
            for (String author : getContext().getDatabaseManager().getAuthors(symmetric)) {
                MapSnapshot snapshot = new MapSnapshot(0, "null", author);
                snapshot.setDirectory(true);
                list.put(snapshot.toJson());
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    public JSONArray getSerializedMapList(String author, boolean symmetric) {
        JSONArray list = new JSONArray();
        try {
            for (MapSnapshot snapshot : getContext().getDatabaseManager().getMapSnapshots(author, symmetric)) {
                list.put(snapshot.toJson());
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    private int getCapacity(Map map) {
        int player_count = 0;
        for (int team = 0; team < 4; team++) {
            if (map.hasTeamAccess(team)) {
                player_count++;
            }
        }
        return player_count;
    }

    public class MapExistingException extends Exception {
    }

}
