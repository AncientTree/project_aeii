package com.toyknight.aeii.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 4/3/2015.
 */
public class AELogoGlowAnimator extends Animator {

    private final int WIDTH = 240 * ts / 48;
    private final int HEIGHT = 85 * ts / 48;

    private final TextureRegionDrawable ae_logo;
    private final TextureRegionDrawable ae_logo_mask;
    private final TextureRegionDrawable ae_logo_glow;
    private final ShapeRenderer shape_renderer;

    private float glow_x = 0f;

    public AELogoGlowAnimator() {
        Texture ae_logo_sheet = ResourceManager.getAELogoTexture();
        TextureRegion ae_logo_texture =
                new TextureRegion(ae_logo_sheet, ae_logo_sheet.getWidth() - 240, ae_logo_sheet.getHeight() - 85, 240, 85);
        ae_logo = new TextureRegionDrawable(ae_logo_texture);

        TextureRegion ae_logo_mask_texture = new TextureRegion(ResourceManager.getAELogoMaskTexture(), 0, 0, 240, 85);
        ae_logo_mask = new TextureRegionDrawable(ae_logo_mask_texture);

        TextureRegion ae_logo_glow_texture = new TextureRegion(ResourceManager.getAELogoGlowTexture(), 0, 0, 67, 78);
        ae_logo_glow = new TextureRegionDrawable(ae_logo_glow_texture);

        shape_renderer = new ShapeRenderer();
        shape_renderer.setAutoShapeType(true);
    }

    @Override
    public void render(Batch batch) {
        batch.begin();
        int logo_x = (Gdx.app.getGraphics().getWidth() - WIDTH) / 2;
        int logo_y = Gdx.app.getGraphics().getHeight() - HEIGHT;
        ae_logo.draw(batch, logo_x, logo_y, WIDTH, HEIGHT);
        ae_logo_glow.draw(batch, glow_x, Gdx.app.getGraphics().getHeight() - 78 * ts / 48, 67 * ts / 48, 78 * ts / 48);
        ae_logo_mask.draw(batch, logo_x, logo_y, WIDTH, HEIGHT);
        batch.end();

        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        shape_renderer.rect(0, 0, (Gdx.app.getGraphics().getWidth() - WIDTH) / 2, Gdx.app.getGraphics().getHeight());
        shape_renderer.rect((Gdx.app.getGraphics().getWidth() - WIDTH) / 2 + WIDTH, 0, (Gdx.app.getGraphics().getWidth() - WIDTH) / 2, Gdx.app.getGraphics().getHeight());
        shape_renderer.end();
    }

    @Override
    public void update(float delta_time) {
        if (glow_x < Gdx.app.getGraphics().getWidth()) {
            glow_x += Gdx.app.getGraphics().getWidth() / (1.0f / delta_time) / 2;
        } else {
            glow_x = 0f;
        }
    }

    @Override
    public boolean isAnimationFinished() {
        return false;
    }

}
