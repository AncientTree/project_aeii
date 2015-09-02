package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 8/31/2015.
 */
public class SettingDialog extends Table {

    private final int ts;
    private final AEIIApplication context;

    private TextField tf_username;

    public SettingDialog(AEIIApplication context) {
        this.context = context;
        this.ts = getContext().getTileSize();
        int width = ts * 10;
        int height = ts * 6;
        int title_height = ts * 85 / 48;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - title_height - height) / 2, width, height);
        this.initComponents();
    }

    private void initComponents() {
        TextButton btn_ok = new TextButton(Language.getText("LB_OK"), getContext().getSkin());
        btn_ok.setBounds((getWidth() - ts * 3) / 2, ts / 2, ts * 3, ts);
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                save();
                getContext().getMainMenuScreen().showMenu();
            }
        });
        addActor(btn_ok);

        Label lb_username = new Label(Language.getText("LB_USERNAME"), getContext().getSkin());
        lb_username.setPosition(ts / 2, getHeight() - ts / 2 - lb_username.getPrefHeight());
        addActor(lb_username);

        tf_username = new TextField("", getContext().getSkin());
        tf_username.setPosition(ts * 3 + ts / 2, getHeight() - ts / 2 - tf_username.getPrefHeight());
        tf_username.setMaxLength(12);
        tf_username.setWidth(getWidth() - ts * 4);
        addActor(tf_username);
    }

    private AEIIApplication getContext() {
        return context;
    }

    private void save() {
        getContext().updateConfigureation("username", tf_username.getText());
    }

    public void display() {
        String username = getContext().getUsername();
        tf_username.setText(username);
        setVisible(true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
