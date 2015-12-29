package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.manager.MapEditor;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 7/9/2015.
 */
public class UnitSelector extends Container<ScrollPane> {

    private final int ts;
    private final MapEditor editor;

    public UnitSelector(MapEditor editor, int ts) {
        this.editor = editor;
        this.ts = ts;
        this.initComponents();
    }

    private void initComponents() {
        ImageButton[] btn_team = new ImageButton[4];
        for (int i = 0; i < 4; i++) {
            TextureRegionDrawable team_bg = new TextureRegionDrawable(new TextureRegion(ResourceManager.getTeamBackground(i)));
            team_bg.setMinWidth(ts);
            team_bg.setMinHeight(ts);
            btn_team[i] = new ImageButton(team_bg);
        }
        Table unit_table = new Table();
        unit_table.padBottom(ts / 4);
        btn_team[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSelectedTeam(0);
            }
        });
        unit_table.add(btn_team[0]).padTop(ts / 4);
        btn_team[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSelectedTeam(1);
            }
        });
        unit_table.add(btn_team[1]).padLeft(ts / 4).padTop(ts / 4).row();
        btn_team[2].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSelectedTeam(2);
            }
        });
        unit_table.add(btn_team[2]).padTop(ts / 4);
        btn_team[3].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSelectedTeam(3);
            }
        });
        unit_table.add(btn_team[3]).padLeft(ts / 4).padTop(ts / 4).row();

        //add units
        int index = 0;
        for (int i = 0; i < UnitFactory.getUnitCount(); i++) {
            UnitButton btn_unit = new UnitButton(editor, UnitFactory.getSample(i), ts);
            if (index % 2 == 0) {
                unit_table.add(btn_unit).padTop(ts / 4);
            } else {
                unit_table.add(btn_unit).padLeft(ts / 4).padTop(ts / 4).row();
            }
            index++;
        }

        ScrollPane sp_unit_table = new ScrollPane(unit_table);
        sp_unit_table.setScrollBarPositions(false, true);
        sp_unit_table.setFadeScrollBars(false);
        sp_unit_table.setBounds(
                Gdx.graphics.getWidth() - ts * 2 - ts / 4 * 3, ts,
                ts * 2 - ts / 4 * 3, Gdx.graphics.getHeight() - ts);
        this.setActor(sp_unit_table);
    }

    public void setSelectedTeam(int team) {
        editor.setSelectedTeam(team);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
    }

}
