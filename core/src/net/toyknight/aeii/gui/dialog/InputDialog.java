package net.toyknight.aeii.gui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.gui.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/22/2016.
 */
public class InputDialog extends Dialog {

    private final int ts;

    private final StageScreen owner;

    private final Label input_message;
    private final TextField input_field;
    private final TextButton input_btn_ok;
    private final TextButton input_btn_cancel;

    private Input.TextInputListener input_listener;

    public InputDialog(StageScreen owner) {
        super("", owner.getContext().getSkin());
        this.owner = owner;
        this.ts = owner.getContext().getTileSize();

        int idw = ts * 8;
        int idh = ts / 2 * 7;
        setBounds((Gdx.graphics.getWidth() - idw) / 2, (Gdx.graphics.getHeight() - idh) / 2, idw, idh);
        input_message = new Label("", getContext().getSkin());
        input_message.setWrap(true);
        getContentTable().add(input_message).width(idw - ts / 2).pad(ts / 4).padBottom(ts / 12).row();
        input_field = new TextField("", getContext().getSkin());
        input_field.setFocusTraversal(false);
        input_field.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n' || c == '\r') {
                    close();
                    fireInputEvent(input_field.getText());
                }
            }
        });
        getContentTable().add(input_field).size(idw - ts / 2, ts / 2 + ts / 8);
        input_btn_ok = new TextButton(Language.getText("LB_OK"), getContext().getSkin());
        input_btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
                fireInputEvent(input_field.getText());
            }
        });
        getButtonTable().add(input_btn_ok).size(ts * 3, ts / 3 * 2).pad(ts / 4).padTop(ts / 8);
        input_btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        input_btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
                fireCancelEvent();
            }
        });
        getButtonTable().add(input_btn_cancel).size(ts * 3, ts / 3 * 2).pad(ts / 4).padTop(ts / 8);
    }

    private void fireInputEvent(String str) {
        if (input_listener != null) {
            input_listener.input(str);
        }
    }

    private void fireCancelEvent() {
        if (input_listener != null) {
            input_listener.canceled();
        }
    }

    public StageScreen getOwner() {
        return owner;
    }

    public GameContext getContext() {
        return getOwner().getContext();
    }

    public void setFilter(TextField.TextFieldFilter filter) {
        input_field.setTextFieldFilter(filter);
    }

    public void display(String message, int max_length, boolean password, Input.TextInputListener input_listener) {
        this.input_listener = input_listener;
        input_message.setText(message);
        input_field.setText("");
        input_field.setMaxLength(max_length);
        input_field.setAlignment(password ? Align.center : Align.left);
        getContentTable().pack();
        pack();
        setPosition((Gdx.graphics.getWidth() - getWidth()) / 2, (Gdx.graphics.getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        getOwner().updateFocus();
    }

}
