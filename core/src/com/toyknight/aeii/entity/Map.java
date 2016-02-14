package com.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitToolkit;

/**
 * @author toyknight 4/3/2015.
 */
public class Map {

    protected String author;

    protected final short[][] map_data;

    protected final Unit[][] upper_unit_layer;
    protected final ObjectMap<Position, Unit> units;

    protected final ObjectSet<Tomb> tombs;

    private final ObjectSet<Position> castle_positions;
    private final ObjectSet<Position> village_positions;

    protected final boolean[] team_access;

    protected final Position[][] position_map;

    public Map(Map map) {
        this(map.getWidth(), map.getHeight());
        author = map.author;
        System.arraycopy(map.team_access, 0, team_access, 0, 4);
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                setTile(map.getTileIndex(x, y), x, y);
                Unit unit = map.upper_unit_layer[x][y];
                if (unit != null) {
                    upper_unit_layer[x][y] = new Unit(unit);
                }
            }
        }
        for (Position position : map.getUnitPositions()) {
            Unit unit = map.units.get(position);
            units.put(position, new Unit(unit));
        }
        for (Tomb tomb : map.tombs) {
            tombs.add(new Tomb(tomb));
        }
    }

    public Map(int width, int height) {
        map_data = new short[width][height];
        team_access = new boolean[4];

        units = new ObjectMap<Position, Unit>();
        tombs = new ObjectSet<Tomb>();
        upper_unit_layer = new Unit[width][height];
        position_map = new Position[width][height];
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                position_map[x][y] = new Position(x, y);
            }
        }
        castle_positions = new ObjectSet<Position>();
        village_positions = new ObjectSet<Position>();
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public boolean hasTeamAccess(int team) {
        return team_access[team];
    }

    public void setTeamAccess(int team, boolean access) {
        team_access[team] = access;
    }

    public void setTeamAccess(boolean[] access_table) {
        if (access_table.length == 4) {
            for (int team = 0; team < 4; team++) {
                setTeamAccess(team, access_table[team]);
            }
        }
    }

    public int getWidth() {
        return map_data.length;
    }

    public int getHeight() {
        return map_data[0].length;
    }

    public boolean isWithinMap(int x, int y) {
        return 0 <= x && x < getWidth() && 0 <= y && y < getHeight();
    }

    public void setTile(short index, int x, int y) {
        map_data[x][y] = index;

        Position position = getPosition(x, y);
        Tile tile = getTile(position);
        if (tile.isCastle()) {
            getCastlePositions().add(position);
        } else {
            if (tile.isVillage()) {
                getVillagePositions().add(position);
            } else {
                getCastlePositions().remove(position);
                getVillagePositions().remove(position);
            }
        }
    }

    public short getTileIndex(int x, int y) {
        if (isWithinMap(x, y)) {
            return map_data[x][y];
        } else {
            return -1;
        }
    }

    public Tile getTile(int x, int y) {
        if (isWithinMap(x, y)) {
            return TileFactory.getTile(map_data[x][y]);
        } else {
            return null;
        }
    }

    public Tile getTile(Position position) {
        return getTile(position.x, position.y);
    }

    public ObjectSet<Position> getCastlePositions() {
        return castle_positions;
    }

    public ObjectSet<Position> getCastlePositions(int team) {
        ObjectSet<Position> positions = new ObjectSet<Position>();
        for (Position position : getCastlePositions()) {
            if (getTile(position).getTeam() == team) {
                positions.add(position);
            }
        }
        return positions;
    }

    public ObjectSet<Position> getVillagePositions() {
        return village_positions;
    }

    public ObjectSet<Position> getVillagePositions(int team) {
        ObjectSet<Position> positions = new ObjectSet<Position>();
        for (Position position : getVillagePositions()) {
            if (getTile(position).getTeam() == team) {
                positions.add(position);
            }
        }
        return positions;
    }

    public void addTomb(int x, int y) {
        addTomb(new Tomb(x, y));
    }

    public void addTomb(Tomb tomb) {
        tombs.add(tomb);
    }

    public void removeTomb(int x, int y) {
        for (ObjectSet.ObjectSetIterator<Tomb> iterator = tombs.iterator(); iterator.hasNext(); ) {
            Tomb tomb = iterator.next();
            if (tomb.x == x && tomb.y == y) {
                iterator.remove();
                break;
            }
        }
    }

    public boolean isTomb(int x, int y) {
        for (Tomb tomb : tombs) {
            if (tomb.x == x && tomb.y == y) {
                return true;
            }
        }
        return false;
    }

    public boolean isTomb(Position position) {
        return isTomb(position.x, position.y);
    }

    public void updateTombs() {
        for (ObjectSet.ObjectSetIterator<Tomb> iterator = tombs.iterator(); iterator.hasNext(); ) {
            Tomb tomb = iterator.next();
            tomb.update();
            if (tomb.getRemains() < 0) {
                iterator.remove();
            }
        }
    }

    public ObjectSet<Tomb> getTombs() {
        return tombs;
    }

    public void moveUnit(Unit unit, int dest_x, int dest_y) {
        int start_x = unit.getX();
        int start_y = unit.getY();
        Position start_position = getPosition(start_x, start_y);
        Position dest_position = getPosition(dest_x, dest_y);
        if (canMove(dest_x, dest_y)) {
            unit.setX(dest_x);
            unit.setY(dest_y);
            if (UnitToolkit.isTheSameUnit(unit, upper_unit_layer[start_x][start_y])) {
                upper_unit_layer[start_x][start_y] = null;
            }
            if (UnitToolkit.isTheSameUnit(unit, units.get(start_position))) {
                units.remove(start_position);
            }
            if (units.get(dest_position) == null) {
                units.put(dest_position, unit);
            } else {
                upper_unit_layer[dest_x][dest_y] = unit;
            }
        }
    }

    public void addUnit(Unit unit) {
        addUnit(unit, false);
    }

    public void addUnit(Unit unit, boolean replace) {
        Position position = getPosition(unit.getX(), unit.getY());
        if (replace) {
            units.put(position, unit);
        } else {
            if (!units.containsKey(position)) {
                units.put(position, unit);
            } else {
                if (upper_unit_layer[position.x][position.y] == null) {
                    upper_unit_layer[position.x][position.y] = unit;
                }
            }
        }
    }

    public Unit getUnit(int x, int y) {
        if (isWithinMap(x, y)) {
            if (upper_unit_layer[x][y] != null) {
                return upper_unit_layer[x][y];
            } else {
                return units.get(getPosition(x, y));
            }
        } else {
            return null;
        }
    }

    public Unit getUnit(Position position) {
        return getUnit(position.x, position.y);
    }

    public Unit getUnit(String unit_code) {
        ObjectMap.Values<Unit> units = this.units.values();
        for (Unit unit : units) {
            if (unit.getUnitCode().equals(unit_code)) {
                return unit;
            }
        }
        return null;
    }

    public void removeUnit(int x, int y) {
        units.remove(getPosition(x, y));
    }

    public ObjectMap.Values<Unit> getUnits() {
        return units.values();
    }

    public ObjectSet<Unit> getUnits(int team) {
        ObjectSet<Unit> units = new ObjectSet<Unit>();
        for (Unit unit : getUnits()) {
            if (unit.getTeam() == team) {
                units.add(unit);
            }
        }
        return units;
    }

    public ObjectMap.Keys<Position> getUnitPositions() {
        return units.keys();
    }

    public void removeTeam(int team) {
        Array<Position> positions = new Array<Position>(getUnitPositions().toArray());
        for (Position position : positions) {
            Unit unit = getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                removeUnit(position.x, position.y);
            }
        }
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Unit unit = getUnit(x, y);
                if (unit != null && unit.getTeam() == team) {
                    removeUnit(x, y);
                }
                Tile tile = getTile(x, y);
                if (tile.getTeam() == team && tile.isCapturable()) {
                    setTile(tile.getCapturedTileIndex(-1), x, y);
                }
            }
        }
    }

    public int getPopulation(int team) {
        return getUnitCount(team, false);
    }

    public int getUnitCount(int team, boolean count_skeleton) {
        ObjectMap.Values<Unit> units = getUnits();
        int count = 0;
        for (Unit unit : units) {
            if (unit != null && unit.getTeam() == team) {
                if (unit.isSkeleton()) {
                    count += count_skeleton ? 1 : 0;
                } else {
                    count++;
                }
            }
        }
        for (Unit[] unit_row : upper_unit_layer) {
            for (Unit unit : unit_row) {
                if (unit != null && unit.getTeam() == team) {
                    if (unit.isSkeleton()) {
                        count += count_skeleton ? 1 : 0;
                    } else {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public boolean canMove(int x, int y) {
        Position dest_position = getPosition(x, y);
        return units.get(dest_position) == null || upper_unit_layer[x][y] == null;
    }

    public boolean canStandby(Unit unit) {
        Position position = getPosition(unit.getX(), unit.getY());
        if (UnitToolkit.isTheSameUnit(unit, upper_unit_layer[unit.getX()][unit.getY()])) {
            return units.get(position) == null;
        } else {
            return UnitToolkit.isTheSameUnit(unit, units.get(position));
        }
    }

    public Position getPosition(int x, int y) {
        if (isWithinMap(x, y)) {
            return position_map[x][y];
        } else {
            return null;
        }
    }

    public Position getPosition(Unit unit) {
        return getPosition(unit.getX(), unit.getY());
    }

    public int getPlayerCount() {
        int count = 0;
        for (int team = 0; team < 4; team++) {
            if (hasTeamAccess(team)) {
                count++;
            }
        }
        return count;
    }

    public int getCastleCount(int team) {
        int count = 0;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Tile tile = getTile(x, y);
                if (tile.isCastle() && tile.getTeam() == team) {
                    count++;
                }
            }
        }
        return count;
    }

}
