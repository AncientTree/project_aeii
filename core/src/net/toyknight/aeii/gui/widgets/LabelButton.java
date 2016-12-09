package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 6/14/2016.
 */
public class LabelButton extends AEIILabel {

    private final ClickListener click_listener;

    public LabelButton(GameContext context, CharSequence text) {
        super(context, text, context.getSkin());
        click_listener = new ClickListener();
        addListener(click_listener);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (click_listener.isVisualPressed()) {
            batch.draw(getResources().getBorderDarkColor(), getX(), getY(), getWidth(), getHeight());
            batch.flush();
        }
        super.draw(batch, parentAlpha);
    }


}
