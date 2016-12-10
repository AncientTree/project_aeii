package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.GraphicsUtil;

/**
 * @author toyknight 5/17/2015.
 */
public class DustAriseAnimator extends MapAnimator {

    private Animation dust_animation;

    public DustAriseAnimator(int map_x, int map_y) {
        super(map_x, map_y);
        createAnimation();
    }

    public DustAriseAnimator(ObjectSet<Position> positions) {
        createAnimation();
        for (Position position : positions) {
            addLocation(position);
        }
    }

    private void createAnimation() {
        Texture texture_dust = AER.resources.getDustTexture();
        dust_animation = new Animation(1f / 15, GraphicsUtil.createFrames(texture_dust, 4, 1));
    }

    @Override
    public void render(Batch batch) {
        int dust_height = ts() * 20 / 24;
        for (Position position : getLocations()) {
            int dust_sx = getCanvas().getXOnScreen(position.x);
            int dust_sy = getCanvas().getYOnScreen(position.y);
            int offset = (int) (getStateTime() / (1f / 15)) * ts() / 6;
            batch.draw(dust_animation.getKeyFrame(
                    getStateTime()), dust_sx, dust_sy + ts() - dust_height + offset, ts(), dust_height);
            batch.flush();
        }
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
