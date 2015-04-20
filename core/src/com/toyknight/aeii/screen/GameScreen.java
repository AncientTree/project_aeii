package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.animator.AttackCursorAnimator;
import com.toyknight.aeii.animator.CursorAnimator;
import com.toyknight.aeii.animator.UnitAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.renderer.MapRenderer;
import com.toyknight.aeii.renderer.StatusBarRenderer;
import com.toyknight.aeii.renderer.UnitRenderer;

import java.util.Set;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameScreen extends Stage implements Screen {

    private final int ts;
    private final AEIIApplication context;

    private final SpriteBatch batch;
    private final MapRenderer map_renderer;
    private final UnitRenderer unit_renderer;
    private final StatusBarRenderer status_bar_renderer;
    private final ShapeRenderer shape_renderer;
    private final GameManager manager;

    private final CursorAnimator cursor;
    private final AttackCursorAnimator attack_cursor;

    private int viewport_width;
    private int viewport_height;
    private int viewport_x;
    private int viewport_y;

    private int pointer_x;
    private int pointer_y;
    private boolean dragged;
    private int press_x;
    private int press_y;

    public GameScreen(AEIIApplication context) {
        this.context = context;
        this.ts = context.getTileSize();

        this.batch = new SpriteBatch();
        this.map_renderer = new MapRenderer(this, ts);
        this.unit_renderer = new UnitRenderer(this, ts);
        this.status_bar_renderer = new StatusBarRenderer(this, ts);
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
        this.manager = new GameManager();

        this.cursor = new CursorAnimator(this, ts);
        this.attack_cursor = new AttackCursorAnimator(this, ts);

        this.viewport_width = Gdx.graphics.getWidth();
        this.viewport_height = Gdx.graphics.getHeight() - ts;
    }

    @Override
    public void draw() {
        map_renderer.drawMap(batch, getGame().getMap());
        drawUnits();
        drawCursor();
        status_bar_renderer.drawStatusBar(batch, manager);

        super.draw();
    }

    private void drawUnits() {
        Set<Point> unit_positions = getGame().getMap().getUnitPositionSet();
        for (Point position : unit_positions) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            //if this unit isn't animating, then paint it. otherwise, let animation paint it
            if (!isOnUnitAnimation(unit.getX(), unit.getY())) {
                int unit_x = unit.getX();
                int unit_y = unit.getY();
                int sx = getXOnScreen(unit_x);
                int sy = getYOnScreen(unit_y);
                if (isWithinPaintArea(sx, sy)) {
                    unit_renderer.drawUnitWithInformation(batch, unit, unit_x, unit_y);
                }
            }
        }
    }

    private void drawCursor() {
        if (isOperatable()) {
            int cursor_x = getCursorXOnMap();
            int cursor_y = getCursorYOnMap();
            Unit selected_unit = manager.getSelectedUnit();
            switch (manager.getState()) {
                case GameManager.STATE_ATTACK:
                    if (getGame().canAttack(selected_unit, cursor_x, cursor_y)) {
                        attack_cursor.render(batch, cursor_x, cursor_y);
                    } else {
                        cursor.render(batch, cursor_x, cursor_y);
                    }
                    break;
                case GameManager.STATE_SUMMON:
                    if (getGame().canSummon(cursor_x, cursor_y)) {
                        attack_cursor.render(batch, cursor_x, cursor_y);
                    } else {
                        cursor.render(batch, cursor_x, cursor_y);
                    }
                    break;
                case GameManager.STATE_HEAL:
                    if (getGame().canHeal(selected_unit, cursor_x, cursor_y)) {
                        attack_cursor.render(batch, cursor_x, cursor_y);
                    } else {
                        cursor.render(batch, cursor_x, cursor_y);
                    }
                    break;
                default:
                    cursor.render(batch, cursor_x, cursor_y);
            }
        }
    }

    @Override
    public void act(float delta) {
        map_renderer.update(delta);
        unit_renderer.update(delta);
        cursor.addStateTime(delta);
        attack_cursor.addStateTime(delta);
        super.act(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchDown(screenX, screenY, pointer, button);
        this.press_x = screenX;
        this.press_y = screenY;
        return event_handled;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchUp(screenX, screenY, pointer, button);
        if (dragged == false) {
            onClick(screenX, screenY);
        }
        this.dragged = false;
        return event_handled;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean event_handled = super.touchDragged(screenX, screenY, pointer);
        processDragEvent(screenX, screenY);
        return event_handled;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        boolean event_handled = super.mouseMoved(screenX, screenY);
        this.pointer_x = screenX;
        this.pointer_y = screenY;
        return event_handled;
    }

    private void onClick(int screen_x, int screen_y) {
        if (isOperatable()) {
            pointer_x = screen_x;
            pointer_y = screen_y;
        }
    }

    private void processDragEvent(int drag_x, int drag_y) {
        dragged = true;
        if (isOperatable()) {
            int delta_x = press_x - drag_x;
            int delta_y = press_y - drag_y;
            dragViewport(delta_x, delta_y);
            press_x = drag_x;
            press_y = drag_y;
            pointer_x = drag_x;
            pointer_y = drag_y;
        }
    }

    private boolean isOnUnitAnimation(int x, int y) {
        Animator current_animation = manager.getCurrentAnimation();
        if (current_animation == null) {
            return false;
        } else {
            if (current_animation instanceof UnitAnimator) {
                return ((UnitAnimator) current_animation).hasLocation(x, y);
            } else {
                return false;
            }
        }
    }

    private boolean isOperatable() {
        return manager.getCurrentAnimation() == null;
    }

    public void setGame(GameCore game) {
        this.manager.setGame(game);
        this.locateViewport(0, 0);
        this.dragged = false;
        pointer_x = -1;
        pointer_y = -1;
    }

    public GameCore getGame() {
        return manager.getGame();
    }

    public int getCursorXOnMap() {
        int map_width = manager.getGame().getMap().getWidth();
        int cursor_x = (pointer_x + viewport_x) / ts;
        if (cursor_x >= map_width) {
            return map_width - 1;
        }
        if (cursor_x < 0) {
            return 0;
        }
        return cursor_x;
    }

    public int getCursorYOnMap() {
        int map_height = manager.getGame().getMap().getHeight();
        int cursor_y = (pointer_y + viewport_y) / ts;
        if (cursor_y >= map_height) {
            return map_height - 1;
        }
        if (cursor_y < 0) {
            return 0;
        }
        return cursor_y;
    }

    public int getXOnScreen(int map_x) {
        int sx = viewport_x / ts;
        sx = sx > 0 ? sx : 0;
        int x_offset = sx * ts - viewport_x;
        return (map_x - sx) * ts + x_offset;
    }

    public int getYOnScreen(int map_y) {
        int screen_height = Gdx.graphics.getHeight();
        int sy = viewport_y / ts;
        sy = sy > 0 ? sy : 0;
        int y_offset = sy * ts - viewport_y;
        return screen_height - ((map_y - sy) * ts + y_offset) - ts;
    }

    public boolean isWithinPaintArea(int sx, int sy) {
        return -ts <= sx && sx <= Gdx.graphics.getWidth() && -ts <= sy && sy <= Gdx.graphics.getHeight();
    }

    public void locateViewport(int map_x, int map_y) {
        int center_sx = map_x * ts;
        int center_sy = map_y * ts;
        int map_width = getGame().getMap().getWidth() * ts;
        int map_height = getGame().getMap().getHeight() * ts;
        if (viewport_width < map_width) {
            viewport_x = center_sx - (viewport_width - ts) / 2;
            if (viewport_x < 0) {
                viewport_x = 0;
            }
            if (viewport_x > map_width - viewport_width) {
                viewport_x = map_width - viewport_width;
            }
        } else {
            viewport_x = (map_width - viewport_width) / 2;
        }
        if (viewport_height < map_height) {
            viewport_y = center_sy - (viewport_height - ts) / 2;
            if (viewport_y < 0) {
                viewport_y = 0;
            }
            if (viewport_y > map_height - viewport_height) {
                viewport_y = map_height - viewport_height;
            }
        } else {
            viewport_y = (map_height - viewport_height) / 2;
        }
    }

    public void dragViewport(int delta_x, int delta_y) {
        int map_width = getGame().getMap().getWidth() * ts;
        int map_height = getGame().getMap().getHeight() * ts;
        if (viewport_width < map_width) {
            if (0 <= viewport_x + delta_x
                    && viewport_x + delta_x <= map_width - viewport_width) {
                viewport_x += delta_x;
            } else {
                viewport_x = viewport_x + delta_x < 0 ? 0 : map_width - viewport_width;
            }
        } else {
            viewport_x = (map_width - viewport_width) / 2;
        }
        if (viewport_height < map_height) {
            if (0 <= viewport_y + delta_y
                    && viewport_y + delta_y <= map_height - viewport_height) {
                viewport_y += delta_y;
            } else {
                viewport_y = viewport_y + delta_y < 0 ? 0 : map_height - viewport_height;
            }
        } else {
            viewport_y = (map_height - viewport_height) / 2;
        }
    }

    public int getViewportX() {
        return viewport_x;
    }

    public int getViewportY() {
        return viewport_y;
    }

}
