package com.toyknight.aeii;

import com.toyknight.aeii.animator.Animator;
import com.toyknight.aeii.listener.AnimationListener;

/**
 * @author toyknight 4/14/2015.
 */
public interface AnimationDispatcher {

    void addAnimationListener(AnimationListener listener);

    void submitAnimation(Animator animation);

    void updateAnimation(float delta);

    Animator getCurrentAnimation();

    boolean isAnimating();

}
