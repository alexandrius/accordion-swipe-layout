package com.alexandrius.accordionswipelayout.library;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by Alexander Pataridze on 18/11/2016.
 */
class SwipeAnimation extends Animation {
    private int width;
    private int startWidth = -1;
    private View resizeView;
    private View changeXView;
    private boolean left;

    SwipeAnimation(View resizeView, int width, View changeXView, boolean left) {
        this.resizeView = resizeView;
        this.width = width;
        this.changeXView = changeXView;
        this.left = left;
        setDuration(300);
        setInterpolator(new DecelerateInterpolator());
    }


    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        if (startWidth < 0) {
            startWidth = resizeView.getWidth();
        }

        Utils.setViewWidth(resizeView, startWidth + (int) (((float) width - (float) startWidth) * interpolatedTime));

        if (left) {
            changeXView.setX(resizeView.getWidth());
        } else {
            changeXView.setX(-resizeView.getWidth());
        }

    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}