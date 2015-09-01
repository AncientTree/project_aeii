package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.SnapshotArray;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.screen.widgets.CircleButton;
import com.toyknight.aeii.utils.Platform;

/**
 * Created by toyknight on 4/26/2015.
 */
public class ActionButtonBar extends HorizontalGroup {

    private final int ts;
    private final GameScreen screen;
    private final int PADDING_LEFT;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;

    private CircleButton btn_buy;
    private CircleButton btn_standby;
    private CircleButton btn_attack;
    private CircleButton btn_move;
    private CircleButton btn_occupy;
    private CircleButton btn_repair;
    private CircleButton btn_summon;
    private CircleButton btn_heal;
    //private ImageButton btn_info;

    public ActionButtonBar(GameScreen screen) {
        this.screen = screen;
        this.ts = screen.getContext().getTileSize();
        this.PADDING_LEFT = screen.getContext().getTileSize() / 4;
        this.BUTTON_WIDTH = getPlatform() == Platform.Desktop ? ts / 24 * 20 : ts / 24 * 40;
        this.BUTTON_HEIGHT = getPlatform() == Platform.Desktop ? ts / 24 * 21 : ts / 24 * 42;
        initComponents();
    }

    private void initComponents() {

        btn_buy = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(0), ts);
        btn_buy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getGameManager().setState(GameManager.STATE_SELECT);
                screen.showUnitStore();
            }
        });
        btn_occupy = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(1), ts);
        btn_occupy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameHost.doOccupy();
                screen.onButtonUpdateRequested();
            }
        });
        btn_repair = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(1), ts);
        btn_repair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameHost.doRepair();
                screen.onButtonUpdateRequested();
            }
        });
        btn_attack = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(2), ts);
        btn_attack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getGameManager().beginAttackPhase();
                screen.onButtonUpdateRequested();
            }
        });
        btn_summon = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(3), ts);
        btn_summon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getGameManager().beginSummonPhase();
                screen.onButtonUpdateRequested();
            }
        });
        btn_move = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(4), ts);
        btn_move.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getGameManager().beginMovePhase();
                screen.onButtonUpdateRequested();
            }
        });
        btn_standby = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(5), ts);
        btn_standby.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameHost.doStandbyUnit();
                screen.onButtonUpdateRequested();
            }
        });
        btn_heal = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(7), ts);
    }

    private GameManager getGameManager() {
        return screen.getGameManager();
    }

    private Platform getPlatform() {
        return screen.getContext().getPlatform();
    }

    private GameCore getGame() {
        return getGameManager().getGame();
    }

    public int getButtonHeight() {
        return BUTTON_HEIGHT;
    }

    public void updateButtons() {
        this.clear();
        if (screen.canOperate()) {
            Unit selected_unit = getGameManager().getSelectedUnit();
            switch (getGameManager().getState()) {
                case GameManager.STATE_ACTION:
                    getGameManager().createAttackablePositions(selected_unit);
                    if (getGameManager().canSelectedUnitAct()) {
                        if (getGameManager().hasEnemyWithinRange(selected_unit)) {
                            addActor(btn_attack);
                        }
                        if (selected_unit.hasAbility(Ability.NECROMANCER)
                                && getGameManager().hasTombWithinRange(selected_unit)) {
                            addActor(btn_summon);
                        }
                        if (selected_unit.hasAbility(Ability.HEALER)
                                && getGameManager().hasAllyWithinRange(selected_unit)) {
                            addActor(btn_heal);
                        }
                        if (getGameManager().getGame().canOccupy(selected_unit, selected_unit.getX(), selected_unit.getY())) {
                            addActor(btn_occupy);
                        }
                        if (getGameManager().getGame().canRepair(selected_unit, selected_unit.getX(), selected_unit.getY())) {
                            addActor(btn_repair);
                        }
                    }
                    addActor(btn_standby);
                    break;
                case GameManager.STATE_BUY:
                    getGameManager().createAttackablePositions(selected_unit);
                    if (selected_unit.isCommander() && selected_unit.getTeam() == getGame().getCurrentTeam()
                            && getGame().isCastleAccessible(getGame().getMap().getTile(selected_unit.getX(), selected_unit.getY()))) {
                        addActor(btn_buy);
                        addActor(btn_move);
                        if (getGameManager().hasEnemyWithinRange(selected_unit)) {
                            addActor(btn_attack);
                        }
                    }
                    break;
                default:
                    //do nothing
            }
            this.layout();
        }
    }

    @Override
    public void layout() {
        SnapshotArray<Actor> children = getChildren();
        int btn_count = children.size;
        int margin_left = (screen.getViewportWidth() - btn_count * BUTTON_WIDTH - (btn_count + 1) * PADDING_LEFT) / 2;
        for (int i = 0; i < btn_count; i++) {
            children.get(i).setBounds(
                    margin_left + PADDING_LEFT + i * (BUTTON_WIDTH + PADDING_LEFT), 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    @Override
    public void draw(Batch batch, float parent_alpha) {
        super.draw(batch, parent_alpha);
    }

}
