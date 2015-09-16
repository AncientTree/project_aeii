package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.animator.*;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.listener.GameManagerListener;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.renderer.*;
import com.toyknight.aeii.screen.dialog.*;
import com.toyknight.aeii.screen.widgets.ActionButtonBar;
import com.toyknight.aeii.screen.widgets.CircleButton;
import com.toyknight.aeii.screen.widgets.MessageBoard;
import com.toyknight.aeii.screen.widgets.MessageBox;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameScreen extends StageScreen implements MapCanvas, GameManagerListener {

    private final int RIGHT_PANEL_WIDTH;

    private final TileRenderer tile_renderer;
    private final UnitRenderer unit_renderer;
    private final AlphaRenderer alpha_renderer;
    private final MovePathRenderer move_path_renderer;
    private final StatusBarRenderer status_bar_renderer;
    private final RightPanelRenderer right_panel_renderer;
    private final AttackInformationRenderer attack_info_renderer;

    private final GameManager manager;

    private final CursorAnimator cursor;
    private final AttackCursorAnimator attack_cursor;

    private final MapViewport viewport;

    private int pointer_x;
    private int pointer_y;
    private int cursor_map_x;
    private int cursor_map_y;
    private int press_map_x;
    private int press_map_y;
    private int drag_distance_x;
    private int drag_distance_y;

    private TextButton btn_menu;
    private TextButton btn_end_turn;
    private CircleButton btn_message;
    private ActionButtonBar action_button_bar;

    private MessageBoard message_board;

    private SaveLoadDialog save_load_dialog;
    private UnitStoreDialog unit_store;
    private MessageBox message_box;
    private MiniMap mini_map;
    private GameMenu menu;

    public GameScreen(AEIIApplication context) {
        super(context);
        this.RIGHT_PANEL_WIDTH = 3 * ts;

        this.viewport = new MapViewport();
        this.viewport.width = Gdx.graphics.getWidth() - RIGHT_PANEL_WIDTH;
        this.viewport.height = Gdx.graphics.getHeight() - ts;

        this.tile_renderer = new TileRenderer(ts);
        this.unit_renderer = new UnitRenderer(this, ts);
        this.alpha_renderer = new AlphaRenderer(this, ts);
        this.move_path_renderer = new MovePathRenderer(this, ts);
        this.status_bar_renderer = new StatusBarRenderer(this, ts);
        this.right_panel_renderer = new RightPanelRenderer(this, ts);
        this.attack_info_renderer = new AttackInformationRenderer(this);

        this.cursor = new CursorAnimator(this, ts);
        this.attack_cursor = new AttackCursorAnimator(this, ts);

        this.manager = new GameManager();
        GameHost.setGameManager(manager);
        initComponents();
    }

    private void initComponents() {
        this.btn_menu = new TextButton(Language.getText("LB_MENU"), getContext().getSkin());
        this.btn_menu.setBounds(Gdx.graphics.getWidth() - RIGHT_PANEL_WIDTH, Gdx.graphics.getHeight() - ts, RIGHT_PANEL_WIDTH, ts);
        this.btn_menu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeAllWindows();
                menu.display();
                onButtonUpdateRequested();
            }
        });
        this.addActor(btn_menu);
        this.btn_end_turn = new TextButton(Language.getText("LB_END_TURN"), getContext().getSkin());
        this.btn_end_turn.setBounds(Gdx.graphics.getWidth() - RIGHT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, ts);
        this.btn_end_turn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameHost.doEndTurn();
                onButtonUpdateRequested();
            }
        });
        this.addActor(btn_end_turn);

        this.btn_message = new CircleButton(CircleButton.LARGE, ResourceManager.getMenuIcon(7), ts);
        this.btn_message.setPosition(0, Gdx.graphics.getHeight() - btn_message.getPrefHeight());
        this.btn_message.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                message_board.display();
                showDialog("message");
            }
        });
        this.addActor(btn_message);

        //message board
        this.message_board = new MessageBoard(ts);
        this.message_board.setBounds(0, ts, viewport.width, ts * 3);
        addActor(message_board);

        //action button bar
        this.action_button_bar = new ActionButtonBar(this);
        this.action_button_bar.setPosition(0, ts + action_button_bar.getButtonHeight() / 4);
        this.addActor(action_button_bar);

        //save load dialog
        this.save_load_dialog =
                new SaveLoadDialog(getContext(), new Rectangle(0, ts, getViewportWidth(), getViewportHeight()));
        this.addActor(save_load_dialog);
        this.save_load_dialog.setVisible(false);

        //unit store
        this.unit_store = new UnitStoreDialog(this, getContext().getSkin());
        this.addActor(unit_store);
        this.unit_store.setVisible(false);

        //game menu
        this.menu = new GameMenu(this);
        this.addActor(menu);
        this.menu.setVisible(false);

        this.message_box = new MessageBox(getContext());
        this.message_box.setBounds((viewport.width - ts * 8) / 2, (viewport.height - ts * 8) / 2 + ts, ts * 8, ts * 8);
        this.message_box.setCallback(new DialogCallback() {
            @Override
            public void doCallback() {
                closeDialog("message");
            }
        });
        addDialog("message", message_box);

        //mini map
        this.mini_map = new MiniMap(this);
        this.addActor(mini_map);
        this.mini_map.setVisible(false);
    }

    @Override
    public void draw() {
        batch.begin();
        drawMap();
        if (!manager.isAnimating() && getGame().getCurrentPlayer().isLocalPlayer()) {
            switch (manager.getState()) {
                case GameManager.STATE_REMOVE:
                case GameManager.STATE_MOVE:
                    alpha_renderer.drawMoveAlpha(batch, manager.getMovablePositions());
                    move_path_renderer.drawMovePath(batch, manager.getMovePath(getCursorMapX(), getCursorMapY()));
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
        drawTombs();
        drawUnits();
        drawCursor();
        drawAnimation();
        attack_info_renderer.render(batch);
        status_bar_renderer.drawStatusBar(batch);
        right_panel_renderer.drawStatusBar(batch);
        batch.end();
        super.draw();
    }

    private void drawMap() {
        for (int x = 0; x < getGame().getMap().getWidth(); x++) {
            for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                int sx = getXOnScreen(x);
                int sy = getYOnScreen(y);
                if (isWithinPaintArea(sx, sy)) {
                    int index = getGame().getMap().getTileIndex(x, y);
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

    private void drawTombs() {
        ArrayList<Tomb> tomb_list = getGame().getMap().getTombList();
        for (Tomb tomb : tomb_list) {
            int tomb_sx = getXOnScreen(tomb.x);
            int tomb_sy = getYOnScreen(tomb.y);
            batch.draw(ResourceManager.getTombTexture(), tomb_sx, tomb_sy, ts, ts);
            batch.flush();
        }
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
        if (!getGameManager().isAnimating()) {
            int cursor_x = getCursorMapX();
            int cursor_y = getCursorMapY();
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

    private void drawAnimation() {
        if (manager.isAnimating()) {
            Animator animator = manager.getCurrentAnimation();
            if (animator instanceof MapAnimator) {
                ((MapAnimator) animator).render(batch, this);
            }
            if (animator instanceof ScreenAnimator) {
                ((ScreenAnimator) animator).render(batch);
            }
        }
    }

    @Override
    public void onPlayerJoin(String service_name, String username) {
        message_board.appendMessage(null, username + " " + Language.getText("LB_JOINS"));
    }

    @Override
    public void onPlayerLeave(String service_name, String username) {
        if (getContext().isGameHost()) {
            String[] team_allocation = getContext().getRoomConfig().team_allocation;
            for (int team = 0; team < 4; team++) {
                if (team_allocation[team].equals(service_name) && getGame().getPlayer(team) != null) {
                    getGame().getPlayer(team).setType(Player.LOCAL);
                }
            }
            onButtonUpdateRequested();
            message_board.appendMessage(null, username + " " + Language.getText("LB_DISCONNECTED"));
        } else {
            if (getContext().getHostService().equals(service_name)) {
                getContext().showMessage(Language.getText("MSG_ERR_HPD"), new DialogCallback() {
                    @Override
                    public void doCallback() {
                        getContext().getNetworkManager().disconnect();
                        getContext().gotoMainMenuScreen();
                    }
                });
            } else {
                message_board.appendMessage(null, username + " " + Language.getText("LB_DISCONNECTED"));
            }
        }
    }

    @Override
    public void onReceiveGameEvent(GameEvent event) {
        getGameManager().queueGameEvent(event);
    }

    @Override
    public void onReceiveMessage(String username, String message) {
        message_board.appendMessage(username, Language.getText(message));
    }

    @Override
    public void act(float delta) {
        if (!message_box.isVisible()) {
            message_board.update(delta);
        }
        mini_map.update(delta);
        cursor.addStateTime(delta);
        tile_renderer.update(delta);
        unit_renderer.update(delta);
        attack_cursor.addStateTime(delta);
        updateViewport();

        super.act(delta);
        manager.updateAnimation(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        if (getContext().getNetworkManager().isConnected()) {
            btn_message.setVisible(true);
            getContext().getNetworkManager().setNetworkListener(this);
        } else {
            btn_message.setVisible(false);
        }
        onButtonUpdateRequested();
        message_board.clearMessages();
        mini_map.updateBounds();
        unit_store.setVisible(false);
        mini_map.setVisible(false);
        menu.setVisible(false);

        message_board.appendMessage(null, Language.getText("MSG_INFO_GS"));
    }

    public void prepare(GameCore game) {
        this.manager.setGame(game);
        this.manager.setGameManagerListener(this);
        GameHost.setGameManager(getGameManager());
        UnitToolkit.setGame(game);
        Point team_focus = getGame().getTeamFocus(getGame().getCurrentTeam());
        this.locateViewport(team_focus.x, team_focus.y);
        cursor_map_x = team_focus.x;
        cursor_map_y = team_focus.y;
    }

    @Override
    public boolean keyDown(int keyCode) {
        boolean event_handled = super.keyDown(keyCode);
        if (!event_handled) {
            if (keyCode == Input.Keys.BACK) {
                doCancel();
                return true;
            }
            switch (getGameManager().getState()) {
                case GameManager.STATE_BUY:
                    if (keyCode == Input.Keys.B) {
                        getGameManager().setState(GameManager.STATE_SELECT);
                        showUnitStore();
                    }
                    if (keyCode == Input.Keys.M) {
                        getGameManager().beginMovePhase();
                        onButtonUpdateRequested();
                    }
                    return true;
                case GameManager.STATE_ACTION:
                    if (keyCode == Input.Keys.A && action_button_bar.isButtonAvailable("attack")) {
                        getGameManager().beginAttackPhase();
                        onButtonUpdateRequested();
                    }
                    if (keyCode == Input.Keys.O && action_button_bar.isButtonAvailable("occupy")) {
                        GameHost.doOccupy();
                        onButtonUpdateRequested();
                    }
                    if (keyCode == Input.Keys.R && action_button_bar.isButtonAvailable("repair")) {
                        GameHost.doRepair();
                        onButtonUpdateRequested();
                    }
                    if (keyCode == Input.Keys.S && action_button_bar.isButtonAvailable("summon")) {
                        getGameManager().beginSummonPhase();
                        onButtonUpdateRequested();
                    }
                    if (keyCode == Input.Keys.H && action_button_bar.isButtonAvailable("heal")) {
                        getGameManager().beginHealPhase();
                        onButtonUpdateRequested();
                    }
                    if (keyCode == Input.Keys.SPACE) {
                        GameHost.doStandbyUnit();
                        onButtonUpdateRequested();
                    }
                    return true;
                default:
                    return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchDown(screenX, screenY, pointer, button);
        if (!event_handled) {
            this.pointer_x = screenX;
            this.pointer_y = screenY;
            if (canOperate()) {
                this.press_map_x = createCursorMapX(screenX);
                this.press_map_y = createCursorMapY(screenY);
                this.drag_distance_x = 0;
                this.drag_distance_y = 0;
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        boolean event_handled = super.touchUp(screenX, screenY, pointer, button);
        if (!event_handled && canOperate()) {
            if (button == Input.Buttons.LEFT) {
                onClick(screenX, screenY);
            }
            if (button == Input.Buttons.RIGHT) {
                doCancel();
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean event_handled = super.touchDragged(screenX, screenY, pointer);
        if (!event_handled) {
            onDrag(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        boolean event_handled = super.mouseMoved(screenX, screenY);
        if (!event_handled && getGameManager().getState() != GameManager.STATE_BUY) {
            if (canOperate() && getGameManager().getState() != GameManager.STATE_ACTION &&
                    0 <= screenX && screenX <= viewport.width && 0 <= screenY && screenY <= viewport.height) {
                this.pointer_x = screenX;
                this.pointer_y = screenY;
                this.cursor_map_x = createCursorMapX(pointer_x);
                this.cursor_map_y = createCursorMapY(pointer_y);
            }
        }
        return true;
    }

    private void onDrag(int drag_x, int drag_y) {
        int delta_x = pointer_x - drag_x;
        int delta_y = pointer_y - drag_y;
        this.drag_distance_x += Math.abs(delta_x);
        this.drag_distance_y += Math.abs(delta_y);
        dragViewport(delta_x, delta_y);
        pointer_x = drag_x;
        pointer_y = drag_y;
    }

    private void onClick(int screen_x, int screen_y) {
        if (0 <= screen_x && screen_x <= viewport.width && 0 <= screen_y && screen_y <= viewport.height) {
            int release_map_x = createCursorMapX(screen_x);
            int release_map_y = createCursorMapY(screen_y);
            if (press_map_x == release_map_x && press_map_y == release_map_y && drag_distance_x < ts && drag_distance_y < ts) {
                switch (getGameManager().getState()) {
                    case GameManager.STATE_MOVE:
                    case GameManager.STATE_REMOVE:
                        if (getGameManager().isMovablePosition(release_map_x, release_map_y)) {
                            if (release_map_x == cursor_map_x && release_map_y == cursor_map_y) {
                                doClick(release_map_x, release_map_y);
                            } else {
                                cursor_map_x = release_map_x;
                                cursor_map_y = release_map_y;
                            }
                        } else {
                            if (getGameManager().getState() == GameManager.STATE_MOVE
                                    && getGame().getMap().canStandby(getGameManager().getSelectedUnit())) {
                                getGameManager().cancelMovePhase();
                            }
                        }
                        break;
                    case GameManager.STATE_ATTACK:
                    case GameManager.STATE_SUMMON:
                    case GameManager.STATE_HEAL:
                        if (getGameManager().getAttackablePositions().contains(new Point(release_map_x, release_map_y))) {
                            if (release_map_x == cursor_map_x && release_map_y == cursor_map_y) {
                                doClick(release_map_x, release_map_y);
                            } else {
                                cursor_map_x = release_map_x;
                                cursor_map_y = release_map_y;
                            }
                        } else {
                            getGameManager().cancelActionPhase();
                        }
                        break;
                    case GameManager.STATE_BUY:
                    case GameManager.STATE_ACTION:
                        doClick(release_map_x, release_map_y);
                        break;
                    default:
                        cursor_map_x = release_map_x;
                        cursor_map_y = release_map_y;
                        doClick(release_map_x, release_map_y);
                }
            }
        }
    }

    private void doClick(int cursor_x, int cursor_y) {
        if (canOperate()) {
            Unit selected_unit = manager.getSelectedUnit();
            switch (getGameManager().getState()) {
                case GameManager.STATE_BUY:
                    getGameManager().setState(GameManager.STATE_SELECT);
                    break;
                case GameManager.STATE_PREVIEW:
                    if (selected_unit != null && !selected_unit.isAt(cursor_x, cursor_y)) {
                        getGameManager().cancelPreviewPhase();
                    }
                case GameManager.STATE_SELECT:
                    onSelect(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_MOVE:
                case GameManager.STATE_REMOVE:
                    GameHost.doMoveUnit(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_ACTION:
                    GameHost.doReverseMove();
                    break;
                case GameManager.STATE_ATTACK:
                    GameHost.doAttack(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_SUMMON:
                    GameHost.doSummon(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_HEAL:
                    GameHost.doHeal(cursor_x, cursor_y);
                    break;
                default:
                    //do nothing
            }
            onButtonUpdateRequested();
        }
    }

    private void doCancel() {
        if (canOperate()) {
            switch (manager.getState()) {
                case GameManager.STATE_BUY:
                    getGameManager().setState(GameManager.STATE_SELECT);
                case GameManager.STATE_PREVIEW:
                    manager.cancelPreviewPhase();
                    break;
                case GameManager.STATE_MOVE:
                    if (getGame().getMap().canStandby(getGameManager().getSelectedUnit())) {
                        manager.cancelMovePhase();
                    }
                    break;
                case GameManager.STATE_ACTION:
                    GameHost.doReverseMove();
                    break;
                case GameManager.STATE_ATTACK:
                case GameManager.STATE_SUMMON:
                case GameManager.STATE_HEAL:
                    manager.cancelActionPhase();
                    break;
                default:
                    //do nothing
            }
            //onButtonUpdateRequested();
        }
    }

    private void onSelect(int map_x, int map_y) {
        Tile target_tile = getGame().getMap().getTile(map_x, map_y);
        Unit target_unit = getGame().getMap().getUnit(map_x, map_y);
        if (target_unit == null) {
            if (getGame().isCastleAccessible(target_tile)) {
                showUnitStore();
            }
        } else {
            if (getGame().isUnitAccessible(target_unit)) {
                if (getGame().isCastleAccessible(getGame().getMap().getTile(map_x, map_y))
                        && target_unit.isCommander() && target_unit.getTeam() == getGame().getCurrentTeam()) {
                    GameHost.doSelect(map_x, map_y);
                } else {
                    GameHost.doSelect(map_x, map_y);
                }
            } else {
                if (target_unit.isCommander() && target_unit.getTeam() == getGame().getCurrentTeam() &&
                        getGame().isCastleAccessible(target_tile)) {
                    showUnitStore();
                }
                if (target_unit.getTeam() != getGame().getCurrentTeam()) {
                    getGameManager().beginPreviewPhase(target_unit);
                }
            }
        }
    }

    public void showUnitStore() {
        closeAllWindows();
        unit_store.display(cursor_map_x, cursor_map_y);
        onButtonUpdateRequested();
    }

    public void showMiniMap() {
        closeAllWindows();
        mini_map.setVisible(true);
        onButtonUpdateRequested();
    }

    public void showSaveDialog() {
        closeAllWindows();
        save_load_dialog.display(SaveLoadDialog.MODE_SAVE);
        onButtonUpdateRequested();
    }

    public void showLoadDialog() {
        closeAllWindows();
        save_load_dialog.display(SaveLoadDialog.MODE_LOAD);
        onButtonUpdateRequested();
    }

    public boolean isWindowOpened() {
        return save_load_dialog.isVisible() || unit_store.isVisible() || mini_map.isVisible() || menu.isVisible();
    }

    public void closeAllWindows() {
        save_load_dialog.setVisible(false);
        unit_store.setVisible(false);
        mini_map.setVisible(false);
        menu.setVisible(false);
        onButtonUpdateRequested();
    }

    @Override
    public void onMapFocusRequired(int map_x, int map_y) {
        cursor_map_x = map_x;
        cursor_map_y = map_y;
        //locateViewport(map_x, map_y);
    }

    @Override
    public void onManagerStateChanged(int last_state) {
        onButtonUpdateRequested();
    }

    @Override
    public void onButtonUpdateRequested() {
        int state = getGameManager().getState();
        this.action_button_bar.updateButtons();
        AEIIApplication.setButtonEnabled(btn_end_turn,
                canOperate() && (state == GameManager.STATE_SELECT || state == GameManager.STATE_PREVIEW));
        AEIIApplication.setButtonEnabled(btn_menu, !menu.isVisible());
    }

    @Override
    public void onGameOver() {
        //for test
        getContext().gotoMainMenuScreen();
        getContext().getNetworkManager().disconnect();
    }

    private boolean isOnUnitAnimation(int x, int y) {
        if (manager.isAnimating()) {
            Animator current_animation = manager.getCurrentAnimation();
            return current_animation instanceof UnitAnimator && ((UnitAnimator) current_animation).hasLocation(x, y);
        } else {
            return false;
        }
    }

    public boolean canOperate() {
        return getGameManager().getCurrentAnimation() == null &&
                !save_load_dialog.isVisible() &&
                !unit_store.isVisible() &&
                !mini_map.isVisible() &&
                !menu.isVisible() &&
                getGame().getCurrentPlayer().isLocalPlayer();
    }

    public GameCore getGame() {
        return manager.getGame();
    }

    public GameManager getGameManager() {
        return manager;
    }

    public UnitRenderer getUnitRenderer() {
        return unit_renderer;
    }

    public int getRightPanelWidth() {
        return RIGHT_PANEL_WIDTH;
    }

    public int getCursorMapX() {
        return cursor_map_x;
    }

    private int createCursorMapX(int pointer_x) {
        int map_width = getGame().getMap().getWidth();
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
        int map_height = getGame().getMap().getHeight();
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
        return -ts <= sx && sx <= viewport.width && -ts <= sy && sy <= viewport.height + ts;
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
        if (dx != 0 || dy != 0) {
            this.cursor_map_x = createCursorMapX(pointer_x);
            this.cursor_map_y = createCursorMapY(pointer_y);
        }
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
        if (viewport.width < map_width + ts * 2) {
            if (-ts <= viewport.x + delta_x
                    && viewport.x + delta_x <= map_width - viewport.width + ts) {
                viewport.x += delta_x;
            } else {
                viewport.x = viewport.x + delta_x < -ts ? -ts : map_width - viewport.width + ts;
            }
        } else {
            viewport.x = (map_width - viewport.width) / 2;
        }
        if (viewport.height < map_height + ts * 2) {
            if (-ts <= viewport.y + delta_y
                    && viewport.y + delta_y <= map_height - viewport.height + ts) {
                viewport.y += delta_y;
            } else {
                viewport.y = viewport.y + delta_y < -ts ? -ts : map_height - viewport.height + ts;
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

    public int getViewportWidth() {
        return viewport.width;
    }

    public int getViewportHeight() {
        return viewport.height;
    }

}
