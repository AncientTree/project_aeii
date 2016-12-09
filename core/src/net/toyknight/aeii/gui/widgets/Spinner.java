package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 8/31/2015.
 */
public class Spinner<T> extends AEIITable {

    private float prefWidth;
    private final float prefHeight;

    private SpinnerListener listener;

    private T[] items;
    private int selected_index;

    private TextButton btn_left;
    private TextButton btn_right;

    public Spinner(GameContext context) {
        super(context);
        this.prefWidth = ts * 3;
        this.setWidth(prefWidth);
        this.prefHeight = ts;
        this.setHeight(prefHeight);
        this.btn_left = new TextButton("<", getContext().getSkin());
        this.btn_left.setBounds(0, 0, ts, ts);
        this.btn_left.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selected_index = selected_index > 0 ? selected_index - 1 : items.length - 1;
                if (listener != null) {
                    listener.onValueChanged(Spinner.this);
                }
            }
        });
        this.addActor(btn_left);
        this.btn_right = new TextButton(">", getContext().getSkin());
        this.btn_right.setBounds(ts * 2, 0, ts, ts);
        this.btn_right.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selected_index = selected_index < items.length - 1 ? selected_index + 1 : 0;
                if (listener != null) {
                    listener.onValueChanged(Spinner.this);
                }
            }
        });
        this.addActor(btn_right);
    }

    public void setContentWidth(int width) {
        this.prefWidth = width + ts * 2;
        this.setWidth(prefWidth);
        this.btn_right.setBounds(ts + width, 0, ts, ts);
    }

    public void setListener(SpinnerListener listener) {
        this.listener = listener;
    }

    @Override
    public float getPrefWidth() {
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        return prefHeight;
    }

    public void setItems(T[] items) {
        this.items = items;
        this.selected_index = 0;
    }

    protected T[] getItems() {
        return items;
    }

    public void setSelectedIndex(int index) {
        if (0 <= index && index < items.length) {
            selected_index = index;
        }
    }

    public T getSelectedItem() {
        return items[selected_index];
    }

    public void setEnabled(boolean enabled) {
        GameContext.setButtonEnabled(btn_left, enabled);
        GameContext.setButtonEnabled(btn_right, enabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(getResources().getBorderDarkColor(),
                getX() + ts / 2, getY() + ts / 24, getWidth() - ts, getHeight() - ts / 12);
        getContext().getFontRenderer().drawTextCenter(
                batch, getSelectedItem().toString(), getX() + ts, getY(), getWidth() - ts * 2, getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }


}
