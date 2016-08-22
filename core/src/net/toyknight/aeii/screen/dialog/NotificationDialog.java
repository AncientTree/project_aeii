package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/22/2016.
 */
public class NotificationDialog extends Dialog {

    private final int ts;

    private final StageScreen owner;

    private final Label prompt_message;
    private final TextButton prompt_btn_ok;

    private Callable prompt_callback;

    public NotificationDialog(StageScreen owner) {
        super("", owner.getContext().getSkin());
        this.owner = owner;
        this.ts = owner.getContext().getTileSize();

        int pdw = ts * 6;
        int pdh = ts / 2 * 5;
        setBounds((Gdx.graphics.getWidth() - pdw) / 2, (Gdx.graphics.getHeight() - pdh) / 2, pdw, pdh);
        //set the message
        getContentTable().pad(ts / 4);
        prompt_message = new Label("", getContext().getSkin());
        prompt_message.setAlignment(Align.center);
        prompt_message.setWrap(true);
        getContentTable().add(prompt_message).width(ts * 6);
        //set the button
        prompt_btn_ok = new TextButton(Language.getText("LB_OK"), getContext().getSkin());
        prompt_btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
                fireCallback();
            }
        });
        getButtonTable().add(prompt_btn_ok).size(ts / 2 * 5, ts / 3 * 2).padBottom(ts / 8);
    }

    private void fireCallback() {
        if (prompt_callback != null) {
            prompt_callback.call();
            prompt_callback = null;
        }
    }

    public StageScreen getOwner() {
        return owner;
    }

    public GameContext getContext() {
        return getOwner().getContext();
    }

    public void display(String message, Callable callback) {
        prompt_callback = callback;
        prompt_message.setText(message);
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
