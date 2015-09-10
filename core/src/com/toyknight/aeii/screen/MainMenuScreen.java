package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.AudioManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.animator.AELogoAnimator;
import com.toyknight.aeii.animator.AELogoGlowAnimator;
import com.toyknight.aeii.screen.dialog.MainMenu;
import com.toyknight.aeii.screen.dialog.ServerListDialog;
import com.toyknight.aeii.screen.dialog.SettingDialog;
import com.toyknight.aeii.screen.widgets.CircleButton;

/**
 * @author toyknight 4/3/2015.
 */
public class MainMenuScreen extends StageScreen {

    private final AELogoAnimator logo_animator;
    private final AELogoGlowAnimator logo_glow_animator;

    private final MainMenu menu;
    private final ServerListDialog server_list;
    private final SettingDialog setting_dialog;

    private final CircleButton btn_setting;
    private final Label lb_version;

    private boolean logo_shown;

    public MainMenuScreen(AEIIApplication context) {
        super(context);
        this.logo_animator = new AELogoAnimator();
        this.logo_glow_animator = new AELogoGlowAnimator();
        this.menu = new MainMenu(getContext());
        this.menu.setVisible(false);
        this.addActor(menu);
        this.server_list = new ServerListDialog(getContext());
        this.server_list.setVisible(false);
        this.addActor(server_list);
        this.setting_dialog = new SettingDialog(getContext());
        this.setting_dialog.setVisible(false);
        this.addActor(setting_dialog);

        this.btn_setting = new CircleButton(CircleButton.LARGE, ResourceManager.getMenuIcon(4), ts);
        this.btn_setting.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (setting_dialog.isVisible()) {
                    showMenu();
                } else {
                    showSettingDialog();
                }
            }
        });
        this.btn_setting.setPosition(0, 0);
        this.btn_setting.setVisible(false);
        this.addActor(btn_setting);

        this.lb_version = new Label(getContext().getVersion(), getContext().getSkin());
        this.lb_version.setPosition(Gdx.graphics.getWidth() - lb_version.getPrefWidth(), 0);
        this.lb_version.setVisible(false);
        addActor(lb_version);

        this.logo_shown = false;
    }

    public void showMenu() {
        if (logo_shown) {
            clearScreen();
            menu.setVisible(true);
        }
    }

    public void showServerList() {
        clearScreen();
        server_list.setSelectedIndex(0);
        server_list.setVisible(true);
    }

    public void showSettingDialog() {
        clearScreen();
        setting_dialog.display();
    }

    private void clearScreen() {
        menu.setVisible(false);
        server_list.setVisible(false);
        setting_dialog.setVisible(false);
    }

    @Override
    public void draw() {
        if (logo_animator.isAnimationFinished()) {
            logo_glow_animator.render(batch);
        } else {
            logo_animator.render(batch);
        }
        super.draw();
    }

    @Override
    public void act(float delta) {
        if (logo_animator.isAnimationFinished()) {
            logo_glow_animator.update(delta);
            if (!logo_shown) {
                btn_setting.setVisible(true);
                lb_version.setVisible(true);
                menu.setVisible(true);
                logo_shown = true;
            }
        } else {
            logo_animator.update(delta);
        }
        super.act(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        AudioManager.loopMainTheme();
        showMenu();
    }

}
