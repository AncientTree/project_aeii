package com.toyknight.aeii.manager;

import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.IOException;

/**
 * @author toyknight 12/29/2015.
 */
public class MapEditor {

    public static final int MODE_ERASER = 0x1;
    public static final int MODE_BRUSH = 0x2;
    public static final int MODE_HAND = 0x3;

    public static final int TYPE_TILE = 0x4;
    public static final int TYPE_UNIT = 0x5;

    private final MapEditorListener listener;

    private Map map;
    private String filename;

    private int mode;
    private int brush_type;
    private short selected_tile_index;
    private Unit selected_unit;
    private int selected_team;

    public MapEditor(MapEditorListener listener) {
        this.listener = listener;
    }

    public MapEditorListener getListener() {
        return listener;
    }

    public void initialize() {
        Map map = createEmptyMap(15, 15);
        setMap(map, "not defined");
        this.brush_type = TYPE_TILE;
        this.selected_tile_index = 0;
        this.selected_unit = UnitFactory.getSample(0);
        this.setSelectedTeam(0);
        setBrushType(TYPE_TILE);
    }

    public void setMap(Map map, String filename) {
        this.map = map;
        this.filename = filename;
        setMode(MODE_HAND);
        setBrushType(TYPE_TILE);
        getListener().onMapChange(map);
    }

    public Map getMap() {
        return map;
    }

    public void setMode(int mode) {
        this.mode = mode;
        getListener().onModeChange(mode);
    }

    public int getMode() {
        return mode;
    }

    public void setBrushType(int type) {
        this.brush_type = type;
    }

    public int getBrushType() {
        return brush_type;
    }

    public void setSelectedTileIndex(short index) {
        this.selected_tile_index = index;
        this.setBrushType(TYPE_TILE);
    }

    public short getSelectedTileIndex() {
        return selected_tile_index;
    }

    public void setSelectedUnit(Unit unit) {
        this.selected_unit = unit;
        this.setBrushType(TYPE_UNIT);
    }

    public Unit getSelectedUnit() {
        return selected_unit;
    }

    public void setSelectedTeam(int team) {
        this.selected_team = team;
        this.setBrushType(TYPE_UNIT);
    }

    public int getSelectedTeam() {
        return selected_team;
    }

    public String getFilename() {
        return filename;
    }

    public Map createEmptyMap(int width, int height) {
        short[][] map_data = new short[width][height];
        boolean[] team_access = new boolean[4];
        return new Map(map_data, team_access, "default");
    }

    public void saveMap(String filename, String author) {
        this.filename = filename;
        getMap().setAuthor(author);
        FileHandle map_file = FileProvider.getUserFile("map/" + filename + ".aem");
        try {
            MapFactory.createTeamAccess(map);
            if (map.getPlayerCount() >= 2) {
                MapFactory.writeMap(map, map_file);
                getListener().onMapSaved();
            } else {
                getListener().onError(Language.getText("EDITOR_ERROR_1"));
            }
        } catch (IOException ex) {
            getListener().onError(Language.getText("EDITOR_ERROR_2"));
        }
    }

}
