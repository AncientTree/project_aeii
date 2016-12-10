package net.toyknight.aeii.gui.wiki;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.toyknight.aeii.gui.widgets.LabelButton;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/12/2016.
 */
public class ReferenceLabel extends LabelButton {

    public static final int TYPE_ABILITY = 0x1;
    public static final int TYPE_STATUS = 0x2;
    public static final int TYPE_UNIT = 0x3;

    private final int type;
    private final int value;

    public ReferenceLabel(int type, int value, Skin skin) {
        super("", skin);
        this.type = type;
        this.value = value;
        switch (type) {
            case TYPE_ABILITY:
                setText(String.format("[%s]", AER.lang.getAbilityName(value)));
                break;
            case TYPE_STATUS:
                setText(String.format("[%s]", AER.lang.getStatusName(value)));
                break;
            case TYPE_UNIT:
                setText(String.format("[%s]", AER.lang.getUnitName(value)));
                break;
        }
        setColor(162 / 256f, 215 / 256f, 245 / 256f, 1.0f);
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

}
