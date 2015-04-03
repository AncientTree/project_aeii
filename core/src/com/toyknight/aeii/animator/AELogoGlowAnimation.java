package com.toyknight.aeii.animator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.TextureManager;

/**
 * Created by toyknight on 4/3/2015.
 */
public class AELogoGlowAnimation extends Animator {

    private final TextureRegionDrawable ae_logo;
    private final TextureRegionDrawable ae_logo_mask;
    private final TextureRegionDrawable ae_logo_glow;
    private final ShapeRenderer shape_renderer;

    private float glow_x = 0f;

    public AELogoGlowAnimation() {
        Texture ae_logo_sheet = TextureManager.getAELogoTexture();
        TextureRegion ae_logo_texture =
                new TextureRegion(ae_logo_sheet, ae_logo_sheet.getWidth() - 240, ae_logo_sheet.getHeight() - 85, 240, 85);
        ae_logo = new TextureRegionDrawable(ae_logo_texture);

        TextureRegion ae_logo_mask_texture = new TextureRegion(TextureManager.getAELogoMaskTexture(), 0, 0, 240, 85);
        ae_logo_mask = new TextureRegionDrawable(ae_logo_mask_texture);

        TextureRegion ae_logo_glow_texture = new TextureRegion(TextureManager.getAELogoGlowTexture(), 0, 0, 67, 78);
        ae_logo_glow = new TextureRegionDrawable(ae_logo_glow_texture);

        shape_renderer = new ShapeRenderer();
        shape_renderer.setAutoShapeType(true);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        float x = (Gdx.app.getGraphics().getWidth() - 240) / 2.0f;
        float y = (Gdx.app.getGraphics().getHeight() - 85) / 2.0f;
        ae_logo.draw(batch, x, y, 240, 85);
        ae_logo_glow.draw(batch, glow_x, (Gdx.app.getGraphics().getHeight() - 78) / 2.0f, 67, 78);
        ae_logo_mask.draw(batch, x, y, 240, 85);
        batch.end();

        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        shape_renderer.rect(0, 0, (Gdx.app.getGraphics().getWidth() - 240) / 2, Gdx.app.getGraphics().getHeight());
        shape_renderer.rect((Gdx.app.getGraphics().getWidth() - 240) / 2 + 240, 0, (Gdx.app.getGraphics().getWidth() - 240) / 2, Gdx.app.getGraphics().getHeight());
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
