package net.toyknight.aeii.gui.widgets;

import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 6/10/2016.
 */
public class NumberSpinner extends Spinner<Integer> {

    public NumberSpinner(GameContext context, int min_value, int max_value) {
        this(context, min_value, max_value, 1);
    }

    public NumberSpinner(GameContext context, int min_value, int max_value, int step) {
        super(context);
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
