package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 7/8/2016.
 */
public class AEIILabel extends Label {

    protected final int ts;

    private final GameContext context;

    public AEIILabel(GameContext context, CharSequence text, Skin skin) {
        super(text, skin);
        this.context = context;
        this.ts = context.getTileSize();
    }

    public GameContext getContext() {
        return context;
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

}
