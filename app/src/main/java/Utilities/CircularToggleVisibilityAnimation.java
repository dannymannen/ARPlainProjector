package Utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by dat13aca on 11/04/2017.
 */

public class CircularToggleVisibilityAnimation {
    private View target;
    private boolean isVisible;

    public CircularToggleVisibilityAnimation(View target) {
        this.target = target;
    }

    public void show() {
        toggleVisibility(true);
    }

    public void hide() {
        toggleVisibility(false);
    }

    private void toggleVisibility(boolean visible) {
        if (isVisible != visible) {
            int cx = target.getWidth() / 2;
            int cy = target.getHeight() / 2;
            float radius = (float) Math.hypot(cx, cy);
            if (visible) {
                Animator anim = ViewAnimationUtils.createCircularReveal(target, cx, cy, 0, radius);
                target.setVisibility(View.VISIBLE);
                anim.setDuration(1000);
                anim.start();
            } else {
                Animator delayAnimator = ViewAnimationUtils.createCircularReveal(target, cx, cy, 0, 0);
                delayAnimator.setDuration(500);
                Animator revealAnimator =
                        ViewAnimationUtils.createCircularReveal(target, cx, cy, radius, 0);
                AnimatorSet set = new AnimatorSet();
                set.playSequentially(revealAnimator, delayAnimator);

                revealAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        target.setVisibility(View.INVISIBLE);
                    }

                });
                set.start();
            }
            isVisible = visible;
        }
    }
}
