package net.toyknight.aeii.screen.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.manager.MapEditor;
import net.toyknight.aeii.screen.widgets.AEIIContainer;
import net.toyknight.aeii.utils.TileFactory;

/**
 * @author toyknight 7/9/2015.
 */
public class TileSelector extends AEIIContainer {

    private final MapEditor editor;
    private final ObjectMap<Short, TileButton> buttons;

    public TileSelector(GameContext context, MapEditor editor) {
        super(context);
        this.editor = editor;
        this.buttons = new ObjectMap<Short, TileButton>();
        this.initComponents();
    }

    private void initComponents() {
        int index = 0;
        Table tile_table = new Table();
        tile_table.padBottom(ts / 4);
        for (short i = 0; i < TileFactory.getTileCount(); i++) {
            if ((0 <= i && i <= 2) || (15 <= i && i <= 45) || (80 <= i && i < TileFactory.getTileCount())) {
                TileButton t_btn = new TileButton(getContext(), getEditor(), i);
                switch (index % 3) {
                    case 0:
                    case 1:
                        tile_table.add(t_btn).padLeft(ts / 4).padTop(ts / 4);
                        break;
                    case 2:
                        tile_table.add(t_btn).padLeft(ts / 4).padRight(ts / 4).padTop(ts / 4).row();
                        break;
                }
                buttons.put(i, t_btn);
                index++;
            }
        }
        tile_table.layout();
        ScrollPane sp_tile_table = new ScrollPane(tile_table);
        sp_tile_table.setScrollBarPositions(false, true);
        sp_tile_table.setFadeScrollBars(false);
        sp_tile_table.setBounds(0, 0, ts * 4, Gdx.graphics.getHeight() - ts);
        setActor(sp_tile_table);
    }

    public MapEditor getEditor() {
        return editor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(getResources().getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
        getContext().getBorderRenderer().drawBorder(batch, getX(), getY(), getWidth(), getHeight());
    }

}
