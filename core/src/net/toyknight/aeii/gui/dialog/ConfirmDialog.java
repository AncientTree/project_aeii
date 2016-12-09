package net.toyknight.aeii.gui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.gui.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/22/2016.
 */
public class ConfirmDialog extends Dialog {

    private final int ts;

    private final StageScreen owner;

    private final Label confirm_message;
    private final TextButton btn_confirm;
    private final TextButton btn_cancel;

    private ConfirmDialogListener listener;

    public ConfirmDialog(StageScreen owner) {
        super("", owner.getContext().getSkin());
        this.owner = owner;
        this.ts = owner.getContext().getTileSize();

        int cdw = ts * 6;
        int cdh = ts / 2 * 5;
        setBounds((Gdx.graphics.getWidth() - cdw) / 2, (Gdx.graphics.getHeight() - cdh) / 2, cdw, cdh);
        //set the message
        getContentTable().pad(ts / 4);
        confirm_message = new Label("", getContext().getSkin());
        confirm_message.setAlignment(Align.center);
        confirm_message.setWrap(true);
        getContentTable().add(confirm_message).width(ts * 6);
        //set the button
        btn_confirm = new TextButton(Language.getText("LB_YES"), getContext().getSkin());
        btn_confirm.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
                fireConfirmEvent();
            }
        });
        getButtonTable().add(btn_confirm).size(ts * 2, ts / 3 * 2).padRight(ts / 2).padBottom(ts / 8);
        btn_cancel = new TextButton(Language.getText("LB_NO"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
                fireCancelEvent();
            }
        });
        getButtonTable().add(btn_cancel).size(ts * 2, ts / 3 * 2).padBottom(ts / 8);
    }

    private void fireConfirmEvent() {
        if (listener != null) {
            listener.confirmed();
        }
    }

    private void fireCancelEvent() {
        if (listener != null) {
            listener.canceled();
        }
    }

    public StageScreen getOwner() {
        return owner;
    }

    public GameContext getContext() {
        return getOwner().getContext();
    }

    public void display(String message, ConfirmDialogListener listener) {
        this.listener = listener;
        confirm_message.setText(message);
        getContentTable().pack();
        pack();
        setPosition((Gdx.graphics.getWidth() - getWidth()) / 2, (Gdx.graphics.getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        getOwner().updateFocus();
    }

    public interface ConfirmDialogListener {

        void confirmed();

        void canceled();
    }

}
