package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.screen.MainMenuScreen;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/31/2015.
 */
public class SettingDialog extends BasicDialog {

    private TextField tf_username;

    public SettingDialog(MainMenuScreen screen) {
        super(screen);
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
                getOwner().closeDialog("setting");
            }
        });
        addActor(btn_ok);

        Label lb_username = new Label(Language.getText("LB_USERNAME"), getContext().getSkin());
        lb_username.setPosition(ts / 2, getHeight() - ts / 2 - lb_username.getPrefHeight());
        addActor(lb_username);

        tf_username = new TextField("", getContext().getSkin());
        tf_username.setPosition(ts * 3 + ts / 2, getHeight() - ts / 2 - tf_username.getPrefHeight());
        tf_username.setTextFieldFilter(new TextFilter());
        tf_username.setMaxLength(10);
        tf_username.setWidth(getWidth() - ts * 4);
        addActor(tf_username);
    }

    private void save() {
        String username = tf_username.getText();
        if (username.length() > 0) {
            getContext().updateConfiguration("username", username);
        } else {
            getContext().updateConfiguration("username", "nobody");
        }
    }

    @Override
    public MainMenuScreen getOwner() {
        return (MainMenuScreen) super.getOwner();
    }

    @Override
    public void display() {
        String username = getContext().getUsername();
        tf_username.setText(username);
        setVisible(true);
    }

    private class TextFilter implements TextField.TextFieldFilter {

        @Override
        public boolean acceptChar(TextField textField, char c) {
            return c == 32 || (65 <= c && c <= 90) || (97 <= c && c <= 122);
        }

    }

}
