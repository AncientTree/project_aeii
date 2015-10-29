package com.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;

import java.io.*;
import java.util.Scanner;
/**
 * @author toyknight 4/3/2015.
 */
public class MapFactory {

    private MapFactory() {
    }

    public static Map createMap(FileHandle map_file) throws AEIIException {
        DataInputStream dis = new DataInputStream(map_file.read());
        return createMap(dis);
    }

    public static Map createMap(DataInputStream dis) throws AEIIException {
        try {
            String author_name = dis.readUTF();
            boolean[] team_access = new boolean[4];
            for (int team = 0; team < 4; team++) {
                team_access[team] = dis.readBoolean();
            }
            int width = dis.readInt();
            int height = dis.readInt();
            short[][] map_data = new short[width][height];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    map_data[i][j] = dis.readShort();
                }
            }
            Map map = new Map(map_data, team_access, author_name);
            int unit_count = dis.readInt();
            for (int i = 0; i < unit_count; i++) {
                int team = dis.readInt();
                int index = dis.readInt();
                int x = dis.readInt();
                int y = dis.readInt();
                Unit unit = UnitFactory.createUnit(index, team);
                unit.setX(x);
                unit.setY(y);
                map.addUnit(unit);
            }
            dis.close();
            return map;
        } catch (IOException ex) {
            throw new AEIIException("broken map file!");
        }
    }

    public static void writeMap(Map map, FileHandle map_file) throws IOException {
        DataOutputStream fos = new DataOutputStream(map_file.write(false));
        fos.writeUTF(map.getAuthor());
        for (boolean b : map.getTeamAccessTable()) {
            fos.writeBoolean(b);
        }
        fos.writeInt(map.getWidth());
        fos.writeInt(map.getHeight());
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                fos.writeShort(map.getTileIndex(x, y));
            }
        }
        Array<Unit> unit_list = map.getUnitSet().toArray();
        fos.writeInt(unit_list.size);
        for (Unit unit : unit_list) {
            fos.writeInt(unit.getTeam());
            fos.writeInt(unit.getIndex());
            fos.writeInt(unit.getX());
            fos.writeInt(unit.getY());
        }
        fos.flush();
        fos.close();
    }

    public static int getPlayerCount(FileHandle map_file) throws IOException {
        DataInputStream dis = new DataInputStream(map_file.read());
        return getPlayerCount(dis);
    }

    public static int getPlayerCount(DataInputStream dis) throws IOException {
        int count = 0;
        dis.readUTF();
        for (int team = 0; team < 4; team++) {
            boolean has_access = dis.readBoolean();
            if (has_access) {
                count++;
            }
        }
        dis.close();
        return count;
    }

    public static void createTeamAccess(Map map) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                if (tile.isCastle() && tile.getTeam() >= 0) {
                    map.setTeamAccess(tile.getTeam(), true);
                }
            }
        }
        ObjectMap.Keys<Point> unit_positions = map.getUnitPositionSet();
        for (Point position : unit_positions) {
            Unit unit = map.getUnit(position.x, position.y);
            if (!map.hasTeamAccess(unit.getTeam())) {
                map.setTeamAccess(unit.getTeam(), true);
            }
        }
    }

    public static MapSnapshot createMapSnapshot(FileHandle map_file) {
        try {
            MapSnapshot snapshot = new MapSnapshot();
            snapshot.file = map_file;
            snapshot.capacity = getPlayerCount(map_file);
            return snapshot;
        } catch (IOException ex) {
            return null;
        }
    }

    public static Array<MapSnapshot> getSystemMapSnapshots() {
        FileHandle internal_map_config = FileProvider.getAssetsFile("map/config.dat");
        Scanner din = new Scanner(internal_map_config.read());
        int map_count = din.nextInt();
        din.nextLine();
        Array<MapSnapshot> snapshots = new Array<MapSnapshot>();
        for (int i = 0; i < map_count; i++) {
            String map_name = din.nextLine().trim();
            MapSnapshot snapshot = createMapSnapshot(FileProvider.getAssetsFile("map/" + map_name));
            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }
        din.close();
        return snapshots;
    }

    public static Array<MapSnapshot> getUserMapSnapshots() {
        Array<MapSnapshot> snapshots = new Array<MapSnapshot>();
        FileHandle[] user_maps = FileProvider.getUserDir("map").list();
        for (FileHandle map_file : user_maps) {
            MapSnapshot snapshot = createMapSnapshot(map_file);
            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    public static Array<MapSnapshot> getAllMapSnapshots() {
        Array<MapSnapshot> system_maps = getSystemMapSnapshots();
        Array<MapSnapshot> user_maps = getUserMapSnapshots();
        system_maps.addAll(user_maps);
        return system_maps;
    }

    public static class MapSnapshot {

        public FileHandle file;

        public int capacity;

        @Override
        public String toString() {
            return "(" + capacity + ") " + file.name();
        }

    }

}
