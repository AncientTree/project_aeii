package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.utils.Language;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by toyknight on 5/29/2015.
 */
public class AvailableUnitList extends Widget implements Cullable {

    private final int ts;
    private final int item_height;

    private final int big_circle_width;
    private final int big_circle_height;
    private final int bc_offset;
    private final int unit_offset;


    private GameCore game;
    private HashMap<String, ArrayList<Integer>> available_units;

    private float prefWidth;
    private float prefHeight;
    private Rectangle cullingArea;

    private int selected_index = 0;

    private UnitListListener listener;

    public AvailableUnitList(int ts) {
        this.ts = ts;
        this.item_height = ts / 2 * 3;
        this.big_circle_width = ts / 24 * 32;
        this.big_circle_height = ts / 24 * 33;
        this.bc_offset = (item_height - big_circle_height) / 2;
        this.unit_offset = (item_height - ts) / 2;
        addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && button != 0) return false;
                onSelect(y);
                return true;
            }
        });
    }

    private void onSelect(float y) {
        float height = getHeight();
        int index = (int) ((height - y) / item_height);
        if (index != selected_index) {
            selected_index = index;
            updateSelection();
        }
    }

    private void updateSelection() {
        int index = 0;
        for (String package_name : available_units.keySet()) {
            for (Integer unit_index : available_units.get(package_name)) {
                if (index == selected_index) {
                    if (listener != null) {
                        listener.onUnitSelected(package_name, unit_index);
                    }
                    return;
                }
                index++;
            }
        }
    }

    public void setGame(GameCore game) {
        this.game = game;
    }

    private GameCore getGame() {
        return game;
    }

    public void setUnitListListener(UnitListListener listener) {
        this.listener = listener;
    }

    public void setAvailableUnits(HashMap<String, ArrayList<Integer>> list) {
        this.available_units = list;
        int count = 0;
        for (String package_name : available_units.keySet()) {
            for (Integer unit_index : available_units.get(package_name)) {
                count++;
            }
        }
        this.selected_index = 0;
        this.updateSelection();
        this.prefWidth = getWidth();
        this.prefHeight = count * item_height;
    }

    @Override
    public void setCullingArea(Rectangle cullingArea) {
        this.cullingArea = cullingArea;
    }

    @Override
    public float getPrefWidth() {
        validate();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        validate();
        return prefHeight;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int index = 0;
        int current_team = getGame().getCurrentTeam();
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        float itemY = height;
        for (String package_name : available_units.keySet()) {
            for (Integer unit_index : available_units.get(package_name)) {
                if (index == selected_index) {
                    batch.draw(ResourceManager.getListSelectedBackground(), x, y + itemY - item_height, width, item_height);
                }
                batch.draw(ResourceManager.getBigCircleTexture(0),
                        x + bc_offset, y + itemY - item_height + bc_offset, big_circle_width, big_circle_height);
                batch.draw(ResourceManager.getUnitTexture(package_name, current_team, unit_index, 0, 0),
                        x + unit_offset, y + itemY - item_height + unit_offset, ts, ts);
                batch.flush();
                FontRenderer.drawTextCenter(batch, Language.getUnitName(package_name, unit_index),
                        x + big_circle_width + bc_offset, y + itemY - item_height, width - big_circle_width - bc_offset, item_height);
                index++;
                itemY -= item_height;
            }
            //batch.draw(ResourceManager.getMenuIcon(0), x, y + height - 800, width, 800);
            super.draw(batch, parentAlpha);
        }
    }

}