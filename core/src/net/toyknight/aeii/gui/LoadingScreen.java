package net.toyknight.aeii.gui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.animation.Animator;
import net.toyknight.aeii.animation.BackgroundFadeAnimator;
import net.toyknight.aeii.animation.LoadingAnimator;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.Platform;

/**
 * @author toyknight 4/2/2015.
 */
public class LoadingScreen implements Screen {

    private final GameContext context;
    private final SpriteBatch batch;

    private Animator loading_animator;
    private Animator bg_fade_animator;

    public LoadingScreen(GameContext context) {
        this.context = context;
        this.batch = new SpriteBatch();

        loading_animator = new LoadingAnimator();

        bg_fade_animator = new BackgroundFadeAnimator(1.0f, 1.0f, 1.0f);
    }

    public GameContext getContext() {
        return context;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (getContext().getResources().update()) {
            if (getContext().initialized()) {
                if (bg_fade_animator.isAnimationFinished()) {
                    if (Language.getLocale().equals("zh_CN") && getContext().getPlatform() == Platform.Android) {
                        getContext().gotoMainMenuScreen(true, true);
                    } else {
                        getContext().gotoMainMenuScreen(true, false);
                    }
                } else {
                    bg_fade_animator.render(batch);
                    bg_fade_animator.update(delta);
                }
            } else {
                getContext().initialize();
            }
        } else {
            loading_animator.render(batch);
            loading_animator.update(delta);
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
