package com.alexandrius.accordionswipelayout.library;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by Alexander Pataridze on 18/11/2016.
 */
public class ResizeAndChangeXAnimation extends Animation {
    private int width;
    private int startWidth;
    private View resizeView;
    private View changeXView;
    private boolean left;

    public ResizeAndChangeXAnimation(View resizeView, int width, View changeXView, boolean left) {
        this.resizeView = resizeView;
        this.width = width;
        this.changeXView = changeXView;
        this.left = left;
        setDuration(300);
        setInterpolator(new DecelerateInterpolator());
    }

    private boolean initial = true;

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        if (initial) {
            startWidth = resizeView.getWidth();
            initial = false;
        }

        int reqWidth = startWidth + (int) (((float) width - (float) startWidth) * interpolatedTime);

        resizeView.getLayoutParams().width = reqWidth;
        resizeView.requestLayout();

        if (left) {
            changeXView.setX(resizeView.getWidth());
        } else {
            changeXView.setX(-resizeView.getWidth());
        }


//        Log.d("INFO", "startWidth = " + startWidth + " reqWidth = " + reqWidth + " Width = " + resizeView.getWidth() + " X = " + changeXView.getX());
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}