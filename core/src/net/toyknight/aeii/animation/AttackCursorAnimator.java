package net.toyknight.aeii.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.GraphicsUtil;

/**
 * @author toyknight 4/19/2015.
 */
public class AttackCursorAnimator extends MapAnimator {


    private final Animation attack_cursor_animation;

    public AttackCursorAnimator() {
        Texture attack_cursor_texture = AER.resources.getAttackCursorTexture();
        this.attack_cursor_animation = GraphicsUtil.createAnimation(attack_cursor_texture, 3, 1, 0.3f);
    }

    @Override
    public void render(Batch batch) {
        int map_x = getCanvas().getCursorMapX();
        int map_y = getCanvas().getCursorMapY();
        int width = ts() * 40 / 24;
        int height = ts() * 41 / 24;
        int dx = (ts() - width) / 2;
        int dy = (ts() - height) / 2;
        int screen_x = getCanvas().getXOnScreen(map_x);
        int screen_y = getCanvas().getYOnScreen(map_y);
        TextureRegion current_frame = attack_cursor_animation.getKeyFrame(getStateTime(), true);
        batch.draw(current_frame, screen_x + dx, screen_y + dy, width, height);
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
