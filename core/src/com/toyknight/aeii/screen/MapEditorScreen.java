package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.animator.CursorAnimator;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.renderer.TileRenderer;
import com.toyknight.aeii.renderer.UnitRenderer;
import com.toyknight.aeii.screen.editor.*;
import com.toyknight.aeii.screen.widgets.CircleButton;
import com.toyknight.aeii.utils.*;

import java.io.IOException;
import java.util.Set;

/**
 * @author toyknight 6/1/2015.
 */
public class MapEditorScreen extends StageScreen implements MapCanvas {

    private static final int MODE_ERASER = 0x1;
    private static final int MODE_BRUSH = 0x2;
    private static final int MODE_HAND = 0x3;

    public static final int TYPE_TILE = 0x4;
    public static final int TYPE_UNIT = 0x5;

    private final TileRenderer tile_renderer;
    private final UnitRenderer unit_renderer;

    private final CursorAnimator cursor;
    private final MapViewport viewport;

    private MapResizeDialog map_resize_dialog;
    private MapSaveDialog map_save_dialog;
    private MapOpenDialog map_open_dialog;
    private Dialog dialog;

    private CircleButton btn_hand;
    private CircleButton btn_brush;
    private CircleButton btn_eraser;

    private Map map;
    private String filename;

    private int mode;
    private int brush_type;
    private short selected_tile_index;
    private Unit selected_unit;
    private int selected_team;

    private int pointer_x;
    private int pointer_y;
    private int cursor_map_x;
    private int cursor_map_y;

    public MapEditorScreen(AEIIApplication context) {
        super(context);
        this.tile_renderer = new TileRenderer(ts);
        this.unit_renderer = new UnitRenderer(this, ts);

        this.cursor = new CursorAnimator(this, ts);

        this.viewport = new MapViewport();
        this.viewport.width = Gdx.graphics.getWidth();
        this.viewport.height = Gdx.graphics.getHeight() - ts;

        this.initComponents();
    }

    private void initComponents() {
        TileSelector tile_selector = new TileSelector(this);
        tile_selector.setBounds(0, 0, ts * 4, Gdx.graphics.getHeight());
        this.addActor(tile_selector);

        int usw = ts * 2 + ts / 4 * 3;
        UnitSelector unit_selector = new UnitSelector(this);
        unit_selector.setBounds(Gdx.graphics.getWidth() - usw, 0, usw, Gdx.graphics.getHeight());
        this.addActor(unit_selector);

        int mrw = ts * 8;
        int mrh = ts * 4;
        this.map_resize_dialog = new MapResizeDialog(this);
        this.map_resize_dialog.setBounds((Gdx.graphics.getWidth() - mrw) / 2, (Gdx.graphics.getHeight() - mrh) / 2, mrw, mrh);
        this.map_resize_dialog.setVisible(false);
        this.addActor(map_resize_dialog);

        int msw = ts * 6;
        int msh = ts * 4;
        this.map_save_dialog = new MapSaveDialog(this);
        this.map_save_dialog.setBounds((Gdx.graphics.getWidth() - msw) / 2, (Gdx.graphics.getHeight() - msh) / 2, msw, msh);
        this.map_save_dialog.setVisible(false);
        this.addActor(map_save_dialog);

        int mow = ts * 6;
        int moh = ts * 8;
        this.map_open_dialog = new MapOpenDialog(this);
        this.map_open_dialog.setBounds((Gdx.graphics.getWidth() - mow) / 2, (Gdx.graphics.getHeight() - moh) / 2, msw, moh);
        this.map_open_dialog.setVisible(false);
        this.addActor(map_open_dialog);

        Table button_bar = new Table();
        btn_hand = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_hand"), ts);
        btn_hand.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setMode(MODE_HAND);
            }
        });
        button_bar.add(btn_hand);
        btn_brush = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_brush"), ts);
        btn_brush.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setMode(MODE_BRUSH);
            }
        });
        button_bar.add(btn_brush).padLeft(ts / 4);
        btn_eraser = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_eraser"), ts);
        btn_eraser.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setMode(MODE_ERASER);
            }
        });
        button_bar.add(btn_eraser).padLeft(ts / 4);

        CircleButton btn_resize = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_resize"), ts);
        btn_resize.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (map_resize_dialog.isVisible()) {
                    map_resize_dialog.setVisible(false);
                } else {
                    closeAllDialogs();
                    map_resize_dialog.display();
                }
            }
        });
        button_bar.add(btn_resize).padLeft(ts / 4);

        CircleButton btn_save = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_save"), ts);
        btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (map_save_dialog.isVisible()) {
                    map_save_dialog.setVisible(false);
                } else {
                    closeAllDialogs();
                    map_save_dialog.display();
                }
            }
        });
        button_bar.add(btn_save).padLeft(ts / 4);

        CircleButton btn_load = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_load"), ts);
        btn_load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (map_open_dialog.isVisible()) {
                    map_open_dialog.setVisible(false);
                } else {
                    closeAllDialogs();
                    map_open_dialog.display();
                }
            }
        });
        button_bar.add(btn_load).padLeft(ts / 4);

        CircleButton btn_exit = new CircleButton(CircleButton.LARGE, ResourceManager.getEditorTexture("icon_exit"), ts);
        btn_exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMainMenuScreen();
            }
        });
        button_bar.add(btn_exit).padLeft(ts / 4);

        button_bar.layout();
        button_bar.setBounds(
                tile_selector.getWidth(), 0,
                Gdx.graphics.getWidth() - tile_selector.getWidth() - unit_selector.getWidth(), ts * 33 / 24);
        this.addActor(button_bar);

        int dw = ts * 6;
        int dh = ts / 2 * 5;
        this.dialog = new Dialog("", getContext().getSkin());
        this.dialog.setBounds((Gdx.graphics.getWidth() - dw) / 2, (Gdx.graphics.getHeight() - dh) / 2, dw, dh);
        this.dialog.setVisible(false);
        this.dialog.setModal(true);
        this.addActor(dialog);
    }

    @Override
    public void closeAllDialogs() {
        map_resize_dialog.setVisible(false);
        map_save_dialog.setVisible(false);
        map_open_dialog.setVisible(false);
    }

    public void setMode(int mode) {
        this.mode = mode;
        btn_hand.setHold(false);
        btn_brush.setHold(false);
        btn_eraser.setHold(false);
        switch (mode) {
            case MODE_HAND:
                btn_hand.setHold(true);
                break;
            case MODE_BRUSH:
                btn_brush.setHold(true);
                break;
            case MODE_ERASER:
                btn_eraser.setHold(true);
                break;
            default:
                //do nothing
        }
    }

    public void setBrushType(int type) {
        this.brush_type = type;
    }

    public void setSelectedTileIndex(short index) {
        this.selected_tile_index = index;
        this.setBrushType(TYPE_TILE);
    }

    public void setSelectedUnit(Unit unit) {
        this.selected_unit = unit;
        this.setBrushType(TYPE_UNIT);
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

    @Override
    public void draw() {
        batch.begin();
        drawMap();
        drawUnits();
        drawBrushTarget();
        drawCursor();
//        batch.draw(ResourceManager.getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), ts);
//
//        batch.draw(ResourceManager.getTileTexture(selected_tile_index), 0, 0, ts, ts);
//        batch.draw(
//                ResourceManager.getUnitTexture(getSelectedTeam(), selected_unit.getIndex(), 0, 0),
//                Gdx.graphics.getWidth() - ts, 0, ts, ts);
//
//        BorderRenderer.drawBorder(batch, 0, 0, ts, ts);
//        BorderRenderer.drawBorder(batch, ts, 0, Gdx.graphics.getWidth() - ts * 2, ts);
//        BorderRenderer.drawBorder(batch, Gdx.graphics.getWidth() - ts, 0, ts, ts);
        batch.end();
        super.draw();
    }

    private void drawMap() {
        for (int x = 0; x < getMap().getWidth(); x++) {
            for (int y = 0; y < getMap().getHeight(); y++) {
                int sx = getXOnScreen(x);
                int sy = getYOnScreen(y);
                if (isWithinPaintArea(sx, sy)) {
                    int index = getMap().getTileIndex(x, y);
                    tile_renderer.drawTile(batch, index, sx, sy);
                    Tile tile = TileFactory.getTile(index);
                    if (tile.getTopTileIndex() != -1) {
                        int top_tile_index = tile.getTopTileIndex();
                        tile_renderer.drawTopTile(batch, top_tile_index, sx, sy + ts);
                    }
                }
            }
        }
    }

    private void drawUnits() {
        Set<Point> unit_positions = getMap().getUnitPositionSet();
        for (Point position : unit_positions) {
            Unit unit = getMap().getUnit(position.x, position.y);
            int unit_x = unit.getX();
            int unit_y = unit.getY();
            int sx = getXOnScreen(unit_x);
            int sy = getYOnScreen(unit_y);
            if (isWithinPaintArea(sx, sy)) {
                unit_renderer.drawUnit(batch, unit, unit_x, unit_y);
            }
        }
    }

    private void drawBrushTarget() {
        if (mode == MODE_BRUSH) {
            int sx = getXOnScreen(cursor_map_x);
            int sy = getYOnScreen(cursor_map_y);
            switch (brush_type) {
                case TYPE_TILE:
                    batch.draw(ResourceManager.getTileTexture(selected_tile_index), sx, sy, ts, ts);
                    break;
                case TYPE_UNIT:
                    batch.draw(
                            ResourceManager.getUnitTexture(getSelectedTeam(), selected_unit.getIndex(), 0, 0),
                            sx, sy, ts, ts);
                    break;
                default:
                    //do nothing
            }
        }
    }

    private void drawCursor() {
        if (mode == MODE_ERASER) {
            int cursor_x = getCursorMapX();
            int cursor_y = getCursorMapY();
            cursor.render(batch, cursor_x, cursor_y);
        }
    }

    @Override
    public void act(float delta) {
        tile_renderer.update(delta);
        unit_renderer.update(delta);
        cursor.update(delta);
        super.act(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        createEmptyMap(15, 15);
        locateViewport(0, 0);
        cursor_map_x = 0;
        cursor_map_y = 0;
        setMode(MODE_BRUSH);
        this.filename = "not defined";
        this.brush_type = TYPE_TILE;
        this.selected_tile_index = 0;
        this.selected_unit = UnitFactory.getSample(0);
        this.setSelectedTeam(0);
        setBrushType(TYPE_TILE);
    }

    public Map getMap() {
        return map;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchDown(screenX, screenY, pointer, button);
        if (!event_handled) {
            if (button == Input.Buttons.LEFT) {
                if (0 <= screenX && screenX <= viewport.width && 0 <= screenY && screenY <= viewport.height) {
                    this.pointer_x = screenX;
                    this.pointer_y = screenY;
                    this.cursor_map_x = createCursorMapX(pointer_x);
                    this.cursor_map_y = createCursorMapY(pointer_y);
                    switch (mode) {
                        case MODE_ERASER:
                            doErase(cursor_map_x, cursor_map_y);
                            break;
                        case MODE_BRUSH:
                            doBrush(cursor_map_x, cursor_map_y);
                            break;
                        default:
                            //do nothing
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean event_handled = super.touchDragged(screenX, screenY, pointer);
        if (!event_handled) {
            if (mode == MODE_HAND) {
                int delta_x = pointer_x - screenX;
                int delta_y = pointer_y - screenY;
                dragViewport(delta_x, delta_y);
            }
            pointer_x = screenX;
            pointer_y = screenY;
            cursor_map_x = createCursorMapX(pointer_x);
            cursor_map_y = createCursorMapY(pointer_y);
            switch (mode) {
                case MODE_ERASER:
                    doErase(cursor_map_x, cursor_map_y);
                    break;
                case MODE_BRUSH:
                    doBrush(cursor_map_x, cursor_map_y);
                    break;
                default:
                    //do nothing
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        boolean event_handled = super.mouseMoved(screenX, screenY);
        if (!event_handled) {
            this.pointer_x = screenX;
            this.pointer_y = screenY;
            this.cursor_map_x = createCursorMapX(pointer_x);
            this.cursor_map_y = createCursorMapY(pointer_y);
        }
        return true;
    }

    private void doErase(int map_x, int map_y) {
        if (getMap().getUnit(map_x, map_y) == null) {
            getMap().setTile((short) 0, map_x, map_y);
        } else {
            getMap().removeUnit(map_x, map_y);
        }
    }

    private void doBrush(int map_x, int map_y) {
        if (mode == MODE_BRUSH) {
            switch (brush_type) {
                case TYPE_TILE:
                    if (TileFactory.getTile(selected_tile_index).getType() == Tile.TYPE_WATER) {
                        getMap().setTile(selected_tile_index, map_x, map_y);
                        TileValidator.validate(getMap(), map_x, map_y);
                    } else {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dx = -1; dx <= 1; dx++) {
                                TileValidator.validate(getMap(), map_x + dx, map_y + dy);
                            }
                        }
                    }
                    break;
                case TYPE_UNIT:
                    if (getMap().getUnit(map_x, map_y) == null) {
                        Unit unit = UnitFactory.cloneUnit(selected_unit);
                        unit.setX(map_x);
                        unit.setY(map_y);
                        unit.setTeam(selected_team);
                        getMap().addUnit(unit);
                    }
                    break;
                default:
                    //do nothing
            }
        }
    }

    public void locateViewport(int map_x, int map_y) {
        int center_sx = map_x * ts;
        int center_sy = map_y * ts;
        int map_width = getMap().getWidth() * ts;
        int map_height = getMap().getHeight() * ts;
        if (viewport.width < map_width) {
            viewport.x = center_sx - (viewport.width - ts) / 2;
            if (viewport.x < 0) {
                viewport.x = 0;
            }
            if (viewport.x > map_width - viewport.width) {
                viewport.x = map_width - viewport.width;
            }
        } else {
            viewport.x = (map_width - viewport.width) / 2;
        }
        if (viewport.height < map_height) {
            viewport.y = center_sy - (viewport.height - ts) / 2;
            if (viewport.y < 0) {
                viewport.y = 0;
            }
            if (viewport.y > map_height - viewport.height) {
                viewport.y = map_height - viewport.height;
            }
        } else {
            viewport.y = (map_height - viewport.height) / 2;
        }
    }

    public void dragViewport(int delta_x, int delta_y) {
        viewport.x += delta_x;
        viewport.y += delta_y;
//        int map_width = getMap().getWidth() * ts;
//        int map_height = getMap().getHeight() * ts;
//        if (viewport.width < map_width) {
//            if (-ts <= viewport.x + delta_x
//                    && viewport.x + delta_x <= map_width - viewport.width + ts) {
//                viewport.x += delta_x;
//            } else {
//                viewport.x = viewport.x + delta_x < -ts ? -ts : map_width - viewport.width + ts;
//            }
//        } else {
//            viewport.x = (map_width - viewport.width) / 2;
//        }
//        if (viewport.height < map_height) {
//            if (-ts <= viewport.y + delta_y
//                    && viewport.y + delta_y <= map_height - viewport.height + ts) {
//                viewport.y += delta_y;
//            } else {
//                viewport.y = viewport.y + delta_y < -ts ? -ts : map_height - viewport.height + ts;
//            }
//        } else {
//            viewport.y = (map_height - viewport.height) / 2;
//        }
    }

    public int getCursorMapX() {
        return cursor_map_x;
    }

    private int createCursorMapX(int pointer_x) {
        int map_width = getMap().getWidth();
        int cursor_x = (pointer_x + viewport.x) / ts;
        if (cursor_x >= map_width) {
            return map_width - 1;
        }
        if (cursor_x < 0) {
            return 0;
        }
        return cursor_x;
    }

    public int getCursorMapY() {
        return cursor_map_y;
    }

    private int createCursorMapY(int pointer_y) {
        int map_height = getMap().getHeight();
        int cursor_y = (pointer_y + viewport.y) / ts;
        if (cursor_y >= map_height) {
            return map_height - 1;
        }
        if (cursor_y < 0) {
            return 0;
        }
        return cursor_y;
    }

    @Override
    public int getXOnScreen(int map_x) {
        int sx = viewport.x / ts;
        sx = sx > 0 ? sx : 0;
        int x_offset = sx * ts - viewport.x;
        return (map_x - sx) * ts + x_offset;
    }

    @Override
    public int getYOnScreen(int map_y) {
        int screen_height = Gdx.graphics.getHeight();
        int sy = viewport.y / ts;
        sy = sy > 0 ? sy : 0;
        int y_offset = sy * ts - viewport.y;
        return screen_height - ((map_y - sy) * ts + y_offset) - ts;
    }

    private boolean isWithinPaintArea(int sx, int sy) {
        return -ts <= sx && sx <= viewport.width && -ts <= sy && sy <= viewport.height + ts;
    }

    public void createEmptyMap(int width, int height) {
        short[][] map_data = new short[width][height];
        boolean[] team_access = new boolean[4];
        this.map = new Map(map_data, team_access, "default");
    }

    public void setMap(Map map, String filename) {
        this.map = map;
        this.filename = filename;
        locateViewport(0, 0);
        setMode(MODE_BRUSH);
        setBrushType(TYPE_TILE);
    }

    public void saveMap(String filename, String author) {
        this.filename = filename;
        getMap().setAuthor(author);
        FileHandle map_file = FileProvider.getUserFile("map/" + filename + ".aem");
        try {
            if (map_file.exists()) {
                showOverwriteDialog();
            } else {
                MapFactory.createTeamAccess(map);
                if (map.getPlayerCount() >= 2) {
                    MapFactory.writeMap(map, map_file);
                    map_save_dialog.setVisible(false);
                } else {
                    showTeamErrorDialog();
                }
            }
        } catch (IOException ex) {
            map_save_dialog.setVisible(false);
            showSaveErrorDialog();
        }
    }

    private void showTeamErrorDialog() {
        dialog.getContentTable().reset();
        dialog.getContentTable().add(new Label(Language.getText("EDITOR_ERROR_1"), getContext().getSkin()));

        dialog.getButtonTable().reset();
        TextButton btn_ok = new TextButton("OK", getContext().getSkin());
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.setVisible(false);
            }
        });
        dialog.getButtonTable().add(btn_ok).size(ts * 3, ts / 2);

        dialog.setVisible(true);
    }

    private void showOverwriteDialog() {
        dialog.getContentTable().reset();
        dialog.getContentTable().add(new Label(Language.getText("LB_OVERWRITE") + "?", getContext().getSkin()));

        dialog.getButtonTable().reset();
        TextButton btn_overwrite = new TextButton(Language.getText("LB_OVERWRITE"), getContext().getSkin());
        btn_overwrite.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doOverwrite();
            }
        });
        dialog.getButtonTable().add(btn_overwrite).size(ts / 2 * 5, ts / 2);
        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.setVisible(false);
            }
        });
        dialog.getButtonTable().add(btn_cancel).size(ts / 2 * 5, ts / 2).padLeft(ts / 4);

        dialog.setVisible(true);
    }

    private void showSaveErrorDialog() {
        dialog.getContentTable().reset();
        dialog.getContentTable().add(new Label(Language.getText("EDITOR_ERROR_2"), getContext().getSkin()));

        dialog.getButtonTable().reset();
        TextButton btn_ok = new TextButton("OK", getContext().getSkin());
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.setVisible(false);
            }
        });
        dialog.getButtonTable().add(btn_ok).size(ts * 3, ts / 2);

        dialog.setVisible(true);
    }

    private void doOverwrite() {
        try {
            FileHandle map_file = FileProvider.getUserFile("map/" + filename + ".aem");
            MapFactory.writeMap(map, map_file);
        } catch (IOException ex) {
            showSaveErrorDialog();
        }
        map_save_dialog.setVisible(false);
    }

}
