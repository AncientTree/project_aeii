package net.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 4/6/2015.
 */
public class BorderRenderer {

    private final GameContext context;

    private final int border_size;
    private TextureRegion[] borders;

    public BorderRenderer(GameContext context) {
        this.context = context;

        borders = new TextureRegion[8];
        Texture border_texture_sheet = getResources().getBorderTexture();
        int border_size = border_texture_sheet.getHeight();
        for (int i = 0; i < 8; i++) {
            borders[i] = new TextureRegion(border_texture_sheet, border_size * i, 0, border_size, border_size);
        }
        this.border_size = getResources().getBorderTexture().getHeight();
    }

    public GameContext getContext() {
        return context;
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

    public void drawBorder(Batch batch, float x, float y, float width, float height) {
        batch.draw(borders[0], x, y + height - border_size, border_size, border_size);
        batch.draw(borders[1], x + border_size, y + height - border_size, width - border_size * 2, border_size);
        batch.draw(borders[2], x + width - border_size, y + height - border_size, border_size, border_size);
        batch.draw(borders[3], x, y + border_size, border_size, height - border_size * 2);
        batch.draw(borders[4], x + width - border_size, y + border_size, border_size, height - border_size * 2);
        batch.draw(borders[5], x, y, border_size, border_size);
        batch.draw(borders[6], x + border_size, y, width - border_size * 2, border_size);
        batch.draw(borders[7], x + width - border_size, y, border_size, border_size);
        batch.flush();
    }

    public void drawTopBottomBorder(Batch batch, float x, float y, float width, float height) {
        batch.draw(borders[1], x, y + height - border_size, width, border_size);
        batch.draw(borders[6], x, y, width, border_size);
        batch.flush();
    }

}
