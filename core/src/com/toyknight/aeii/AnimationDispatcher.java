package com.toyknight.aeii;

import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.listener.AnimationListener;

/**
 * Created by toyknight on 4/14/2015.
 */
public interface AnimationDispatcher {

    public void addAnimationListener(AnimationListener listener);

    public void submitAnimation(Animator animation);

    public void updateAnimation(float delta);

    public Animator getCurrentAnimation();

}
