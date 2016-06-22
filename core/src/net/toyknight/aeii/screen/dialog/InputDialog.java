package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/15/2016.
 */
public class InputDialog extends BasicDialog {

    private final Label label_message;

    private final TextField input;

    private final TextButton btn_ok;

    private final TextButton btn_cancel;

    private Callable callable_ok;

    private Callable callable_cancel;

    public InputDialog(StageScreen owner) {
        super(owner);
        int width = ts * 5 + ts / 2;
        int height = ts * 3 + ts / 2;
        setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - height) / 2, width, height);

        label_message = new Label("", getContext().getSkin());
        label_message.setAlignment(Align.center);
        add(label_message).size(ts * 4 + ts / 2, ts).row();

        input = new TextField("", getContext().getSkin());
        add(input).size(ts * 4 + ts / 2, ts / 2).padBottom(ts / 2).row();

        Table button_bar = new Table();

        btn_ok = new TextButton(Language.getText("LB_OK"), getContext().getSkin());
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callable_ok != null) {
                    callable_ok.call();
                }
            }
        });
        button_bar.add(btn_ok).size(ts * 2, ts);

        btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callable_cancel != null) {
                    callable_cancel.call();
                }
            }
        });
        button_bar.add(btn_cancel).size(ts * 2, ts).padLeft(ts / 2);

        add(button_bar).size(ts * 5 + ts / 2, ts).padBottom(ts / 2);
    }

    public void setOkCallable(Callable callable) {
        callable_ok = callable;
    }

    public void setCancelCallable(Callable callable) {
        callable_cancel = callable;
    }

    public void setMessage(String message) {
        label_message.setText(message);
    }

    public String getInput() {
        return input.getText();
    }

    @Override
    public void display() {
        input.setText("");
    }

}
