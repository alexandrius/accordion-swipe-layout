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
    private View[] views;
    private int subViewWidth;

    public ResizeAndChangeXAnimation(View resizeView, int width, View changeXView, boolean left, View[] views) {
        this.resizeView = resizeView;
        this.width = width;
        startWidth = resizeView.getWidth();
        this.changeXView = changeXView;
        this.left = left;
        this.views = views;
        subViewWidth = views[0].getWidth();
        setInterpolator(new DecelerateInterpolator());
        setDuration(150);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        resizeView.getLayoutParams().width = startWidth + (int) (((float) width - (float) startWidth) * interpolatedTime);
        resizeView.requestLayout();


        if (left) {
            changeXView.setX(resizeView.getWidth());
        } else {
            changeXView.setX(-resizeView.getWidth());
        }

        //UNCOMMENT LINES FOR FUTURE ANIMATION OPTIMISATIONS
//        for (View v : views) {
//            v.getLayoutParams().width = subViewWidth + (int) (((float) resizeView.getWidth() / 3 - (float) subViewWidth) * interpolatedTime);
//            v.requestLayout();
//        }

    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}