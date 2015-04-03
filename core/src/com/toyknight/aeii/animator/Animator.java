package com.toyknight.aeii.animator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by toyknight on 4/2/2015.
 */
public abstract class Animator {

    private float state_time;

    public Animator() {
        state_time = 0f;
    }

    public void addStateTime(float time) {
        state_time += time;
    }

    public float getStateTime() {
        return state_time;
    }

    public void resize(int width, int height) {
    }

    abstract public void render(SpriteBatch batch);

    abstract public void update(float delta_time);

    abstract public boolean isAnimationFinished();

}
