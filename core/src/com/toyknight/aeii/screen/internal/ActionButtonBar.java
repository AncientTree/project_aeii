package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/26/2015.
 */
public class ActionButtonBar extends HorizontalGroup {

    private final GameScreen screen;
    private final GameManager manager;
    private final int PADDING_LEFT;
    private final ShapeRenderer shape_renderer;

    private ImageButton btn_buy;
    private ImageButton btn_standby;
    private ImageButton btn_attack;
    private ImageButton btn_move;
    private ImageButton btn_occupy;
    private ImageButton btn_repair;
    private ImageButton btn_summon;
    private ImageButton btn_heal;
    private ImageButton btn_info;

    public ActionButtonBar(GameScreen screen, GameManager manager) {
        this.screen = screen;
        this.manager = manager;
        this.PADDING_LEFT = screen.getContext().getTileSize() / 4;
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
        initComponents();
    }

    private void initComponents() {
        int ts = screen.getContext().getTileSize();
        TextureRegionDrawable[][] button_icons = new TextureRegionDrawable[9][3];
        for (int index = 0; index < 9; index++) {
            Texture button_texture = ResourceManager.getActionButtonTexture(index);
            int width = button_texture.getWidth() / 3;
            int height = button_texture.getHeight();
            for (int state = 0; state < 3; state++) {
                TextureRegion button_icon =
                        new TextureRegion(ResourceManager.getActionButtonTexture(index), width * state, 0, width, height);
                button_icons[index][state] = new TextureRegionDrawable(button_icon);
                button_icons[index][state].setMinWidth(ts / 24 * 20);
                button_icons[index][state].setMinHeight(ts / 24 * 21);
            }
        }
        btn_buy = new ImageButton(button_icons[0][0], button_icons[0][2], button_icons[0][1]);
        btn_buy.padLeft(PADDING_LEFT);
        btn_occupy = new ImageButton(button_icons[1][0], button_icons[1][2], button_icons[1][1]);
        btn_occupy.padLeft(PADDING_LEFT);
        btn_repair = new ImageButton(button_icons[1][0], button_icons[1][2], button_icons[1][1]);
        btn_repair.padLeft(PADDING_LEFT);
        btn_attack = new ImageButton(button_icons[2][0], button_icons[2][2], button_icons[2][1]);
        btn_attack.padLeft(PADDING_LEFT);
        btn_attack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.beginAttackPhase();
            }
        });
        btn_summon = new ImageButton(button_icons[3][0], button_icons[3][2], button_icons[3][1]);
        btn_summon.padLeft(PADDING_LEFT);
        btn_summon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.beginSummonPhase();
            }
        });
        btn_move = new ImageButton(button_icons[4][0], button_icons[4][2], button_icons[4][1]);
        btn_move.padLeft(PADDING_LEFT);
        btn_standby = new ImageButton(button_icons[5][0], button_icons[5][2], button_icons[5][1]);
        btn_standby.padLeft(PADDING_LEFT);
        btn_standby.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                manager.standbySelectedUnit();
            }
        });
        btn_heal = new ImageButton(button_icons[7][0], button_icons[7][2], button_icons[7][1]);
        btn_heal.padLeft(PADDING_LEFT);
        btn_info = new ImageButton(button_icons[8][0], button_icons[8][2], button_icons[8][1]);
        btn_info.padLeft(PADDING_LEFT);
    }

    public void updateButtons() {
        this.clear();
        GameCore game = manager.getGame();
        Unit selected_unit = manager.getSelectedUnit();
        switch (manager.getState()) {
            case GameManager.STATE_ACTION:
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

    @Override
    public void draw(Batch batch, float parent_alpha) {
        super.draw(batch, parent_alpha);
    }

}
