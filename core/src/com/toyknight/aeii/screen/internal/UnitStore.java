package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.UnitListListener;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by toyknight on 4/20/2015.
 */
public class UnitStore extends Table implements UnitListListener {

    private final int ts;
    private final GameScreen screen;

    private TextButton btn_buy;
    private AvailableUnitList unit_list;

    private int castle_x;
    private int castle_y;
    private Unit selected_unit;

    public UnitStore(final GameScreen screen, Skin skin) {
        this.screen = screen;
        this.ts = screen.getContext().getTileSize();
        this.initComponents(skin);
    }

    private void initComponents(Skin skin) {
        this.btn_buy = new TextButton(Language.getText("LB_BUY"), skin);
        this.btn_buy.setBounds(ts * 6, ts / 2, ts * 2, ts / 2);
        this.btn_buy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.getGameManager().buyUnit(selected_unit.getPackage(), selected_unit.getIndex(), castle_x, castle_y);
                close();
            }
        });
        this.addActor(btn_buy);
        TextButton btn_close = new TextButton(Language.getText("LB_CLOSE"), skin);
        btn_close.setBounds(ts * 8 + ts / 2, ts / 2, ts * 2, ts / 2);
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        this.addActor(btn_close);


        this.unit_list = new AvailableUnitList(screen.getGameManager(), ts);
        this.unit_list.setUnitListListener(this);
        ScrollPane sp_unit_list = new ScrollPane(unit_list);
        sp_unit_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_unit_list.setScrollBarPositions(false, true);
        sp_unit_list.setBounds(ts / 2, ts / 2, ts * 5, ts * 3 / 2 * 5);
        this.addActor(sp_unit_list);
    }

    public void display(int castle_x, int castle_y) {
        this.castle_x = castle_x;
        this.castle_y = castle_y;
        GameManager manager = screen.getGameManager();
        HashMap<String, ArrayList<Integer>> available_units = manager.getGame().getRule().getAvailableUnitList();
        unit_list.setAvailableUnits(available_units);
        this.setVisible(true);
    }

    public void close() {
        this.setVisible(false);
        screen.onButtonUpdateRequested();
    }

    public void onUnitSelected(String package_name, int index) {
        selected_unit = UnitFactory.getSample(index, package_name);
        if (selected_unit.isCommander()) {
            selected_unit = screen.getGameManager().getGame().getCommander(selected_unit.getTeam());
        }
        updateButton();
    }

    private void updateButton() {
        GameManager manager = screen.getGameManager();
        int current_team = manager.getGame().getCurrentTeam();
        if (selected_unit != null
                && manager.getGame().getCurrentPlayer().getPopulation() < manager.getGame().getRule().getMaxPopulation()) {
            int price = screen.getGame().getUnitPrice(selected_unit.getPackage(), selected_unit.getIndex(), current_team);
            if (price >= 0) {
                if (manager.getGame().getCurrentPlayer().getGold() >= selected_unit.getPrice()) {
                    btn_buy.setTouchable(Touchable.enabled);
                } else {
                    btn_buy.setTouchable(Touchable.disabled);
                }
            } else {
                btn_buy.setTouchable(Touchable.disabled);
            }
        } else {
            btn_buy.setTouchable(Touchable.disabled);
        }
    }

    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
