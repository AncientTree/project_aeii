package com.toyknight.aeii.entity;

import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by toyknight on 4/3/2015.
 */
public class Map {

    private final String author;
    private final boolean[] team_access;

    private final short[][] map_data;
    private final Unit[][] upper_unit_layer;
    private final HashMap<Point, Unit> unit_map;
    private final ArrayList<Tomb> tomb_list;
    private final Point[][] position_map;

    public Map(short[][] map_data, boolean[] team_access, String author) {
        this.author = author;
        this.team_access = team_access;
        this.map_data = map_data;
        this.unit_map = new HashMap();
        this.tomb_list = new ArrayList();

        upper_unit_layer = new Unit[getWidth()][getHeight()];
        position_map = new Point[getWidth()][getHeight()];
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                position_map[x][y] = new Point(x, y);
            }
        }
    }

    public String getAuthor() {
        return author;
    }

    public boolean isWithinMap(int x, int y) {
        return 0 <= x && x < getWidth() && 0 <= y && y < getHeight();
    }

    public final int getWidth() {
        return map_data.length;
    }

    public final int getHeight() {
        return map_data[0].length;
    }

    public void setTile(short index, int x, int y) {
        map_data[x][y] = index;
    }

    public short getTileIndex(int x, int y) {
        if (isWithinMap(x, y)) {
            return map_data[x][y];
        } else {
            return -1;
        }
    }

    public Tile getTile(int x, int y) {
        return TileFactory.getTile(map_data[x][y]);
    }

    public void addTomb(int x, int y) {
        Tomb tomb = new Tomb(x, y);
        if (tomb_list.contains(tomb)) {
            int index = tomb_list.indexOf(tomb);
            tomb_list.set(index, tomb);
        } else {
            tomb_list.add(tomb);
        }
    }

    public void removeTomb(int x, int y) {
        for (int i = 0; i < tomb_list.size(); i++) {
            Tomb tomb = tomb_list.get(i);
            if (tomb.x == x && tomb.y == y) {
                tomb_list.remove(i);
                break;
            }
        }
    }

    public boolean isTomb(int x, int y) {
        for (Tomb tomb : tomb_list) {
            if (tomb.x == x && tomb.y == y) {
                return true;
            }
        }
        return false;
    }

    public void updateTombs() {
        ArrayList<Tomb> list = new ArrayList(tomb_list);
        for (Tomb tomb : list) {
            tomb.update();
            if (tomb.getRemains() < 0) {
                tomb_list.remove(tomb);
            }
        }
    }

    public ArrayList<Tomb> getTombList() {
        return tomb_list;
    }

    public void moveUnit(Unit unit, int dest_x, int dest_y) {
        int start_x = unit.getX();
        int start_y = unit.getY();
        Point start_position = getPosition(start_x, start_y);
        Point dest_position = getPosition(dest_x, dest_y);
        if (canMove(dest_x, dest_y)) {
            unit.setX(dest_x);
            unit.setY(dest_y);
            if (UnitToolkit.isTheSameUnit(unit, upper_unit_layer[start_x][start_y])) {
                upper_unit_layer[start_x][start_y] = null;
            }
            if (UnitToolkit.isTheSameUnit(unit, unit_map.get(start_position))) {
                unit_map.remove(start_position);
            }
            if (unit_map.get(dest_position) == null) {
                unit_map.put(dest_position, unit);
            } else {
                upper_unit_layer[dest_x][dest_y] = unit;
            }
        }
    }

    public void addUnit(Unit unit) {
        Point position = getPosition(unit.getX(), unit.getY());
        if (!unit_map.containsKey(position)) {
            unit_map.put(position, unit);
        } else {
            if (upper_unit_layer[position.x][position.y] == null) {
                upper_unit_layer[position.x][position.y] = unit;
            }
        }
    }

    public Unit getUnit(int x, int y) {
        if (upper_unit_layer[x][y] != null) {
            return upper_unit_layer[x][y];
        } else {
            return unit_map.get(getPosition(x, y));
        }
    }

    public Unit getUnit(String unit_code) {
        Collection<Unit> units = unit_map.values();
        for (Unit unit : units) {
            if (unit.getUnitCode().equals(unit_code)) {
                return unit;
            }
        }
        return null;
    }

    public void removeUnit(int x, int y) {
        unit_map.remove(getPosition(x, y));
    }

    public Collection<Unit> getUnitSet() {
        return unit_map.values();
    }

    public Set<Point> getUnitPositionSet() {
        return unit_map.keySet();
    }

    public int getUnitCount(int team) {
        Collection<Unit> units = getUnitSet();
        int count = 0;
        for (Unit unit : units) {
            if (unit.getTeam() == team) {
                count++;
            }
        }
        for (Unit[] unit_row : upper_unit_layer) {
            for (Unit unit : unit_row) {
                if (unit != null && unit.getTeam() == team) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean canMove(int x, int y) {
        Point dest_position = getPosition(x, y);
        return unit_map.get(dest_position) == null || upper_unit_layer[x][y] == null;
    }

    public boolean canStandby(Unit unit) {
        Point position = getPosition(unit.getX(), unit.getY());
        if (UnitToolkit.isTheSameUnit(unit, upper_unit_layer[unit.getX()][unit.getY()])) {
            return unit_map.get(position) == null;
        } else {
            return UnitToolkit.isTheSameUnit(unit, unit_map.get(position));
        }
    }

    public Point getPosition(int x, int y) {
        return position_map[x][y];
    }

    public int getPlayerCount() {
        int count = 0;
        for (int team = 0; team < 4; team++) {
            if (getTeamAccess(team) == true) {
                count++;
            }
        }
        return count;
    }

    public boolean getTeamAccess(int team) {
        return team_access[team];
    }

}
