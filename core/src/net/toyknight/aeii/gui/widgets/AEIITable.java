package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 7/8/2016.
 */
public class AEIITable extends Table {

    protected final int ts;

    private final GameContext context;

    public AEIITable(GameContext context) {
        super();
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
