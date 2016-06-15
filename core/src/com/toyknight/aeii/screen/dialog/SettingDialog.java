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

    private TextField text_username;

    public SettingDialog(MainMenuScreen screen) {
        super(screen);
        int width = ts * 10;
        int height = ts * 6;
        int title_height = ts * 85 / 48;
        this.setBounds(
                (Gdx.graphics.getWidth() - width) / 2,
                (Gdx.graphics.getHeight() - title_height - height) / 2,
                width, height);
        this.initComponents();
    }

    private void initComponents() {
        TextButton btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        btn_save.setBounds((getWidth() - ts * 6 - ts / 2) / 2, ts / 2, ts * 3, ts);
        btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                save();
                getOwner().closeDialog("setting");
            }
        });
        addActor(btn_save);

        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.setBounds((getWidth() - ts * 6 - ts / 2) / 2 + ts * 3 + ts / 2, ts / 2, ts * 3, ts);
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("setting");
            }
        });
        addActor(btn_cancel);

        Label lb_username = new Label(Language.getText("LB_USERNAME") + ": ", getContext().getSkin());
        lb_username.setBounds(ts / 2, getHeight() - ts, lb_username.getPrefWidth(), ts / 2);
        addActor(lb_username);

        text_username = new TextField("", getContext().getSkin());
        text_username.setBounds(
                lb_username.getPrefWidth() + ts, getHeight() - ts,
                ts * 9 - lb_username.getPrefWidth() - ts / 2, ts / 2);
        text_username.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == '_';
            }
        });
        text_username.setMaxLength(10);
        addActor(text_username);
    }

    private void save() {
        String username = text_username.getText();
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
        text_username.setText(getContext().getUsername());
        setVisible(true);
    }

}
