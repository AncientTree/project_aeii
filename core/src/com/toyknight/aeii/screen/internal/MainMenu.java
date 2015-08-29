package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 6/21/2015.
 */
public class MainMenu extends Table {

    private final int ts;
    private final AEIIApplication context;

    private final int MARGIN;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;
    private final int BUTTON_COUNT = 8;

    private TextButton btn_test;
    private TextButton btn_campaign;
    private TextButton btn_multiplayer;
    private TextButton btn_map_editor;
    private TextButton btn_load;
    private TextButton btn_online_contents;
    private TextButton btn_about;
    private TextButton btn_exit;

    public MainMenu(AEIIApplication context) {
        this.context = context;
        this.ts = getContext().getTileSize();

        this.MARGIN = ts / 4;
        this.BUTTON_WIDTH = ts * 4;
        this.BUTTON_HEIGHT = ts / 3 * 2;
        int menu_width = BUTTON_WIDTH + MARGIN * 2;
        int menu_height = BUTTON_HEIGHT * BUTTON_COUNT + MARGIN * (BUTTON_COUNT + 1);
        this.setBounds(
                (Gdx.graphics.getWidth() - menu_width) / 2,
                (Gdx.graphics.getHeight() - 85 - menu_height) / 2,
                menu_width, menu_height);
        this.initComponents();
    }

    private AEIIApplication getContext() {
        return context;
    }

    private void initComponents() {
        this.btn_test = new TextButton("Map Test!", getContext().getSkin());
        this.btn_test.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoTestScreen();
            }
        });
        this.add(btn_test).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_campaign = new TextButton(Language.getText("LB_CAMPAIGN"), getContext().getSkin());
        this.btn_campaign.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().showMessage(Language.getText("MSG_INFO_NSY"), null);
            }
        });
        this.add(btn_campaign).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_multiplayer = new TextButton(Language.getText("LB_MULTIPLAYER"), getContext().getSkin());
        this.btn_multiplayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().getMainMenuScreen().showServerList();
            }
        });
        this.add(btn_multiplayer).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_map_editor = new TextButton(Language.getText("LB_MAP_EDITOR"), getContext().getSkin());
        this.btn_map_editor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMapEditorScreen();
            }
        });
        this.add(btn_map_editor).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_load = new TextButton(Language.getText("LB_LOAD"), getContext().getSkin());
        this.btn_load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().showMessage(Language.getText("MSG_INFO_NSY"), null);
            }
        });
        this.add(btn_load).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_online_contents = new TextButton(Language.getText("LB_ONLINE_CONTENTS"), getContext().getSkin());
        this.btn_online_contents.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().showMessage(Language.getText("MSG_INFO_NSY"), null);
            }
        });
        this.add(btn_online_contents).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_about = new TextButton(Language.getText("LB_ABOUT"), getContext().getSkin());
        this.btn_about.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().showMessage("toyknight - 2015", null);
            }
        });
        this.add(btn_about).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_exit = new TextButton(Language.getText("LB_EXIT"), getContext().getSkin());
        this.btn_exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().getNetworkManager().stop();
                Gdx.app.exit();
            }
        });
        this.add(btn_exit).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
        this.layout();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
