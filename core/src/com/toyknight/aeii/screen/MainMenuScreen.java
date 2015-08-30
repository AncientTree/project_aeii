package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.AudioManager;
import com.toyknight.aeii.animator.AELogoAnimator;
import com.toyknight.aeii.animator.AELogoGlowAnimator;
import com.toyknight.aeii.screen.internal.MainMenu;
import com.toyknight.aeii.screen.internal.ServerList;

/**
 * Created by toyknight on 4/3/2015.
 */
public class MainMenuScreen extends StageScreen {

    private final AELogoAnimator logo_animator;
    private final AELogoGlowAnimator logo_glow_animator;

    private final MainMenu menu;
    private final ServerList server_list;

    private boolean logo_shown;

    public MainMenuScreen(AEIIApplication context) {
        super(context);
        this.logo_animator = new AELogoAnimator();
        this.logo_glow_animator = new AELogoGlowAnimator();
        this.menu = new MainMenu(getContext());
        this.menu.setVisible(false);
        this.addActor(menu);
        this.server_list = new ServerList(getContext());
        this.server_list.setVisible(false);
        this.addActor(server_list);
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

    private void clearScreen() {
        menu.setVisible(false);
        server_list.setVisible(false);
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
