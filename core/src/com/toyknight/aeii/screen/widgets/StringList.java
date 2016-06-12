package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.FontRenderer;

/**
 * @author toyknight 8/25/2015.
 */
public class StringList<T> extends Widget {

    protected final int item_height;
    protected final float text_offset;
    private float prefWidth;
    private float prefHeight;

    protected final Array<T> items = new Array<T>();
    final ArraySelection<T> selection = new ArraySelection<T>(items);

    private SelectionListener listener;

    public StringList(int item_height) {
        this.item_height = item_height;
        this.text_offset = (item_height - ResourceManager.getTextFont().getCapHeight()) / 2;
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && button != 0) return false;
                if (selection.isDisabled()) return false;
                StringList.this.touchDown(y);
                return true;
            }
        });
    }

    private void touchDown(float y) {
        if (items.size == 0) return;
        float height = getHeight();
        int index = (int) ((height - y) / item_height);
        index = Math.max(0, index);
        index = Math.min(items.size - 1, index);
        if (!selection.contains(items.get(index))) {
            selection.choose(items.get(index));
            if (listener != null) {
                listener.onSelect(index, getSelected());
            }
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

    public T getSelected() {
        return selection.first();
    }

    public void setItems(Array<? extends T> items) {
        if (items == null) throw new IllegalArgumentException("list items cannot be null.");
        this.items.clear();
        this.items.addAll(items);
        updateList();
    }

    public void updateList() {
        float max_width = 0;
        for (T item : items) {
            float width = text_offset * 2 + FontRenderer.getTextLayout(item.toString()).width;
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
                batch.draw(ResourceManager.getListSelectedBackground(), x, y + itemY - item_height, width, item_height);
            }
            FontRenderer.setTextColor(Color.WHITE);
            FontRenderer.drawText(batch, toString(item),
                    x + text_offset, y + itemY - item_height + text_offset + ResourceManager.getTextFont().getCapHeight());
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

    }

}
