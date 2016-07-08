package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 7/8/2016.
 */
public class AEIIWidget extends Widget {

    protected final int ts;

    private final GameContext context;

    public AEIIWidget(GameContext context) {
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
