package net.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.toyknight.aeii.utils.Language;

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
                setText(String.format("[%s]", Language.getAbilityName(value)));
                break;
            case TYPE_STATUS:
                setText(String.format("[%s]", Language.getStatusName(value)));
                break;
            case TYPE_UNIT:
                setText(String.format("[%s]", Language.getUnitName(value)));
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
