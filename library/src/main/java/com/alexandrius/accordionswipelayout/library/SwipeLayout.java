package com.alexandrius.accordionswipelayout.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by alex on 11/18/16.
 */


//TODO: consider using PercentRelative LAYOUT for optimisations
public class SwipeLayout extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

    private int layoutId;

    private int[] leftColors;
    private int[] leftIcons;
    private int[] rightColors;
    private int[] rightIcons;
    private String[] leftTexts, rightTexts;

    private int itemWidth;
    private int rightLayoutMaxWidth, leftLayoutMaxWidth;
    private View mainLayout;
    private LinearLayout rightLinear, leftLinear;
    private int iconSize;
    private float textSize;
    private int topMargin, bottomMargin;

    private View[] rightViews, leftViews;
    private OnSwipeItemClickListener onSwipeItemClickListener;

    private int textColor;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public void setOnSwipeItemClickListener(OnSwipeItemClickListener onSwipeItemClickListener) {
        this.onSwipeItemClickListener = onSwipeItemClickListener;
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) return;


        if (attrs != null) {
            setUpAttrs(attrs);
        }

        setUpView();
    }

    private void setUpView() {
        if (layoutId == -1) {
            throw new IllegalStateException("Layout id not defined");
        }

        compareArrays(leftColors, leftIcons);
        compareArrays(rightColors, rightIcons);

        mainLayout = LayoutInflater.from(getContext()).inflate(layoutId, null);
        addView(mainLayout);

        createItemLayouts();
        mainLayout.bringToFront();
        mainLayout.setOnTouchListener(this);
    }

    private void compareArrays(int[] arr1, int[] arr2) {
        if (arr1 != null && arr2 != null) {
            if (arr1.length < arr2.length) {
                throw new IllegalStateException("Drawable array shouldn't be bigger than color array");
            }
        }
    }


    private void createItemLayouts() {
        if (rightIcons != null) {
            rightLayoutMaxWidth = itemWidth * rightIcons.length;
            rightLinear = createLinearLayout(Gravity.RIGHT);
            addView(rightLinear);
            rightViews = new View[rightIcons.length];
            createAndAddSwipeItems(rightIcons, rightColors, rightTexts, rightLinear, rightViews);
        }

        if (leftIcons != null) {
            leftLayoutMaxWidth = itemWidth * leftIcons.length;
            leftLinear = createLinearLayout(Gravity.LEFT);
            addView(leftLinear);
            leftViews = new View[leftIcons.length];
            createAndAddSwipeItems(leftIcons, leftColors, leftTexts, leftLinear, leftViews);
        }
    }

    private void createAndAddSwipeItems(int[] icons, int[] backgroundColors, String[] texts, LinearLayout layout, View[] views) {

        for (int i = 0; i < icons.length; i++) {
            int backgroundColor = -1;
            if (backgroundColors != null) {
                backgroundColor = backgroundColors[i];
            }

            String txt = null;
            if (texts != null) txt = texts[i];

            ViewGroup swipeItem = createSwipeItem(icons[i], backgroundColor, txt);
            swipeItem.setClickable(true);
            swipeItem.setFocusable(true);
            swipeItem.setOnClickListener(this);

            views[i] = swipeItem;
            layout.addView(swipeItem);
        }
    }

    private Drawable getRippleDrawable() {
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        Drawable ripple = ta.getDrawable(0);
        ta.recycle();
        return ripple;
    }

    private ViewGroup createSwipeItem(int icon, int backgroundColor, String text) {
        FrameLayout frameLayout = new FrameLayout(getContext());

        //TODO: SWITCH TO HARDLY CALCULATED VALUES INSTEAD OF WEIGHTS FOR FUTURE OPTIMISATIONS
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        if (Build.VERSION.SDK_INT >= 16) {
            View view = new View(getContext());
            view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setBackground(getRippleDrawable());
            frameLayout.addView(view);
        }
        if (backgroundColor != -1) {
            frameLayout.setBackgroundColor(backgroundColor);
        }


        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(icon);

        LayoutParams imageViewParams = new LayoutParams(iconSize, iconSize, text == null ? Gravity.CENTER : Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        frameLayout.addView(imageView, imageViewParams);

        if (text != null) {
            TextView textView = new TextView(getContext());
            textView.setMaxLines(2);

            if (textSize > 0) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            if (textColor != -1) {
                textView.setTextColor(textColor);
            }
            textView.setText(text);
            textView.setGravity(Gravity.CENTER);
            LayoutParams textViewParams = new LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            frameLayout.addView(textView, textViewParams);

            textViewParams.bottomMargin = bottomMargin;
            imageViewParams.topMargin = topMargin;
        }


        return frameLayout;
    }

    private LinearLayout createLinearLayout(int gravity) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = gravity;
        linearLayout.setLayoutParams(params);
        return linearLayout;
    }

    private void setUpAttrs(AttributeSet attrs) {
        final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        if (array != null) {
            layoutId = array.getResourceId(R.styleable.SwipeLayout_layout, -1);
            itemWidth = array.getDimensionPixelSize(R.styleable.SwipeLayout_swipeItemWidth, 100);
            iconSize = array.getDimensionPixelSize(R.styleable.SwipeLayout_iconSize, ViewGroup.LayoutParams.MATCH_PARENT);
            textColor = array.getColor(R.styleable.SwipeLayout_textColor, -1);
            textSize = array.getDimensionPixelSize(R.styleable.SwipeLayout_textSize, -1);

            bottomMargin = array.getDimensionPixelSize(R.styleable.SwipeLayout_bottomMargin, 20);
            topMargin = array.getDimensionPixelSize(R.styleable.SwipeLayout_topMargin, 20);

            int rightColorsRes = array.getResourceId(R.styleable.SwipeLayout_rightItemColors, -1);
            int rightIconsRes = array.getResourceId(R.styleable.SwipeLayout_rightItemIcons, -1);

            int leftColorsRes = array.getResourceId(R.styleable.SwipeLayout_leftItemColors, -1);
            int leftIconsRes = array.getResourceId(R.styleable.SwipeLayout_leftItemIcons, -1);

            int leftTextRes = array.getResourceId(R.styleable.SwipeLayout_leftStrings, -1);
            int rightTextRes = array.getResourceId(R.styleable.SwipeLayout_rightStrings, -1);


            initiateArrays(rightColorsRes, rightIconsRes, leftColorsRes, leftIconsRes, leftTextRes, rightTextRes);
            array.recycle();
        }

    }

    private void initiateArrays(int rightColorsRes, int rightIconsRes, int leftColorsRes, int leftIconsRes, int leftTextRes, int rightTextRes) {
        if (rightColorsRes != -1)
            rightColors = getResources().getIntArray(rightColorsRes);

        if (rightIconsRes != -1)
            rightIcons = fillDrawables(getResources().obtainTypedArray(rightIconsRes));

        if (leftColorsRes != -1)
            leftColors = getResources().getIntArray(leftColorsRes);

        if (leftIconsRes != -1)
            leftIcons = fillDrawables(getResources().obtainTypedArray(leftIconsRes));

        if (leftTextRes != -1)
            leftTexts = getResources().getStringArray(leftTextRes);

        if (rightTextRes != -1)
            rightTexts = getResources().getStringArray(rightTextRes);
    }

    private int[] fillDrawables(TypedArray ta) {
        int[] drawableArr = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            drawableArr[i] = ta.getResourceId(i, -1);
        }
        ta.recycle();
        return drawableArr;
    }


    float prevX = -1;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:

                boolean directionLeft = prevX - event.getRawX() > 0;
                float delta = Math.abs(prevX - event.getRawX());

                int rightLayoutWidth;
                int leftLayoutWidth;

                if (directionLeft) {
                    float left = mainLayout.getX() - delta;

                    if (left < -rightLayoutMaxWidth) {
                        left = -rightLayoutMaxWidth;
                    }

                    mainLayout.setX(left);

                    if (rightLinear != null) {
                        rightLayoutWidth = (int) Math.abs(left - 1);
                        LayoutParams params = (LayoutParams) rightLinear.getLayoutParams();
                        params.width = rightLayoutWidth;
                        rightLinear.setLayoutParams(params);

                    }

                    if (leftLinear != null && left > 0) {
                        leftLayoutWidth = (int) Math.abs(mainLayout.getX() + 1);
                        LayoutParams params = (LayoutParams) leftLinear.getLayoutParams();
                        params.width = leftLayoutWidth;
                        leftLinear.setLayoutParams(params);
                    }


                } else {
                    float right = mainLayout.getX() + delta;
                    if (right > leftLayoutMaxWidth) {
                        right = leftLayoutMaxWidth;
                    }

                    mainLayout.setX(right);

                    if (leftLinear != null && right > 0) {
                        leftLayoutWidth = (int) Math.abs(right + 1);
                        LayoutParams params = (LayoutParams) leftLinear.getLayoutParams();
                        params.width = leftLayoutWidth;
                        leftLinear.setLayoutParams(params);
                    }

                    if (rightLinear != null) {
                        rightLayoutWidth = (int) Math.abs(mainLayout.getX() - 1);
                        LayoutParams params = (LayoutParams) rightLinear.getLayoutParams();
                        params.width = rightLayoutWidth;
                        rightLinear.setLayoutParams(params);

                    }
                }

//TODO: UNCOMMENT LINES FOR FUTURE OPTIMISATIONS
//                if (leftLinear != null)
//                    for (int i = 0; i < leftLinear.getChildCount(); i++) {
//                        View v = leftLinear.getChildAt(i);
//                        v.getLayoutParams().width = leftLayoutWidth / leftIcons.length;
//                    }
//
//
//                if (rightLinear != null)
//                    for (int i = 0; i < rightViews.length; i++) {
//                        View v = rightViews[i];
//                        v.getLayoutParams().width = rightLayoutWidth / rightIcons.length;
//                    }


                if (delta > itemWidth / 5)
                    getParent().requestDisallowInterceptTouchEvent(true);

                prevX = event.getRawX();

                break;

            case MotionEvent.ACTION_UP:

                finishSwipeAnimated();
                break;

            case MotionEvent.ACTION_CANCEL:

                finishSwipeAnimated();

                break;
        }
        return true;
    }

    private void finishSwipeAnimated() {
        getParent().requestDisallowInterceptTouchEvent(false);

        LinearLayout animateView;
        boolean left;
        int requiredWidth = 0;

        if (mainLayout.getX() > 0) {
            animateView = leftLinear;
            left = true;
            if (leftLinear != null)
                if (leftLinear.getWidth() >= leftLayoutMaxWidth / 2 && leftLinear.getWidth() != leftLayoutMaxWidth) {
                    requiredWidth = leftLayoutMaxWidth;
                }
        } else {
            left = false;
            animateView = rightLinear;
            if (rightLinear != null)
                if (rightLinear.getWidth() >= rightLayoutMaxWidth / 2 && rightLinear.getWidth() != rightLayoutMaxWidth) {
                    requiredWidth = rightLayoutMaxWidth;
                }
        }

        if (animateView != null) {
            ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(animateView, requiredWidth, mainLayout, left, left ? leftViews : rightViews);
            animateView.startAnimation(swipeAnim);
        }
    }

    public void closeItem() {
        if (leftLinear.getWidth() > 0) {
            ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(leftLinear, 0, mainLayout, true, leftViews);
            leftLinear.startAnimation(swipeAnim);
        } else if (rightLinear.getWidth() > 0) {
            ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(rightLinear, 0, mainLayout, false, rightViews);
            rightLinear.startAnimation(swipeAnim);
        }
    }

    @Override
    public void onClick(View view) {
        if (onSwipeItemClickListener != null) {
            for (int i = 0; i < leftViews.length; i++) {
                View v = leftViews[i];
                if (v == view) {
                    onSwipeItemClickListener.onSwipeItemClick(true, i);
                    return;
                }
            }
            for (int i = 0; i < rightViews.length; i++) {
                View v = rightViews[i];
                if (v == view) {
                    onSwipeItemClickListener.onSwipeItemClick(false, i);
                    break;
                }
            }
        }
    }

    public interface OnSwipeItemClickListener {
        void onSwipeItemClick(boolean left, int index);
    }
}
