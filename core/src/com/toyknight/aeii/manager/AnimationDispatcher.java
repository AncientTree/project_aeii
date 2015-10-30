package com.toyknight.aeii.manager;

import com.toyknight.aeii.animator.Animator;

/**
 * @author toyknight 4/14/2015.
 */
public interface AnimationDispatcher {

    boolean canSubmitAnimation();

    void submitAnimation(Animator animation);

    void updateAnimation(float delta);

    Animator getCurrentAnimation();

    boolean isAnimating();

}
