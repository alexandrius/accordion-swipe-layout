package com.alexandrius.accordionswipelayout.library;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by alex on 11/18/16.
 */


public class SwipeLayout extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

    private int layoutId;
    private int[] leftColors;
    private int[] leftIcons;
    private int[] rightColors;
    private int[] rightIcons;
    private int[] rightTextColors;
    private int[] leftTextColors;
    private String[] leftTexts, rightTexts;

    private int itemWidth;
    private int rightLayoutMaxWidth, leftLayoutMaxWidth;
    private View mainLayout;
    private LinearLayout rightLinear, leftLinear;
    private int iconSize;
    private float textSize;
    private int textTopMargin;

    private View[] rightViews, leftViews;
    private OnSwipeItemClickListener onSwipeItemClickListener;

    private static Typeface typeface;

    private boolean swipeEnabled = true;

    public static final int ITEM_STATE_LEFT_EXPAND = 0;
    public static final int ITEM_STATE_RIGHT_EXPAND = 1;
    public static final int ITEM_STATE_COLLAPSED = 2;

    private static final long ANIMATION_MIN_DURATION = 100;
    private static final long ANIMATION_MAX_DURATION = 300;

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
            createAndAddSwipeItems(rightIcons, rightColors, rightTexts, rightTextColors, rightLinear, rightViews);
        }

        if (leftIcons != null) {
            leftLayoutMaxWidth = itemWidth * leftIcons.length;
            leftLinear = createLinearLayout(Gravity.LEFT);
            addView(leftLinear);
            leftViews = new View[leftIcons.length];
            createAndAddSwipeItems(leftIcons, leftColors, leftTexts, leftTextColors, leftLinear, leftViews);
        }
    }

    private void createAndAddSwipeItems(int[] icons, int[] backgroundColors, String[] texts, int[] textColors, LinearLayout layout, View[] views) {

        for (int i = 0; i < icons.length; i++) {
            int backgroundColor = -1;
            if (backgroundColors != null) {
                backgroundColor = backgroundColors[i];
            }

            String txt = null;
            if (texts != null) txt = texts[i];

            int textColor = -1;
            if (textColors != null)
                textColor = textColors[i];

            ViewGroup swipeItem = createSwipeItem(icons[i], backgroundColor, txt, textColor);
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

    int id;

    private ViewGroup createSwipeItem(int icon, int backgroundColor, String text, int textColor) {
        FrameLayout frameLayout = new FrameLayout(getContext());
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

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setLayoutParams(new LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL));


        RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(iconSize, iconSize);
        imageViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        imageView.setLayoutParams(imageViewParams);
        imageView.setId(++id);
        relativeLayout.addView(imageView);

        if (text != null) {
            TextView textView = new TextView(getContext());
            textView.setMaxLines(2);

            if (textSize > 0) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            if (textColor != -1) {
                textView.setTextColor(textColor);
            }

            if (typeface != null) textView.setTypeface(typeface);

            textView.setText(text);
            textView.setGravity(Gravity.CENTER);
            RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            textViewParams.addRule(RelativeLayout.BELOW, id);
            textViewParams.topMargin = textTopMargin;
            relativeLayout.addView(textView, textViewParams);

        }

        frameLayout.addView(relativeLayout);

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

            textSize = array.getDimensionPixelSize(R.styleable.SwipeLayout_textSize, -1);

            textTopMargin = array.getDimensionPixelSize(R.styleable.SwipeLayout_textTopMargin, 20);


            int rightColorsRes = array.getResourceId(R.styleable.SwipeLayout_rightItemColors, -1);
            int rightIconsRes = array.getResourceId(R.styleable.SwipeLayout_rightItemIcons, -1);

            int leftColorsRes = array.getResourceId(R.styleable.SwipeLayout_leftItemColors, -1);
            int leftIconsRes = array.getResourceId(R.styleable.SwipeLayout_leftItemIcons, -1);

            int leftTextRes = array.getResourceId(R.styleable.SwipeLayout_leftStrings, -1);
            int rightTextRes = array.getResourceId(R.styleable.SwipeLayout_rightStrings, -1);

            int leftTextColorRes = array.getResourceId(R.styleable.SwipeLayout_leftTextColors, -1);
            int rightTextColorRes = array.getResourceId(R.styleable.SwipeLayout_rightTextColors, -1);

            String typefaceAssetPath = array.getString(R.styleable.SwipeLayout_customFont);
            if (typefaceAssetPath != null) {
                if (typeface == null) {
                    AssetManager assetManager = getContext().getAssets();
                    typeface = Typeface.createFromAsset(assetManager, typefaceAssetPath);
                }
            }


            initiateArrays(rightColorsRes, rightIconsRes, leftColorsRes, leftIconsRes, leftTextRes, rightTextRes, leftTextColorRes, rightTextColorRes);
            array.recycle();
        }

    }

    private void initiateArrays(int rightColorsRes, int rightIconsRes, int leftColorsRes, int leftIconsRes, int leftTextRes, int rightTextRes, int leftTextColorRes, int rightTextColorRes) {
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

        if (leftTextColorRes != -1)
            leftTextColors = getResources().getIntArray(leftTextColorRes);

        if (rightTextColorRes != -1)
            rightTextColors = getResources().getIntArray(rightTextColorRes);
    }

    private int[] fillDrawables(TypedArray ta) {
        int[] drawableArr = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            drawableArr[i] = ta.getResourceId(i, -1);
        }
        ta.recycle();
        return drawableArr;
    }


    float prevRawX = -1;
    boolean directionLeft;
    boolean movementStarted;
    long lastTime;
    long downTime;
    float speed;
    float downRawX;
    float downX, downY;

    private void clearAnimations() {
        mainLayout.clearAnimation();
        rightLinear.clearAnimation();
        leftLinear.clearAnimation();
    }


    boolean shouldPerformLongClick;
    boolean longClickPerformed;
    private Handler longClickHandler = new Handler();

    private Runnable longClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (shouldPerformLongClick) {
                if (performLongClick()) {
                    longClickPerformed = true;
                    setPressed(false);
                }
            }
        }
    };

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (Build.VERSION.SDK_INT >= 21)
            drawableHotspotChanged(downX, downY);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (swipeEnabled) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    downTime = lastTime = System.currentTimeMillis();
                    downRawX = prevRawX = event.getRawX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(prevRawX - event.getRawX()) < 20 && !movementStarted) {
                        if (System.currentTimeMillis() - lastTime >= 50 && !isPressed() && !isExpanded() && !longClickPerformed) {
                            setPressed(true);

                            if (!shouldPerformLongClick) {
                                shouldPerformLongClick = true;
                                longClickHandler.postDelayed(longClickRunnable, ViewConfiguration.getLongPressTimeout());
                            }
                        }

                        return false;
                    }

                    if (isPressed()) setPressed(false);

                    shouldPerformLongClick = false;
                    movementStarted = true;

                    clearAnimations();

                    directionLeft = prevRawX - event.getRawX() > 0;
                    float delta = Math.abs(prevRawX - event.getRawX());
                    speed = (System.currentTimeMillis() - lastTime) / delta;

                    int rightLayoutWidth = 0;
                    int leftLayoutWidth = 0;

                    if (directionLeft) {
                        float left = mainLayout.getX() - delta;

                        if (left < -rightLayoutMaxWidth) {
                            left = -rightLayoutMaxWidth;
                        }

                        mainLayout.setX(left);

                        if (rightLinear != null) {
                            rightLayoutWidth = (int) Math.abs(left);
                            LayoutParams params = (LayoutParams) rightLinear.getLayoutParams();
                            params.width = rightLayoutWidth;
                            rightLinear.setLayoutParams(params);

                        }

                        if (leftLinear != null && left > 0) {
                            leftLayoutWidth = (int) Math.abs(mainLayout.getX());
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
                            leftLayoutWidth = (int) Math.abs(right);
                            LayoutParams params = (LayoutParams) leftLinear.getLayoutParams();
                            params.width = leftLayoutWidth;
                            leftLinear.setLayoutParams(params);
                        }

                        if (rightLinear != null) {
                            rightLayoutWidth = (int) Math.abs(mainLayout.getX());
                            LayoutParams params = (LayoutParams) rightLinear.getLayoutParams();
                            params.width = rightLayoutWidth;
                            rightLinear.setLayoutParams(params);

                        }
                    }

                    if (Math.abs(mainLayout.getX()) > itemWidth / 5) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    prevRawX = event.getRawX();
                    lastTime = System.currentTimeMillis();
                    return true;

                case MotionEvent.ACTION_UP:
                    finishMotion(event);
                    if (movementStarted) {
                        finishSwipeAnimated();
                    } else {
                        movementStarted = false;
                        setPressed(false);
                        if (System.currentTimeMillis() - downTime < ViewConfiguration.getTapTimeout()) {
                            setPressed(true);
                            performClick();
                            setPressed(false);
                        }
                    }

                    return false;
                case MotionEvent.ACTION_CANCEL:
                    finishMotion(event);
                    finishSwipeAnimated();
                    return false;
            }

        }
        return false;
    }

    private void finishMotion(MotionEvent event) {
        directionLeft = event.getRawX() - downRawX < 0;
        longClickHandler.removeCallbacks(longClickRunnable);
        shouldPerformLongClick = false;
        longClickPerformed = false;
    }

    private void finishSwipeAnimated() {
        shouldPerformLongClick = false;
        setPressed(false);
        getParent().requestDisallowInterceptTouchEvent(false);
        movementStarted = false;

        LinearLayout animateView = null;
        boolean left = false;
        int requiredWidth = 0;

        if (mainLayout.getX() > 0) {
            animateView = leftLinear;
            left = true;
            if (leftLinear != null) {
                int reqWidth = directionLeft ? (leftLayoutMaxWidth - (leftLayoutMaxWidth / 3)) : leftLayoutMaxWidth / 3;

                if (leftLinear.getWidth() >= reqWidth) {
                    requiredWidth = leftLayoutMaxWidth;
                }
                mainLayout.setX(leftLinear.getWidth());
            }

        } else if (mainLayout.getX() < 0) {
            left = false;
            animateView = rightLinear;
            if (rightLinear != null) {
                int reqWidth = directionLeft ? rightLayoutMaxWidth / 3 : (rightLayoutMaxWidth - (rightLayoutMaxWidth / 3));
//                int reqWidth = rightLayoutMaxWidth / 3;

                if (rightLinear.getWidth() >= reqWidth) {
                    requiredWidth = rightLayoutMaxWidth;
                }
                mainLayout.setX(-rightLinear.getWidth());
            }
        }

        if (animateView != null && requiredWidth != Math.abs(mainLayout.getX())) {

            ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(animateView, requiredWidth, mainLayout, left);

            long duration = (long) (100 * speed);

            if (duration < ANIMATION_MIN_DURATION) duration = ANIMATION_MIN_DURATION;
            else if (duration > ANIMATION_MAX_DURATION) duration = ANIMATION_MAX_DURATION;
//
            swipeAnim.setDuration(duration);
            animateView.startAnimation(swipeAnim);

        }
    }

    /**
     * @deprecated use setItemState()
     */
    @Deprecated
    public void closeItem() {
        collapseItem(true);
    }

    private void collapseItem(boolean animated) {
        if (leftLinear.getWidth() > 0) {
            if (animated) {
                ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(leftLinear, 0, mainLayout, true);
                leftLinear.startAnimation(swipeAnim);
            } else {
                mainLayout.setX(0);
                ViewGroup.LayoutParams params = leftLinear.getLayoutParams();
                params.width = 0;
                leftLinear.setLayoutParams(params);
            }
        } else if (rightLinear.getWidth() > 0) {
            if (animated) {
                ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(rightLinear, 0, mainLayout, false);
                rightLinear.startAnimation(swipeAnim);
            } else {
                mainLayout.setX(0);
                ViewGroup.LayoutParams params = rightLinear.getLayoutParams();
                params.width = 0;
                rightLinear.setLayoutParams(params);
            }
        }
    }

    public void setItemState(int state, boolean animated) {
        switch (state) {
            case ITEM_STATE_COLLAPSED:
                collapseItem(animated);
                break;
            case ITEM_STATE_LEFT_EXPAND:
                int requiredWidthLeft = leftIcons.length * itemWidth;
                if (animated) {
                    ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(leftLinear, requiredWidthLeft, mainLayout, true);
                    leftLinear.startAnimation(swipeAnim);
                } else {
                    mainLayout.setX(requiredWidthLeft);
                    ViewGroup.LayoutParams params = leftLinear.getLayoutParams();
                    params.width = requiredWidthLeft;
                    leftLinear.setLayoutParams(params);
                }
                break;
            case ITEM_STATE_RIGHT_EXPAND:
                int requiredWidthRight = rightIcons.length * itemWidth;
                if (animated) {
                    ResizeAndChangeXAnimation swipeAnim = new ResizeAndChangeXAnimation(rightLinear, requiredWidthRight, mainLayout, false);
                    rightLinear.startAnimation(swipeAnim);
                } else {
                    mainLayout.setX(-requiredWidthRight);
                    ViewGroup.LayoutParams params = rightLinear.getLayoutParams();
                    params.width = requiredWidthRight;
                    rightLinear.setLayoutParams(params);
                }
                break;
        }

    }

    public void setSwipeEnabled(boolean enabled) {
        swipeEnabled = enabled;
    }


    public boolean isLeftExpanded() {
        return mainLayout.getX() > 0;
    }

    public boolean isRightExpanded() {
        return mainLayout.getX() < 0;
    }

    public boolean isExpanded() {
        return isRightExpanded() || isLeftExpanded();
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
