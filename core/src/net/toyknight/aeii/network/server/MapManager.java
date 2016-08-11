package net.toyknight.aeii.network.server;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.network.entity.MapSnapshot;
import net.toyknight.aeii.utils.MapFactory;
import org.json.JSONArray;

import java.io.*;

/**
 * @author by toyknight 6/10/2016.
 */
public class MapManager {

    private final Object MAP_LOCK = new Object();

    private final FileFilter map_file_filter = new MapFileFilter();

    private final ObjectMap<String, ObjectSet<MapSnapshot>> maps = new ObjectMap<String, ObjectSet<MapSnapshot>>();

    public void initialize() throws AEIIException {
        File map_dir = new File("maps");
        if (map_dir.exists()) {
            loadMaps(map_dir);
        } else {
            boolean success = map_dir.mkdir();
            if (!success) {
                throw new AEIIException("Cannot make map directory");
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
        File map_file = new File("maps/" + filename);
        FileInputStream fis = new FileInputStream(map_file);
        DataInputStream dis = new DataInputStream(fis);
        return MapFactory.createMap(dis);
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
        synchronized (MAP_LOCK) {
            addSnapshot(snapshot);
        }
    }

    public void addSnapshot(MapSnapshot snapshot) {
        String author = snapshot.getAuthor().toLowerCase();
        if (!maps.containsKey(snapshot.getAuthor())) {
            maps.put(author, new ObjectSet<MapSnapshot>());
        }
        maps.get(author).add(snapshot);
    }

    public JSONArray getSerializedAuthorList() {
        JSONArray list = new JSONArray();
        synchronized (MAP_LOCK) {
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
        synchronized (MAP_LOCK) {
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
