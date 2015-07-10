package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.utils.MapFactory;

/**
 * Created by toyknight on 6/21/2015.
 */
public class MapList extends Widget implements Cullable {

    private final int ts;
    private final int item_height;
    private final float text_offset;
    private float prefWidth;
    private float prefHeight;

    private Map[] maps;
    private String[] map_names;
    private int[] player_counts;
    private int selected_index;

    public MapList(int ts) {
        this.ts = ts;
        this.item_height = ts;
        this.text_offset = (ts - FontRenderer.getLabelFont().getCapHeight()) / 2;
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
        if (index != selected_index && index < maps.length) {
            selected_index = index;
        }
    }

    public Map getSelectedMap() {
        return maps[selected_index];
    }

    public String getSelectedMapName() {
        String name = map_names[selected_index];
        return name.substring(0, name.lastIndexOf("."));
    }

    public void setMaps(FileHandle[] map_files) {
        this.maps = new Map[map_files.length];
        this.map_names = new String[map_files.length];
        this.player_counts = new int[map_files.length];
        for (int i = 0; i < map_files.length; i++) {
            try {
                maps[i] = MapFactory.createMap(map_files[i]);
                player_counts[i] = maps[i].getPlayerCount();
                map_names[i] = map_files[i].name();
            } catch (AEIIException ex) {
            }
        }
        this.selected_index = 0;
        this.prefWidth = getWidth();
        this.prefHeight = map_files.length * item_height;
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
    public void setCullingArea(Rectangle cullingArea) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        float itemY = height;
        for (int index = 0; index < maps.length; index++) {
            if (index == selected_index) {
                batch.draw(ResourceManager.getListSelectedBackground(), x, y + itemY - item_height, width, item_height);
            }
            FontRenderer.drawLabel(batch,
                    "(" + player_counts[index] + ") " + map_names[index],
                    x + text_offset, y + itemY - item_height + text_offset);
            itemY -= item_height;
        }
        super.draw(batch, parentAlpha);
    }

}
