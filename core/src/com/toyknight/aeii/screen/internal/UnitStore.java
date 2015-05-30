package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.Language;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by toyknight on 4/20/2015.
 */
public class UnitStore extends Table {

    private final int ts;
    private final GameScreen screen;

    private TextButton btn_buy;
    private AvailableUnitList unit_list;

    public UnitStore(final GameScreen screen, Skin skin) {
        this.screen = screen;
        this.ts = screen.getContext().getTileSize();
        this.initComponents(skin);
    }

    private void initComponents(Skin skin) {
        this.btn_buy = new TextButton(Language.getText("LB_BUY"), skin);
        this.btn_buy.setBounds(ts * 6, ts / 2, ts * 2, ts / 2);
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
        ScrollPane sp_unit_list = new ScrollPane(unit_list);
        sp_unit_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_unit_list.setScrollBarPositions(false, true);
        sp_unit_list.setBounds(ts / 2, ts / 2, ts * 5, ts * 3 / 2 * 5);
        this.addActor(sp_unit_list);
    }

    public void display() {
        GameManager manager = screen.getGameManager();
        HashMap<String, ArrayList<Integer>> available_units = manager.getGame().getRule().getAvailableUnitList();
        unit_list.setAvailableUnits(available_units);
        this.setVisible(true);
    }

    public void close() {
        this.setVisible(false);
        screen.onButtonUpdateRequested();
    }

    private void updateButton() {

    }

    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
