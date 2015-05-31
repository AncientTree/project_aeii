package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.manager.LocalGameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.screen.widgets.CircleButton;

/**
 * Created by toyknight on 4/26/2015.
 */
public class ActionButtonBar extends HorizontalGroup {

    private final GameScreen screen;
    private final LocalGameManager manager;
    private final int PADDING_LEFT;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;
    private final ShapeRenderer shape_renderer;

    private CircleButton btn_buy;
    private CircleButton btn_standby;
    private CircleButton btn_attack;
    private CircleButton btn_move;
    private CircleButton btn_occupy;
    private CircleButton btn_repair;
    private CircleButton btn_summon;
    private CircleButton btn_heal;
    //private ImageButton btn_info;

    public ActionButtonBar(GameScreen screen, LocalGameManager manager) {
        this.screen = screen;
        this.manager = manager;
        this.PADDING_LEFT = screen.getContext().getTileSize() / 4;
        this.BUTTON_WIDTH = screen.getContext().getTileSize() / 24 * 20;
        this.BUTTON_HEIGHT = screen.getContext().getTileSize() / 24 * 21;
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
        initComponents();
    }

    private void initComponents() {
        int ts = screen.getContext().getTileSize();
        btn_buy = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(0), ts);
        btn_occupy = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(1), ts);
        btn_occupy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.doOccupy();
                screen.onButtonUpdateRequested();
            }
        });
        btn_repair = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(1), ts);
        btn_repair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.doRepair();
                screen.onButtonUpdateRequested();
            }
        });
        btn_attack = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(2), ts);
        btn_attack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.beginAttackPhase();
                screen.onButtonUpdateRequested();
            }
        });
        btn_summon = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(3), ts);
        btn_summon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.beginSummonPhase();
                screen.onButtonUpdateRequested();
            }
        });
        btn_move = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(4), ts);
        btn_standby = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(5), ts);
        btn_standby.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.standbySelectedUnit();
                screen.onButtonUpdateRequested();
            }
        });
        btn_heal = new CircleButton(CircleButton.SMALL, ResourceManager.getActionIcon(7), ts);
    }

    public void updateButtons() {
        this.clear();
        if (manager.getGame().getCurrentPlayer() instanceof LocalPlayer && !manager.isAnimating()) {
            Unit selected_unit = manager.getSelectedUnit();
            switch (manager.getState()) {
                case LocalGameManager.STATE_ACTION:
                    if (manager.canSelectUnitAct()) {
                        addActor(btn_attack);
                        if (selected_unit.hasAbility(Ability.NECROMANCER)) {
                            addActor(btn_summon);
                        }
                        if (selected_unit.hasAbility(Ability.HEALER)) {
                            addActor(btn_heal);
                        }
                        if (manager.getGame().canOccupy(selected_unit, selected_unit.getX(), selected_unit.getY())) {
                            addActor(btn_occupy);
                        }
                        if (manager.getGame().canRepair(selected_unit, selected_unit.getX(), selected_unit.getY())) {
                            addActor(btn_repair);
                        }
                    }
                    addActor(btn_standby);
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
        for (int i = 0; i < children.size; i++) {
            children.get(i).setBounds(
                    PADDING_LEFT + i * (BUTTON_WIDTH + PADDING_LEFT), 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    @Override
    public void draw(Batch batch, float parent_alpha) {
        super.draw(batch, parent_alpha);
    }

}
