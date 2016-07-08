package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;

/**
 * @author toyknight 5/17/2015.
 */
public class DustAriseAnimator extends MapAnimator {

    private final int map_x;
    private final int map_y;

    private final Animation dust_animation;

    public DustAriseAnimator(GameContext context, int map_x, int map_y) {
        super(context, map_x, map_y);
        Texture texture_dust = getResources().getDustTexture();
        this.dust_animation = new Animation(1f / 15, ResourceManager.createFrames(texture_dust, 4, 1));
        this.map_x = map_x;
        this.map_y = map_y;
    }

    @Override
    public void render(Batch batch) {
        int dust_height = ts() * 20 / 24;
        int dust_sx = getCanvas().getXOnScreen(map_x);
        int dust_sy = getCanvas().getYOnScreen(map_y);
        int offset = (int) (getStateTime() / (1f / 15)) * ts() / 6;
        batch.draw(dust_animation.getKeyFrame(
                getStateTime()), dust_sx, dust_sy + ts() - dust_height + offset, ts(), dust_height);
        batch.flush();
    }

    @Override
    public void update(float delta) {
        addStateTime(delta);
    }

    @Override
    public boolean isAnimationFinished() {
        return getStateTime() >= 1f / 15 * 3;
    }

}
