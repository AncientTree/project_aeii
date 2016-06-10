package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.toyknight.aeii.Callable;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/10/2016.
 */
public class ConfirmDialog extends BasicDialog {

    private final Label label_message;

    private final TextButton btn_yes;
    private final TextButton btn_no;

    private Callable yes_callable;
    private Callable no_callable;

    public ConfirmDialog(StageScreen owner) {
        super(owner);

        label_message = new Label("", getContext().getSkin());
        label_message.setAlignment(Align.center);
        add(label_message).size(ts * 6 + ts / 2, ts).row();

        Table button_bar = new Table();
        btn_yes = new TextButton(Language.getText("LB_YES"), getContext().getSkin());
        btn_yes.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                yes_callable.call();
            }
        });
        button_bar.add(btn_yes).size(ts * 3, ts);
        btn_no = new TextButton(Language.getText("LB_NO"), getContext().getSkin());
        btn_no.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                no_callable.call();
            }
        });
        button_bar.add(btn_no).size(ts * 3, ts).padLeft(ts / 2);
        add(button_bar).size(ts * 6 + ts / 2, ts);

        int width = ts * 7 + ts / 2;
        int height = ts * 3;
        setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - height) / 2, width, height);
    }

    public void setMessage(String message) {
        label_message.setText(message);
    }

    public void setYesCallback(Callable callable) {
        this.yes_callable = callable;
    }

    public void setNoCallable(Callable callable) {
        this.no_callable = callable;
    }

}
