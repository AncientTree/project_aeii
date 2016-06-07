package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.Callable;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.animation.*;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.network.NetworkManager;
import com.toyknight.aeii.record.GameRecordPlayerListener;
import com.toyknight.aeii.renderer.*;
import com.toyknight.aeii.screen.dialog.*;
import com.toyknight.aeii.screen.widgets.ActionButtonBar;
import com.toyknight.aeii.screen.widgets.CircleButton;
import com.toyknight.aeii.screen.widgets.MessageBoard;
import com.toyknight.aeii.screen.dialog.MessageBox;
import com.toyknight.aeii.network.entity.PlayerSnapshot;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.TileFactory;
import org.json.JSONObject;

/**
 * @author toyknight 4/4/2015.
 */
public class GameScreen extends StageScreen implements MapCanvas, GameRecordPlayerListener {

    private final int RIGHT_PANEL_WIDTH;

    private final TileRenderer tile_renderer;
    private final UnitRenderer unit_renderer;
    private final AlphaRenderer alpha_renderer;
    private final MovePathRenderer move_path_renderer;
    private final StatusBarRenderer status_bar_renderer;
    private final RightPanelRenderer right_panel_renderer;
    private final AttackInformationRenderer attack_info_renderer;

    private final CursorAnimator cursor;
    private final AttackCursorAnimator attack_cursor;

    private final MapViewport viewport;

    private float scale;

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

    private MiniMapDialog mini_map;
    private MessageBox message_box;
    private GameMenu menu;

    public GameScreen(GameContext context) {
        super(context);
        this.RIGHT_PANEL_WIDTH = 3 * ts;

        this.viewport = new MapViewport();
        this.viewport.width = Gdx.graphics.getWidth() - RIGHT_PANEL_WIDTH;
        this.viewport.height = Gdx.graphics.getHeight() - ts;

        this.tile_renderer = new TileRenderer(this);
        this.unit_renderer = new UnitRenderer(this);
        this.alpha_renderer = new AlphaRenderer(this);
        this.move_path_renderer = new MovePathRenderer(this);
        this.status_bar_renderer = new StatusBarRenderer(this, ts);
        this.right_panel_renderer = new RightPanelRenderer(this, ts);
        this.attack_info_renderer = new AttackInformationRenderer(this);

        this.cursor = new CursorAnimator();
        this.attack_cursor = new AttackCursorAnimator();

        initComponents();
    }

    private void initComponents() {
        this.btn_menu = new TextButton(Language.getText("LB_MENU"), getContext().getSkin());
        this.btn_menu.setBounds(Gdx.graphics.getWidth() - RIGHT_PANEL_WIDTH, Gdx.graphics.getHeight() - ts, RIGHT_PANEL_WIDTH, ts);
        this.btn_menu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialog("menu");
            }
        });
        this.addActor(btn_menu);
        this.btn_end_turn = new TextButton(Language.getText("LB_END_TURN"), getContext().getSkin());
        this.btn_end_turn.setBounds(Gdx.graphics.getWidth() - RIGHT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, ts);
        this.btn_end_turn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getGameManager().doEndTurn();
                update();
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
        this.message_board.setBounds(0, ts, viewport.width, viewport.height);
        addActor(message_board);

        //action button bar
        this.action_button_bar = new ActionButtonBar(this);
        this.action_button_bar.setPosition(0, ts + action_button_bar.getButtonHeight() / 4);
        this.addActor(action_button_bar);

        //game load dialog
        GameLoadDialog game_load_dialog = new GameLoadDialog(this);
        this.addDialog("load", game_load_dialog);

        //unit store
        UnitStoreDialog unit_store_dialog = new UnitStoreDialog(this);
        this.addDialog("store", unit_store_dialog);

        //game menu
        this.menu = new GameMenu(this);
        this.addDialog("menu", menu);

        this.message_box = new MessageBox(this);
        this.message_box.setPosition(
                viewport.width - message_box.getWidth() - (viewport.height - message_box.getHeight()) / 2,
                (viewport.height - message_box.getHeight()) / 2 + ts);
        this.addDialog("message", message_box);

        //mini map
        this.mini_map = new MiniMapDialog(this);
        this.mini_map.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog("map");
            }
        });
        this.addDialog("map", mini_map);
    }

    @Override
    public void draw() {
        batch.begin();
        drawMap();
        if (canOperate()) {
            switch (getGameManager().getState()) {
                case GameManager.STATE_REMOVE:
                case GameManager.STATE_MOVE:
                    alpha_renderer.drawMoveAlpha(batch, getGameManager().getMovablePositions());
                    move_path_renderer.drawMovePath(
                            batch, getGameManager().getMovePath(getCursorMapX(), getCursorMapY()));
                    break;
                case GameManager.STATE_PREVIEW:
                    alpha_renderer.drawMoveAlpha(batch, getGameManager().getMovablePositions());
                    break;
                case GameManager.STATE_ATTACK:
                case GameManager.STATE_SUMMON:
                case GameManager.STATE_HEAL:
                    alpha_renderer.drawAttackAlpha(batch, getGameManager().getAttackablePositions());
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
                        tile_renderer.drawTopTile(batch, top_tile_index, sx, sy + ts());
                    }
                }
            }
        }
    }

    private void drawTombs() {
        for (Tomb tomb : getGame().getMap().getTombs()) {
            int tomb_sx = getXOnScreen(tomb.x);
            int tomb_sy = getYOnScreen(tomb.y);
            batch.draw(ResourceManager.getTombTexture(), tomb_sx, tomb_sy, ts(), ts());
            batch.flush();
        }
    }

    private void drawUnits() {
        ObjectMap.Keys<Position> unit_positions = getGame().getMap().getUnitPositions();
        for (Position position : unit_positions) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            //if this unit isn't animating, then paint it. otherwise, let animation paint it
            if (!isOnUnitAnimation(unit.getX(), unit.getY())) {
                int unit_x = unit.getX();
                int unit_y = unit.getY();
                int sx = getXOnScreen(unit_x);
                int sy = getYOnScreen(unit_y);
                if (isWithinPaintArea(sx, sy)) {
                    getUnitRenderer().drawUnitWithInformation(batch, unit, unit_x, unit_y);
                }
            }
        }
    }

    private void drawCursor() {
        if (!getGameManager().isProcessing() && !getGameManager().isAnimating()) {
            int cursor_x = getCursorMapX();
            int cursor_y = getCursorMapY();
            Unit selected_unit = getGameManager().getSelectedUnit();
            switch (getGameManager().getState()) {
                case GameManager.STATE_ATTACK:
                    if (getGame().canAttack(selected_unit, cursor_x, cursor_y)) {
                        attack_cursor.render(batch);
                    } else {
                        cursor.render(batch, cursor_x, cursor_y);
                    }
                    break;
                case GameManager.STATE_SUMMON:
                    if (getGame().canSummon(selected_unit, cursor_x, cursor_y)) {
                        attack_cursor.render(batch);
                    } else {
                        cursor.render(batch, cursor_x, cursor_y);
                    }
                    break;
                case GameManager.STATE_HEAL:
                    if (getGame().canHeal(selected_unit, cursor_x, cursor_y)) {
                        attack_cursor.render(batch);
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
        Animator animator = getGameManager().getCurrentAnimation();
        if (animator != null) {
            animator.render(batch);
        }
    }

    @Override
    public void showDialog(String name) {
        super.showDialog(name);
        update();
    }

    @Override
    public void closeDialog(String name) {
        super.closeDialog(name);
        update();
    }

    @Override
    public void onDisconnect() {
        showPrompt(Language.getText("MSG_ERR_DFS"), new Callable() {
            @Override
            public void call() {
                getContext().gotoStatisticsScreen(getGame());
            }
        });
    }

    @Override
    public void onAllocationUpdate(int[] alliance, int[] allocation, int[] types) {
        super.onAllocationUpdate(alliance, allocation, types);
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (player.getType() == Player.LOCAL && allocation[team] != NetworkManager.getServiceID()) {
                player.setType(Player.REMOTE);
                message_board.appendMessage(null,
                        String.format(Language.getText("MSG_INFO_LTC"), Language.getText("LB_TEAM_" + team)));
            }
            if (player.getType() == Player.REMOTE && allocation[team] == NetworkManager.getServiceID()) {
                player.setType(Player.LOCAL);
                message_board.appendMessage(null,
                        String.format(Language.getText("MSG_INFO_GTC"), Language.getText("LB_TEAM_" + team)));
            }
        }
        update();
    }

    @Override
    public void onPlayerJoin(int id, String username) {
        super.onPlayerJoin(id, username);
        message_box.setPlayers(getContext().getRoomManager().getPlayers());
        appendMessage(null, String.format(Language.getText("MSG_INFO_PJ"), username));
    }

    @Override
    public void onPlayerLeave(int id, String username, int host) {
        super.onPlayerLeave(id, username, host);
        message_box.setPlayers(getContext().getRoomManager().getPlayers());
        appendMessage(null, String.format(Language.getText("MSG_INFO_PD"), username));
    }

    @Override
    public void onReceiveGameEvent(JSONObject event) {
        getGameManager().getGameEventExecutor().submitGameEvent(event);
    }

    @Override
    public void onReceiveMessage(String username, String message) {
        appendMessage(username, message);
    }

    @Override
    public void act(float delta) {
        message_board.update(delta);
        mini_map.update(delta);
        cursor.update(delta);
        attack_cursor.update(delta);
        tile_renderer.update(delta);
        unit_renderer.update(delta);
        updateViewport();
        super.act(delta);

        getContext().getGameManager().update(delta);
        getContext().getRecordPlayer().update(delta);
    }

    @Override
    public void show() {
        super.show();
        MapAnimator.setCanvas(this);

        scale = 1.0f;
        Position team_focus = getGame().getTeamFocus(getGame().getCurrentTeam());
        locateViewport(team_focus.x, team_focus.y);
        cursor_map_x = team_focus.x;
        cursor_map_y = team_focus.y;

        btn_message.setVisible(NetworkManager.isConnected());

        if (NetworkManager.isConnected()) {
            Array<PlayerSnapshot> players = new Array<PlayerSnapshot>(getContext().getRoomManager().getPlayers());
            message_box.setPlayers(players);
        } else {
            message_box.setPlayers(new Array<PlayerSnapshot>());
        }

        message_board.clearMessages();
        mini_map.setMap(getGame().getMap());
        mini_map.updateBounds(0, ts, viewport.width, viewport.height);
        closeAllDialogs();

        appendMessage(null, Language.getText("MSG_INFO_GS"));
        update();
    }

    @Override
    public boolean dialogKeyDown(int keyCode) {
        switch (keyCode) {
            case Input.Keys.ENTER:
                if (message_box.isVisible()) {
                    message_box.sendMessage();
                    return true;
                } else {
                    return false;
                }
            case Input.Keys.ESCAPE:
                if (isDialogShown()) {
                    closeTopDialog();
                    update();
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        boolean event_handled = super.keyDown(keyCode);
        if (event_handled) {
            return true;
        } else {
            if (keyCode == Input.Keys.BACK) {
                doCancel();
                return true;
            }
            if (keyCode == Input.Keys.ENTER) {
                showDialog("message");
            }
            switch (getGameManager().getState()) {
                case GameManager.STATE_BUY:
                    if (keyCode == Input.Keys.A && action_button_bar.isButtonAvailable("attack")) {
                        getGameManager().beginAttackPhase();
                        update();
                        return true;
                    }
                    if (keyCode == Input.Keys.B) {
                        getGameManager().setState(GameManager.STATE_SELECT);
                        showDialog("store");
                        return true;
                    }
                    if (keyCode == Input.Keys.M) {
                        getGameManager().beginMovePhase();
                        update();
                        return true;
                    }
                    return false;
                case GameManager.STATE_ACTION:
                    if (keyCode == Input.Keys.A && action_button_bar.isButtonAvailable("attack")) {
                        getGameManager().beginAttackPhase();
                        update();
                        return true;
                    }
                    if (keyCode == Input.Keys.O && action_button_bar.isButtonAvailable("occupy")) {
                        getGameManager().doOccupy();
                        update();
                        return true;
                    }
                    if (keyCode == Input.Keys.R && action_button_bar.isButtonAvailable("repair")) {
                        getGameManager().doRepair();
                        update();
                        return true;
                    }
                    if (keyCode == Input.Keys.S && action_button_bar.isButtonAvailable("summon")) {
                        getGameManager().beginSummonPhase();
                        update();
                        return true;
                    }
                    if (keyCode == Input.Keys.H && action_button_bar.isButtonAvailable("heal")) {
                        getGameManager().beginHealPhase();
                        update();
                        return true;
                    }
                    if (keyCode == Input.Keys.SPACE) {
                        getGameManager().doStandbySelectedUnit();
                        update();
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
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
            int target_x = createCursorMapX(screen_x);
            int target_y = createCursorMapY(screen_y);
            Position target = getGame().getMap().getPosition(target_x, target_y);
            if (press_map_x == target_x && press_map_y == target_y && drag_distance_x < ts && drag_distance_y < ts) {
                switch (getGameManager().getState()) {
                    case GameManager.STATE_MOVE:
                    case GameManager.STATE_REMOVE:
                        if (getGameManager().getMovablePositions().contains(target)) {
                            if (target_x == cursor_map_x && target_y == cursor_map_y) {
                                doClick(target_x, target_y);
                            } else {
                                cursor_map_x = target_x;
                                cursor_map_y = target_y;
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
                        if (getGameManager().getAttackablePositions().contains(target)) {
                            if (target_x == cursor_map_x && target_y == cursor_map_y) {
                                doClick(target_x, target_y);
                            } else {
                                cursor_map_x = target_x;
                                cursor_map_y = target_y;
                            }
                        } else {
                            getGameManager().cancelActionPhase();
                        }
                        break;
                    case GameManager.STATE_BUY:
                    case GameManager.STATE_ACTION:
                        doClick(target_x, target_y);
                        break;
                    default:
                        cursor_map_x = target_x;
                        cursor_map_y = target_y;
                        doClick(target_x, target_y);
                }
            }
        }
    }

    private void doClick(int cursor_x, int cursor_y) {
        if (canOperate()) {
            Unit selected_unit = getGameManager().getSelectedUnit();
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
                    getGameManager().doMove(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_ACTION:
                    getGameManager().doReverseMove();
                    break;
                case GameManager.STATE_ATTACK:
                    getGameManager().doAttack(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_SUMMON:
                    getGameManager().doSummon(cursor_x, cursor_y);
                    break;
                case GameManager.STATE_HEAL:
                    getGameManager().doHeal(cursor_x, cursor_y);
                    break;
                default:
                    //do nothing
            }
            update();
        }
    }

    private void doCancel() {
        if (canOperate()) {
            switch (getGameManager().getState()) {
                case GameManager.STATE_BUY:
                    getGameManager().setState(GameManager.STATE_SELECT);
                case GameManager.STATE_PREVIEW:
                    getGameManager().cancelPreviewPhase();
                    break;
                case GameManager.STATE_MOVE:
                    if (getGame().getMap().canStandby(getGameManager().getSelectedUnit())) {
                        getGameManager().cancelMovePhase();
                    }
                    break;
                case GameManager.STATE_ACTION:
                    getGameManager().doReverseMove();
                    break;
                case GameManager.STATE_ATTACK:
                case GameManager.STATE_SUMMON:
                case GameManager.STATE_HEAL:
                    getGameManager().cancelActionPhase();
                    break;
                default:
                    //do nothing
            }
            //update();
        }
    }

    private void onSelect(int map_x, int map_y) {
        Tile target_tile = getGame().getMap().getTile(map_x, map_y);
        Unit target_unit = getGame().getMap().getUnit(map_x, map_y);
        if (target_unit == null) {
            if (getGame().isCastleAccessible(target_tile)) {
                showDialog("store");
            }
        } else {
            if (getGame().isUnitAccessible(target_unit)) {
                if (getGame().isCastleAccessible(getGame().getMap().getTile(map_x, map_y))
                        && target_unit.isCommander() && target_unit.getTeam() == getGame().getCurrentTeam()) {
                    getGameManager().doSelect(map_x, map_y);
                } else {
                    getGameManager().doSelect(map_x, map_y);
                }
            } else {
                if (target_unit.isCommander() && target_unit.getTeam() == getGame().getCurrentTeam() &&
                        getGame().isCastleAccessible(target_tile)) {
                    showDialog("store");
                }
                if (target_unit.getTeam() != getGame().getCurrentTeam()) {
                    getGameManager().beginPreviewPhase(target_unit);
                }
            }
        }
    }

    public void update() {
        action_button_bar.updateButtons();
        btn_message.setVisible(NetworkManager.isConnected() && !message_box.isVisible());
        message_board.setFading(!message_box.isVisible());
        GameContext.setButtonEnabled(btn_end_turn, canEndTurn());
        GameContext.setButtonEnabled(btn_menu, !menu.isVisible());
    }

    public void appendMessage(String username, String message) {
        message_board.appendMessage(username, message);
    }

    private boolean isOnUnitAnimation(int x, int y) {
        if (getGameManager().isAnimating()) {
            Animator current_animation = getGameManager().getCurrentAnimation();
            return current_animation instanceof UnitAnimator && ((UnitAnimator) current_animation).hasLocation(x, y);
        } else {
            return false;
        }
    }

    public boolean canOperate() {
        return getGame().getCurrentPlayer().getType() == Player.LOCAL
                && !getGameManager().isProcessing()
                && !getGameManager().isAnimating();
    }

    public boolean canEndTurn() {
        int state = getGameManager().getState();
        int player_type = getGame().getCurrentPlayer().getType();
        return !getGameManager().isProcessing()
                && !getGameManager().isAnimating()
                && (player_type == Player.LOCAL || player_type == Player.NONE)
                && (state == GameManager.STATE_SELECT || state == GameManager.STATE_PREVIEW);
    }

    public GameManager getGameManager() {
        return getContext().getGameManager();
    }

    public GameCore getGame() {
        return getGameManager().getGame();
    }

    public int getRightPanelWidth() {
        return RIGHT_PANEL_WIDTH;
    }

    @Override
    public void onRecordPlaybackFinished() {
        appendMessage(null, Language.getText("MSG_INFO_RPF"));
    }

    @Override
    public Map getMap() {
        return getGame().getMap();
    }

    @Override
    public UnitRenderer getUnitRenderer() {
        return unit_renderer;
    }

    @Override
    public void focus(int map_x, int map_y) {
        cursor_map_x = map_x;
        cursor_map_y = map_y;
        //locateViewport(map_x, map_y);
    }

    @Override
    public int getCursorMapX() {
        return cursor_map_x;
    }

    private int createCursorMapX(int pointer_x) {
        int map_width = getGame().getMap().getWidth();
        int cursor_x = (pointer_x + viewport.x) / ts();
        if (cursor_x >= map_width) {
            return map_width - 1;
        }
        if (cursor_x < 0) {
            return 0;
        }
        return cursor_x;
    }

    @Override
    public int getCursorMapY() {
        return cursor_map_y;
    }

    private int createCursorMapY(int pointer_y) {
        int map_height = getGame().getMap().getHeight();
        int cursor_y = (pointer_y + viewport.y) / ts();
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
        int sx = viewport.x / ts();
        sx = sx > 0 ? sx : 0;
        int x_offset = sx * ts() - viewport.x;
        return (map_x - sx) * ts() + x_offset;
    }

    @Override
    public int getYOnScreen(int map_y) {
        int screen_height = Gdx.graphics.getHeight();
        int sy = viewport.y / ts();
        sy = sy > 0 ? sy : 0;
        int y_offset = sy * ts() - viewport.y;
        return screen_height - ((map_y - sy) * ts() + y_offset) - ts();
    }

    @Override
    public int ts() {
        return (int) (ts * scale);
    }

    @Override
    public boolean isWithinPaintArea(int sx, int sy) {
        return -ts() <= sx && sx <= viewport.width && -ts() <= sy && sy <= viewport.height + ts;
    }

    @Override
    public int getViewportWidth() {
        return viewport.width;
    }

    @Override
    public int getViewportHeight() {
        return viewport.height;
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
        int center_sx = map_x * ts();
        int center_sy = map_y * ts();
        int map_width = getGame().getMap().getWidth() * ts();
        int map_height = getGame().getMap().getHeight() * ts();
        if (viewport.width < map_width) {
            viewport.x = center_sx - (viewport.width - ts()) / 2;
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
            viewport.y = center_sy - (viewport.height - ts()) / 2;
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
        int map_width = getGame().getMap().getWidth() * ts();
        int map_height = getGame().getMap().getHeight() * ts();
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

}
