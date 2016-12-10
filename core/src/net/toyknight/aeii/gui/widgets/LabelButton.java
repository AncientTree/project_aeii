package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/14/2016.
 */
public class LabelButton extends Label {

    private final ClickListener click_listener;

    public LabelButton(CharSequence text, Skin skin) {
        super(text, skin);
        click_listener = new ClickListener();
        addListener(click_listener);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (click_listener.isVisualPressed()) {
            batch.draw(AER.resources.getBorderDarkColor(), getX(), getY(), getWidth(), getHeight());
            batch.flush();
        }
        super.draw(batch, parentAlpha);
    }


}
