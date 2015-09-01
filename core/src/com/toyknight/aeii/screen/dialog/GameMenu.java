package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 6/1/2015.
 */
public class GameMenu extends Table {

    private final int ts;
    private final int MARGIN;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;
    private final int BUTTON_COUNT = 6;

    private final GameScreen screen;

    private TextButton btn_mini_map;
    private TextButton btn_objective;
    private TextButton btn_load;
    private TextButton btn_save;
    private TextButton btn_exit;
    private TextButton btn_resume;

    public GameMenu(GameScreen screen) {
        this.screen = screen;
        this.ts = getContext().getTileSize();
        this.MARGIN = ts / 4;
        this.BUTTON_WIDTH = ts * 4;
        this.BUTTON_HEIGHT = ts / 3 * 2;
        int menu_width = BUTTON_WIDTH + MARGIN * 2;
        int menu_height = BUTTON_HEIGHT * BUTTON_COUNT + MARGIN * (BUTTON_COUNT + 1);
        this.setBounds(
                (screen.getViewportWidth() - menu_width) / 2,
                (screen.getViewportHeight() - menu_height) / 2 + ts,
                menu_width, menu_height);
        this.initComponents();
    }

    private void initComponents() {
        this.btn_mini_map = new TextButton(Language.getText("LB_MINI_MAP"), getContext().getSkin());
        this.btn_mini_map.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.showMiniMap();
            }
        });
        this.add(btn_mini_map).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_objective = new TextButton(Language.getText("LB_OBJECTIVE"), getContext().getSkin());
        this.add(btn_objective).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_load = new TextButton(Language.getText("LB_LOAD"), getContext().getSkin());
        this.btn_load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //screen.showLoadDialog();
            }
        });
        this.add(btn_load).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        this.btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //screen.showSaveDialog();
            }
        });
        this.add(btn_save).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_exit = new TextButton(Language.getText("LB_EXIT_GAME"), getContext().getSkin());
        this.btn_exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getContext().getNetworkManager().isConnected()) {
                    getContext().getNetworkManager().disconnect();
                }
                getContext().gotoMainMenuScreen();
            }
        });
        this.add(btn_exit).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_resume = new TextButton(Language.getText("LB_RESUME"), getContext().getSkin());
        this.btn_resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameMenu.this.setVisible(false);
                screen.onButtonUpdateRequested();
            }
        });
        this.add(btn_resume).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
        this.layout();
    }

    private AEIIApplication getContext() {
        return screen.getContext();
    }

    public void display() {
        this.setVisible(true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
