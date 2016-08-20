package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.entity.MapSnapshot;
import net.toyknight.aeii.server.ServerException;
import net.toyknight.aeii.utils.MapFactory;
import org.json.JSONArray;

import java.io.*;

/**
 * @author by toyknight 6/10/2016.
 */
public class MapManager {

    private static final String TAG = "MAP MANAGER";

    private final Object CHANGE_LOCK = new Object();

    private final FileFilter map_file_filter = new MapFileFilter();

    private final ObjectMap<String, ObjectSet<MapSnapshot>> maps = new ObjectMap<String, ObjectSet<MapSnapshot>>();

    public void initialize() throws ServerException {
        File map_dir = new File("maps");
        if (map_dir.exists() && map_dir.isDirectory()) {
            loadMaps(map_dir);
        } else {
            boolean success = map_dir.mkdir();
            if (!success) {
                throw new ServerException(TAG, "Cannot make map directory");
            }
        }
    }

    public void loadMaps(File map_dir) {
        File[] map_files = map_dir.listFiles(map_file_filter);
        for (File map_file : map_files) {
            try {
                FileInputStream fis = new FileInputStream(map_file);
                DataInputStream dis = new DataInputStream(fis);
                Map map = MapFactory.createMap(dis);

                MapSnapshot snapshot = new MapSnapshot(getCapacity(map), map_file.getName(), map.getAuthor());
                addSnapshot(snapshot);
            } catch (IOException ignored) {
            } catch (AEIIException ignored) {
            }
        }
    }

    public Map getMap(String filename) throws IOException, AEIIException {
        synchronized (CHANGE_LOCK) {
            File map_file = new File("maps/" + filename);
            FileInputStream fis = new FileInputStream(map_file);
            DataInputStream dis = new DataInputStream(fis);
            return MapFactory.createMap(dis);
        }
    }

    public void addMap(Map map, String map_name) throws IOException {
        File map_file = new File("maps/" + map_name + ".aem");
        if (map_file.exists()) {
            throw new IOException("Map " + map_file.getName() + " already exists!");
        }
        FileOutputStream fos = new FileOutputStream(map_file);
        DataOutputStream dos = new DataOutputStream(fos);
        MapFactory.writeMap(map, dos);

        MapSnapshot snapshot = new MapSnapshot(getCapacity(map), map_file.getName(), map.getAuthor());
        addSnapshot(snapshot);
    }

    public boolean removeMap(String filename) {
        try {
            Map map = getMap(filename);
            synchronized (CHANGE_LOCK) {
                String author = map.getAuthor().trim().toLowerCase();
                if (maps.containsKey(author)) {
                    File map_file = new File("maps/" + filename);
                    ObjectSet<MapSnapshot> snapshots = maps.get(author);
                    MapSnapshot target_snapshot = null;
                    for (MapSnapshot snapshot : snapshots) {
                        if (snapshot.getFilename().equals(map_file.getName())) {
                            target_snapshot = snapshot;
                            break;
                        }
                    }
                    return target_snapshot != null && snapshots.remove(target_snapshot) && map_file.delete();
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public void addSnapshot(MapSnapshot snapshot) {
        synchronized (CHANGE_LOCK) {
            String author = snapshot.getAuthor().trim().toLowerCase();
            if (!maps.containsKey(author)) {
                maps.put(author, new ObjectSet<MapSnapshot>());
            }
            maps.get(author).add(snapshot);
        }
    }

    public JSONArray getSerializedAuthorList() {
        JSONArray list = new JSONArray();
        synchronized (CHANGE_LOCK) {
            for (String author : maps.keys()) {
                MapSnapshot snapshot = new MapSnapshot(0, "null", author);
                snapshot.setDirectory(true);
                list.put(snapshot.toJson());
            }
        }
        return list;
    }

    public JSONArray getSerializedMapList(String author) {
        JSONArray list = new JSONArray();
        synchronized (CHANGE_LOCK) {
            if (maps.containsKey(author)) {
                for (MapSnapshot snapshot : maps.get(author)) {
                    list.put(snapshot.toJson());
                }
            }
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

    private class MapFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            return !file.isDirectory() && file.getName().endsWith(".aem");
        }

    }

}
