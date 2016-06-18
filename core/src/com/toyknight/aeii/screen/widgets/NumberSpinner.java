package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * @author toyknight 6/10/2016.
 */
public class NumberSpinner extends Spinner<Integer> {

    public NumberSpinner(int min_value, int max_value, int ts, Skin skin) {
        this(min_value, max_value, 1, ts, skin);
    }

    public NumberSpinner(int min_value, int max_value, int step, int ts, Skin skin) {
        super(ts, skin);
        int length = (max_value - min_value) / step + 1;
        Integer[] values = new Integer[length];
        for (int i = 0; i < length; i++) {
            values[i] = min_value + step * i;
        }
        setItems(values);
    }

    public void select(int value) {
        for (int index = 0; index < getItems().length; index++) {
            if (value == getItems()[index]) {
                setSelectedIndex(index);
                break;
            }
        }
    }

}
