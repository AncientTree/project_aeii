package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 4/19/2015.
 */
public class CursorAnimator extends MapAnimator {

    private final Animation cursor_animation;

    public CursorAnimator(GameContext context) {
        super(context);
        Texture cursor_texture = getResources().getNormalCursorTexture();
        this.cursor_animation = ResourceManager.createAnimation(cursor_texture, 2, 1, 0.3f);
    }

    public void render(SpriteBatch batch, int map_x, int map_y) {
        int size = ts() * 26 / 24;
        int dx = (ts() - size) / 2;
        int dy = (ts() - size) / 2;
        int screen_x = getCanvas().getXOnScreen(map_x);
        int screen_y = getCanvas().getYOnScreen(map_y);
        TextureRegion current_frame = cursor_animation.getKeyFrame(getStateTime(), true);
        batch.draw(current_frame, screen_x + dx, screen_y + dy, size, size);
        batch.flush();
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
