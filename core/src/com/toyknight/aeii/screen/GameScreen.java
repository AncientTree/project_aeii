package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.animator.AttackCursorAnimator;
import com.toyknight.aeii.animator.CursorAnimator;
import com.toyknight.aeii.animator.UnitAnimator;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.entity.MapViewport;
import com.toyknight.aeii.renderer.AlphaRenderer;
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
    private final AlphaRenderer alpha_renderer;
    private final StatusBarRenderer status_bar_renderer;
    private final ShapeRenderer shape_renderer;
    private final GameManager manager;

    private final CursorAnimator cursor;
    private final AttackCursorAnimator attack_cursor;

    private final MapViewport viewport;

    private int pointer_x;
    private int pointer_y;
    private boolean dragged;

    private final TextField command_line;

    public GameScreen(AEIIApplication context) {
        this.context = context;
        this.ts = context.getTileSize();

        this.batch = new SpriteBatch();
        this.map_renderer = new MapRenderer(this, ts);
        this.unit_renderer = new UnitRenderer(this, ts);
        this.alpha_renderer = new AlphaRenderer(this, ts);
        this.status_bar_renderer = new StatusBarRenderer(this, ts);
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
        this.manager = new GameManager();

        this.cursor = new CursorAnimator(this, ts);
        this.attack_cursor = new AttackCursorAnimator(this, ts);

        this.viewport = new MapViewport();
        this.viewport.width = Gdx.graphics.getWidth();
        this.viewport.height = Gdx.graphics.getHeight() - ts;

        this.command_line = new TextField("", getContext().getSkin());
        initComponents();
    }

    private void initComponents() {
        this.command_line.setPosition(0, Gdx.graphics.getHeight() - command_line.getHeight());
        this.command_line.setWidth(Gdx.graphics.getWidth());
        this.command_line.setVisible(false);
        this.addActor(command_line);
    }

    public AEIIApplication getContext() {
        return context;
    }

    @Override
    public void draw() {
        map_renderer.drawMap(batch, getGame().getMap());
        if (manager.getCurrentAnimation() == null /*&& getGame().isLocalPlayer()*/) {
            switch (manager.getState()) {
                case GameManager.STATE_RMOVE:
                case GameManager.STATE_MOVE:
                    alpha_renderer.drawMoveAlpha(batch, manager.getMovablePositions());
                    //paintMovePath(g, ts);
                    break;
                case GameManager.STATE_PREVIEW:
                    alpha_renderer.drawMoveAlpha(batch, manager.getMovablePositions());
                    break;
                case GameManager.STATE_ATTACK:
                case GameManager.STATE_SUMMON:
                case GameManager.STATE_HEAL:
                    alpha_renderer.drawAttackAlpha(batch, manager.getAttackablePositions());
                    break;
                default:
                    //do nothing
            }
        }
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
        updateViewport();
        super.act(delta);
    }

    private void updateViewport() {
        int dx = 0;
        int dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy -= 8;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy += 8;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx -= 8;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx += 8;
        }
        dragViewport(dx, dy);
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
    public boolean keyDown(int keyCode) {
        boolean event_handled = super.keyDown(keyCode);
        if (!event_handled) {
            switch (keyCode) {
                case Input.Keys.GRAVE:
                    //show command line
                    break;
                default:
                    //do nothing
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchDown(screenX, screenY, pointer, button);
        if (!event_handled) {
            this.pointer_x = screenX;
            this.pointer_y = screenY;
        }
        return true;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchUp(screenX, screenY, pointer, button);
        if (!event_handled) {
            if (dragged == false) {
                onClick(screenX, screenY);
            }
            this.dragged = false;
        }
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean event_handled = super.touchDragged(screenX, screenY, pointer);
        if (!event_handled) {
            processDragEvent(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        boolean event_handled = super.mouseMoved(screenX, screenY);
        if (!event_handled) {
            this.pointer_x = screenX;
            this.pointer_y = screenY;
        }
        return true;
    }

    private void processDragEvent(int drag_x, int drag_y) {
        dragged = true;
        if (isOperatable()) {
            int delta_x = pointer_x - drag_x;
            int delta_y = pointer_y - drag_y;
            dragViewport(delta_x, delta_y);
            pointer_x = drag_x;
            pointer_y = drag_y;
        }
    }

    private void onClick(int screen_x, int screen_y) {
        if (isOperatable()) {
            int cursor_x = getCursorXOnMap();
            int cursor_y = getCursorYOnMap();
            manager.selectUnit(cursor_x, cursor_y);
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
        int cursor_x = (pointer_x + viewport.x) / ts;
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
        int cursor_y = (pointer_y + viewport.y) / ts;
        if (cursor_y >= map_height) {
            return map_height - 1;
        }
        if (cursor_y < 0) {
            return 0;
        }
        return cursor_y;
    }

    public int getXOnScreen(int map_x) {
        int sx = viewport.x / ts;
        sx = sx > 0 ? sx : 0;
        int x_offset = sx * ts - viewport.x;
        return (map_x - sx) * ts + x_offset;
    }

    public int getYOnScreen(int map_y) {
        int screen_height = Gdx.graphics.getHeight();
        int sy = viewport.y / ts;
        sy = sy > 0 ? sy : 0;
        int y_offset = sy * ts - viewport.y;
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
        int map_width = getGame().getMap().getWidth() * ts;
        int map_height = getGame().getMap().getHeight() * ts;
        if (viewport.width < map_width) {
            if (0 <= viewport.x + delta_x
                    && viewport.x + delta_x <= map_width - viewport.width) {
                viewport.x += delta_x;
            } else {
                viewport.x = viewport.x + delta_x < 0 ? 0 : map_width - viewport.width;
            }
        } else {
            viewport.x = (map_width - viewport.width) / 2;
        }
        if (viewport.height < map_height) {
            if (0 <= viewport.y + delta_y
                    && viewport.y + delta_y <= map_height - viewport.height) {
                viewport.y += delta_y;
            } else {
                viewport.y = viewport.y + delta_y < 0 ? 0 : map_height - viewport.height;
            }
        } else {
            viewport.y = (map_height - viewport.height) / 2;
        }
    }

    public int getViewportX() {
        return viewport.x;
    }

    public int getViewportY() {
        return viewport.y;
    }

}
