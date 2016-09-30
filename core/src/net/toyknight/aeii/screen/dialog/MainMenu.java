package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.screen.MainMenuScreen;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.Platform;

/**
 * @author toyknight 6/21/2015.
 */
public class MainMenu extends BasicDialog {

    private final int MARGIN;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;

    public MainMenu(MainMenuScreen screen) {
        super(screen);
        this.MARGIN = ts / 4;
        this.BUTTON_WIDTH = ts * 4;
        this.BUTTON_HEIGHT = ts / 3 * 2;
        this.initComponents();
        this.setPosition(
                (Gdx.graphics.getWidth() - getWidth()) / 2, (Gdx.graphics.getHeight() - 85 * ts / 48 - getHeight()) / 2);
    }

    private void initComponents() {
        TextButton btn_test = new TextButton(Language.getText("LB_SKIRMISH"), getContext().getSkin());
        btn_test.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoSkirmishGameCreateScreen();
            }
        });
        this.add(btn_test).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).row();
        TextButton btn_campaign = new TextButton(Language.getText("LB_CAMPAIGN"), getContext().getSkin());
        btn_campaign.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoCampaignScreen();
            }
        });
        this.add(btn_campaign).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        TextButton btn_multiplayer = new TextButton(Language.getText("LB_MULTIPLAYER"), getContext().getSkin());
        btn_multiplayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().showDialog("server");
            }
        });
        this.add(btn_multiplayer).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        TextButton btn_map_editor = new TextButton(Language.getText("LB_MAP_EDITOR"), getContext().getSkin());
        btn_map_editor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMapEditorScreen();
            }
        });
        this.add(btn_map_editor).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        TextButton btn_map_management = new TextButton(Language.getText("LB_MANAGE_MAPS"), getContext().getSkin());
        btn_map_management.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMapManagementScreen();
            }
        });
        this.add(btn_map_management).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        TextButton btn_load = new TextButton(Language.getText("LB_LOAD_GAME"), getContext().getSkin());
        btn_load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().showDialog("load");
            }
        });
        this.add(btn_load).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        TextButton btn_help = new TextButton(Language.getText("LB_HELP"), getContext().getSkin());
        btn_help.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().showWiki();
            }
        });
        this.add(btn_help).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        if (getContext().getPlatform() != Platform.iOS) {
            TextButton btn_exit = new TextButton(Language.getText("LB_EXIT"), getContext().getSkin());
            btn_exit.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.exit();
                }
            });
            this.add(btn_exit).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(MARGIN).padTop(0).row();
        }
        this.layout();
        this.pack();
    }

    public MainMenuScreen getOwner() {
        return (MainMenuScreen) super.getOwner();
    }

}
