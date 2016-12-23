package com.alexandrius.accordionswipelayout.library;

import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * @author naik
 */
public class ViewUtils {

    public static Drawable setTint(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        return drawable.mutate();
    }
}
