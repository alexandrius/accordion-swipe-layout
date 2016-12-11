package com.alexandrius.accordionswipelayout.library;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by alex on 12/11/2016
 */

class Utils {
    static float getViewWeight(View view) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        return lp.weight;
    }

    static void setViewWeight(View view, float weight){
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.weight = weight;
        view.setLayoutParams(lp);
    }

    static void setViewWidth(View view, int width){
        view.getLayoutParams().width = width;
        view.requestLayout();
    }
}
