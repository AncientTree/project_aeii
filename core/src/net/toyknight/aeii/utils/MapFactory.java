package net.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;

import java.io.*;
import java.util.Scanner;

/**
 * @author toyknight 4/3/2015.
 */
public class MapFactory {

    private MapFactory() {
    }

    public static Map createMap(FileHandle map_file) throws AEIIException {
        try {
            DataInputStream dis = new DataInputStream(map_file.read());
            return createMap(dis);
        } catch (GdxRuntimeException ex) {
            throw new AEIIException("Error reading map file", ex);
        }
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
            if (5 <= width && width <= 21 && 5 <= height && height <= 21) {
                Map map = new Map(width, height);
                map.setTeamAccess(team_access);
                map.setAuthor(author_name);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        short tile_index = checkTile(dis.readShort());
                        map.setTile(tile_index, x, y);
                    }
                }
                int unit_count = dis.readInt();
                for (int i = 0; i < unit_count; i++) {
                    int team = dis.readInt();
                    int index = checkUnit(dis.readInt());
                    int x = dis.readInt();
                    int y = dis.readInt();
                    Unit unit = UnitFactory.createUnit(index, team);
                    unit.setX(x);
                    unit.setY(y);
                    map.addUnit(unit);
                }
                dis.close();
                return map;
            } else {
                dis.close();
                throw new AEIIException("Invalid map size!");
            }
        } catch (IOException ex) {
            throw new AEIIException("broken map file!");
        }
    }

    private static short checkTile(short index) throws AEIIException {
        if (0 <= index && index < TileFactory.getTileCount()) {
            return index;
        } else {
            throw new AEIIException("broken map file!");
        }
    }

    private static int checkUnit(int index) throws AEIIException {
        if (0 <= index && index < UnitFactory.getUnitCount()) {
            return index;
        } else {
            throw new AEIIException("broken map file!");
        }
    }

    public static void writeMap(Map map, FileHandle map_file) throws IOException {
        writeMap(map, new DataOutputStream(map_file.write(false)));
    }

    public static void writeMap(Map map, DataOutputStream fos) throws IOException {
        fos.writeUTF(map.getAuthor());
        for (int team = 0; team < 4; team++) {
            fos.writeBoolean(map.hasTeamAccess(team));
        }
        fos.writeInt(map.getWidth());
        fos.writeInt(map.getHeight());
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                fos.writeShort(map.getTileIndex(x, y));
            }
        }
        Array<Unit> unit_list = map.getUnits().toArray();
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

    public static void createTeamAccess(Map map) {
        map.resetTeamAccess();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                if (tile.isCastle() && tile.getTeam() >= 0) {
                    map.setTeamAccess(tile.getTeam(), true);
                }
            }
        }
        ObjectMap.Keys<Position> unit_positions = map.getUnitPositions();
        for (Position position : unit_positions) {
            Unit unit = map.getUnit(position.x, position.y);
            if (!map.hasTeamAccess(unit.getTeam())) {
                map.setTeamAccess(unit.getTeam(), true);
            }
        }
    }

    public static MapSnapshot createMapSnapshot(FileHandle map_file) {
        try {
            DataInputStream dis = new DataInputStream(map_file.read());
            dis.readUTF();
            int count = 0;
            for (int team = 0; team < 4; team++) {
                boolean has_access = dis.readBoolean();
                if (has_access) {
                    count++;
                }
            }
            dis.close();
            MapSnapshot snapshot = new MapSnapshot();
            snapshot.file = map_file;
            snapshot.capacity = count;
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

    public static boolean isBilaterallySymmetric(Map map) {
        int map_width = map.getWidth();
        for (int x = 0; x < map_width / 2; x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (!isEquivalent(map.getTileIndex(x, y), map.getTileIndex(map_width - x - 1, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isLongitudinallySymmetric(Map map) {
        int map_height = map.getHeight();
        for (int y = 0; y < map_height / 2; y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (!isEquivalent(map.getTileIndex(x, y), map.getTileIndex(x, map_height - y - 1))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isCentrosymmetric(Map map) {
        int map_width = map.getWidth();
        int map_height = map.getHeight();
        for (int x = 0; x < map_width / 2; x++) {
            for (int y = 0; y < map_height; y++) {
                if (!isEquivalent(
                        map.getTileIndex(x, y), map.getTileIndex(map_width - x - 1, map_height - y - 1))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSymmetric(Map map) {
        return isBilaterallySymmetric(map) || isLongitudinallySymmetric(map) || isCentrosymmetric(map);
    }

    public static boolean isEquivalent(int tile_index_a, int tile_index_b) {
        if ((32 <= tile_index_a && tile_index_a <= 35) || (32 <= tile_index_b && tile_index_b <= 35)) {
            return tile_index_a == tile_index_b;
        }
        Tile tile_a = TileFactory.getTile(tile_index_a);
        Tile tile_b = TileFactory.getTile(tile_index_b);
        return tile_a.isCastle() && tile_b.isCastle()
                || tile_a.isVillage() && tile_b.isVillage()
                || tile_a.isTemple() && tile_b.isTemple()
                || (tile_a.getType() == tile_b.getType()
                && tile_a.getStepCost() == tile_b.getStepCost()
                && tile_a.getHpRecovery() == tile_b.getHpRecovery()
                && tile_a.getDefenceBonus() == tile_b.getDefenceBonus());
    }

    public static class MapSnapshot {

        public FileHandle file;

        public int capacity;

        @Override
        public String toString() {
            return "(" + capacity + ") " + file.nameWithoutExtension();
        }

    }

}
