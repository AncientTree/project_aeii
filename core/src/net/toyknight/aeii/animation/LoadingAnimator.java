package net.toyknight.aeii.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.utils.FileProvider;

/**
 * @author toyknight 4/2/2015.
 */
public class LoadingAnimator extends Animator {

    private final Animation animation;
    private final ShapeRenderer shape_renderer;

    public LoadingAnimator() {
        Texture texture_loading = new Texture(FileProvider.getAssetsFile("images/loading.png"));
        animation = ResourceManager.createAnimation(texture_loading, 18, 1, 0.065f);
        shape_renderer = new ShapeRenderer();
        shape_renderer.setAutoShapeType(true);
    }

    @Override
    public void render(Batch batch) {
        shape_renderer.begin();
        shape_renderer.set(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        shape_renderer.rect(0, 0, Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());
        shape_renderer.end();

        batch.begin();
        TextureRegion current_frame = animation.getKeyFrame(getStateTime(), true);
        int draw_x = (Gdx.app.getGraphics().getWidth() - 64 * ts / 48) / 2;
        int draw_y = (Gdx.app.getGraphics().getHeight() - 64 * ts / 48) / 2;
        batch.draw(current_frame, draw_x, draw_y, 64 * ts / 48, 64 * ts / 48);
        batch.end();
    }

    @Override
    public void update(float delta_time) {
        addStateTime(delta_time);
    }

    @Override
    public boolean isAnimationFinished() {
        return false;
    }

}
