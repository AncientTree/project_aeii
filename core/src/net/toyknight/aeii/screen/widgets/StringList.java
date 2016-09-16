package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 8/25/2015.
 */
public class StringList<T> extends AEIIWidget {

    protected final int item_height;
    protected final float text_offset;
    private float prefWidth;
    private float prefHeight;

    protected final Array<T> items = new Array<T>();
    final ArraySelection<T> selection = new ArraySelection<T>(items);

    private SelectionListener listener;

    public StringList(GameContext context, int item_height) {
        super(context);
        this.item_height = item_height;
        this.text_offset = (item_height - getResources().getTextFont().getCapHeight()) / 2;
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selection.isDisabled()) return;
                StringList.this.onClick(y);
            }
        });
    }

    private void onClick(float y) {
        if (items.size == 0) return;
        float height = getHeight();
        int index = (int) ((height - y) / item_height);
        index = Math.max(0, index);
        index = Math.min(items.size - 1, index);
        if (selection.contains(items.get(index))) {
            fireSelectEvent(index, getSelected());
        } else {
            selection.choose(items.get(index));
            fireChangeEvent(index, getSelected());
        }
    }

    private void fireSelectEvent(int index, Object value) {
        if (listener != null) {
            listener.onSelect(index, value);
        }
    }

    private void fireChangeEvent(int index, Object value) {
        if (listener != null) {
            listener.onChange(index, value);
        }
    }

    @Override
    public float getPrefWidth() {
        validate();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        validate();
        return prefHeight;
    }

    public void setListener(SelectionListener listener) {
        this.listener = listener;
    }

    public void setEnabled(boolean b) {
        selection.setDisabled(!b);
    }

    public ArraySelection<T> getSelection() {
        return selection;
    }

    public T getSelected() {
        return selection.first();
    }

    public void setItems(Array<? extends T> items) {
        if (items == null) throw new IllegalArgumentException("list items cannot be null.");
        this.items.clear();
        this.selection.clear();
        this.items.addAll(items);
        updateList();
    }

    public void updateList() {
        float max_width = 0;
        for (T item : items) {
            float width = text_offset * 2 + getContext().getFontRenderer().getTextLayout(item.toString()).width;
            if (width > max_width) {
                max_width = width;
            }
        }
        this.prefWidth = max_width;
        this.prefHeight = items.size * item_height;
        if (items.size > 0) {
            setSelectedIndex(0);
        }
        invalidateHierarchy();
    }

    public void setSelectedIndex(int index) {
        if (index < -1 || index >= items.size)
            throw new IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index);
        if (index == -1) {
            selection.clear();
        } else {
            selection.set(items.get(index));
        }
    }

    public void clearItems() {
        if (items.size == 0) return;
        items.clear();
        selection.clear();
        invalidateHierarchy();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth();
        float itemY = getHeight();
        for (int index = 0; index < items.size; index++) {
            T item = items.get(index);
            if (selection.contains(item)) {
                batch.draw(getResources().getListSelectedBackground(), x, y + itemY - item_height, width, item_height);
            }
            getContext().getFontRenderer().setTextColor(Color.WHITE);
            getContext().getFontRenderer().drawText(batch, toString(item),
                    x + text_offset, y + itemY - item_height + text_offset + getResources().getTextFont().getCapHeight());
            itemY -= item_height;
        }
        super.draw(batch, parentAlpha);
    }

    private String toString(T item) {
        if (item instanceof FileHandle) {
            return ((FileHandle) item).name();
        } else {
            return item.toString();
        }
    }

    public interface SelectionListener {

        void onSelect(int index, Object value);

        void onChange(int index, Object value);

    }

}
