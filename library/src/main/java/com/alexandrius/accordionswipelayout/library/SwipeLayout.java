package com.alexandrius.accordionswipelayout.library;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.alexandrius.accordionswipelayout.library.Utils.getViewWeight;


/**
 * @author alex, naik
 */
public class SwipeLayout extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

    private static final String TAG = "SwipeLayout";

    private static final int NO_ID = 0;

    private int layoutId;
    private int[] leftColors;
    private int[] leftIcons;
    private int[] leftIconColors;
    private int[] rightColors;
    private int[] rightIcons;
    private int[] rightIconColors;
    private int[] rightTextColors;

    public void setLeftColors(int[] leftColors) {
        this.leftColors = leftColors;
    }

    private int[] leftTextColors;
    private String[] leftTexts, rightTexts;

    private int itemWidth;
    private int rightLayoutMaxWidth, leftLayoutMaxWidth;
    private View mainLayout;
    private LinearLayout rightLinear, leftLinear, rightLinearWithoutLast, leftLinearWithoutFirst;
    private int iconSize;
    private float textSize;
    private int textTopMargin;
    private int fullSwipeEdgePadding;
    private View[] rightViews, leftViews;
    private OnSwipeItemClickListener onSwipeItemClickListener;

    private static Typeface typeface;

    private boolean swipeEnabled = true;
    private boolean canFullSwipeFromRight, canFullSwipeFromLeft;
    private boolean autoHideSwipe = true;
    private boolean onlyOneSwipe = true;

    public static final int ITEM_STATE_LEFT_EXPAND = 0;
    public static final int ITEM_STATE_RIGHT_EXPAND = 1;
    public static final int ITEM_STATE_COLLAPSED = 2;

    private static final long ANIMATION_MIN_DURATION = 100;
    private static final long ANIMATION_MAX_DURATION = 300;
    private RecyclerView.OnScrollListener onScrollListener;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public void setOnSwipeItemClickListener(OnSwipeItemClickListener onSwipeItemClickListener) {
        this.onSwipeItemClickListener = onSwipeItemClickListener;
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        fullSwipeEdgePadding = getResources().getDimensionPixelSize(R.dimen.full_swipe_edge_padding);

        if (attrs != null) {
            setUpAttrs(attrs);
        }
        setUpView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAutoHideSwipe(autoHideSwipe);
        setOnlyOneSwipe(onlyOneSwipe);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mainLayout != null) super.addView(child, index, params);
        else {
            mainLayout = child;
            setUpView();
        }
    }

    private void setUpView() {
        if (layoutId != -NO_ID) {
            mainLayout = LayoutInflater.from(getContext()).inflate(layoutId, null);
        }
        if (mainLayout != null) {
            compareArrays(leftColors, leftIcons);
            compareArrays(rightColors, rightIcons);
            compareArrays(leftIconColors, leftIcons);
            compareArrays(rightIconColors, rightIcons);


            addView(mainLayout);

            createItemLayouts();
            mainLayout.bringToFront();
            mainLayout.setOnTouchListener(this);
        }
    }

    private void compareArrays(int[] arr1, int[] arr2) {
        if (arr1 != null && arr2 != null) {
            if (arr1.length < arr2.length) {
                throw new IllegalStateException("Drawable array shouldn't be bigger than color array");
            }
        }
    }

    public void invalidateSwipeItems() {
        createItemLayouts();
    }

    private void createItemLayouts() {
        if (rightIcons != null) {
            rightLayoutMaxWidth = itemWidth * rightIcons.length;
            if (rightLinear != null) removeView(rightLinear);
            rightLinear = createLinearLayout(Gravity.RIGHT);
            rightLinearWithoutLast = createLinearLayout(Gravity.RIGHT);
            rightLinearWithoutLast.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, rightIcons.length - 1));
            addView(rightLinear);
            rightViews = new View[rightIcons.length];
            rightLinear.addView(rightLinearWithoutLast);
            addSwipeItems(rightIcons, rightIconColors, rightColors, rightTexts, rightTextColors, rightLinear, rightLinearWithoutLast, rightViews, false);
        }

        if (leftIcons != null) {
            leftLayoutMaxWidth = itemWidth * leftIcons.length;
            if (leftLinear != null) removeView(leftLinear);
            leftLinear = createLinearLayout(Gravity.LEFT);
            leftLinearWithoutFirst = createLinearLayout(Gravity.LEFT);
            leftLinearWithoutFirst.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, leftIcons.length - 1));
            leftViews = new View[leftIcons.length];
            addView(leftLinear);
            addSwipeItems(leftIcons, leftIconColors, leftColors, leftTexts, leftTextColors, leftLinear, leftLinearWithoutFirst, leftViews, true);
            leftLinear.addView(leftLinearWithoutFirst);
        }
    }

    private void addSwipeItems(int[] icons, int[] iconColors, int[] backgroundColors, String[] texts, int[] textColors,
                               LinearLayout layout, LinearLayout layoutWithout, View[] views, boolean left) {

        for (int i = 0; i < icons.length; i++) {
            int backgroundColor = NO_ID;
            if (backgroundColors != null) {
                backgroundColor = backgroundColors[i];
            }

            int iconColor = NO_ID;
            if (iconColors != null) iconColor = iconColors[i];

            String txt = null;
            if (texts != null) txt = texts[i];

            int textColor = NO_ID;
            if (textColors != null)
                textColor = textColors[i];


            ViewGroup swipeItem = createSwipeItem(icons[i], iconColor, backgroundColor, txt, textColor);
            swipeItem.setClickable(true);
            swipeItem.setFocusable(true);
            swipeItem.setOnClickListener(this);

            views[i] = swipeItem;

            if (i == icons.length - (!left ? 1 : icons.length)) {
                layout.addView(swipeItem);
            } else {
                layoutWithout.addView(swipeItem);
            }
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mainLayout.setOnClickListener(l);
    }

    private Drawable getRippleDrawable() {
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        Drawable ripple = ta.getDrawable(0);
        ta.recycle();
        return ripple;
    }

    int id;

    private ViewGroup createSwipeItem(int icon, int iconColor, int backgroundColor, String text, int textColor) {
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        if (Build.VERSION.SDK_INT >= 16) {
            View view = new View(getContext());
            view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setBackground(getRippleDrawable());
            frameLayout.addView(view);
        }
        if (backgroundColor != NO_ID) {
            frameLayout.setBackgroundColor(backgroundColor);
        }


        ImageView imageView = new ImageView(getContext());
        Drawable drawable = ContextCompat.getDrawable(getContext(), icon);
        if (iconColor != NO_ID) {
            drawable = ViewUtils.setTint(drawable, iconColor);
        }
        imageView.setImageDrawable(drawable);

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

            if (textColor != NO_ID) {
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
        frameLayout.setOnTouchListener(this);
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
            layoutId = array.getResourceId(R.styleable.SwipeLayout_layout, NO_ID);
            itemWidth = array.getDimensionPixelSize(R.styleable.SwipeLayout_swipeItemWidth, 100);
            iconSize = array.getDimensionPixelSize(R.styleable.SwipeLayout_iconSize, ViewGroup.LayoutParams.MATCH_PARENT);
            textSize = array.getDimensionPixelSize(R.styleable.SwipeLayout_textSize, NO_ID);
            textTopMargin = array.getDimensionPixelSize(R.styleable.SwipeLayout_textTopMargin, 20);
            canFullSwipeFromRight = array.getBoolean(R.styleable.SwipeLayout_canFullSwipeFromRight, false);
            canFullSwipeFromLeft = array.getBoolean(R.styleable.SwipeLayout_canFullSwipeFromLeft, false);
            onlyOneSwipe = array.getBoolean(R.styleable.SwipeLayout_onlyOneSwipe, true);
            autoHideSwipe = array.getBoolean(R.styleable.SwipeLayout_autoHideSwipe, true);

            int rightColorsRes = array.getResourceId(R.styleable.SwipeLayout_rightItemColors, NO_ID);
            int rightIconsRes = array.getResourceId(R.styleable.SwipeLayout_rightItemIcons, NO_ID);

            int leftColorsRes = array.getResourceId(R.styleable.SwipeLayout_leftItemColors, NO_ID);
            int leftIconsRes = array.getResourceId(R.styleable.SwipeLayout_leftItemIcons, NO_ID);

            int leftTextRes = array.getResourceId(R.styleable.SwipeLayout_leftStrings, NO_ID);
            int rightTextRes = array.getResourceId(R.styleable.SwipeLayout_rightStrings, NO_ID);

            int leftTextColorRes = array.getResourceId(R.styleable.SwipeLayout_leftTextColors, NO_ID);
            int rightTextColorRes = array.getResourceId(R.styleable.SwipeLayout_rightTextColors, NO_ID);

            int leftIconColors = array.getResourceId(R.styleable.SwipeLayout_leftIconColors, NO_ID);
            int rightIconColors = array.getResourceId(R.styleable.SwipeLayout_rightIconColors, NO_ID);


            String typefaceAssetPath = array.getString(R.styleable.SwipeLayout_customFont);
            if (typefaceAssetPath != null) {
                if (typeface == null) {
                    AssetManager assetManager = getContext().getAssets();
                    typeface = Typeface.createFromAsset(assetManager, typefaceAssetPath);
                }
            }


            initiateArrays(rightColorsRes, rightIconsRes, leftColorsRes, leftIconsRes,
                    leftTextRes, rightTextRes, leftTextColorRes, rightTextColorRes, leftIconColors, rightIconColors);
            array.recycle();
        }

    }

    private void initiateArrays(int rightColorsRes, int rightIconsRes, int leftColorsRes, int leftIconsRes,
                                int leftTextRes, int rightTextRes, int leftTextColorRes, int rightTextColorRes,
                                int leftIconColorsRes, int rightIconColorsRes) {
        Resources res = getResources();

        if (rightColorsRes != NO_ID) rightColors = res.getIntArray(rightColorsRes);
        if (rightIconsRes != NO_ID && !isInEditMode())
            rightIcons = fillDrawables(res.obtainTypedArray(rightIconsRes));
        if (leftColorsRes != NO_ID) leftColors = res.getIntArray(leftColorsRes);
        if (leftIconsRes != NO_ID && !isInEditMode())
            leftIcons = fillDrawables(res.obtainTypedArray(leftIconsRes));
        if (leftTextRes != NO_ID) leftTexts = res.getStringArray(leftTextRes);
        if (rightTextRes != NO_ID) rightTexts = res.getStringArray(rightTextRes);
        if (leftTextColorRes != NO_ID) leftTextColors = res.getIntArray(leftTextColorRes);
        if (rightTextColorRes != NO_ID) rightTextColors = res.getIntArray(rightTextColorRes);
        if (leftIconColorsRes != NO_ID) leftIconColors = res.getIntArray(leftIconColorsRes);
        if (rightIconColorsRes != NO_ID) rightIconColors = res.getIntArray(rightIconColorsRes);
    }

    public void setLeftIcons(int[] leftIcons) {
        this.leftIcons = leftIcons;
    }

    public void setLeftIconColors(int[] leftIconColors) {
        this.leftIconColors = leftIconColors;
    }

    public void setRightColors(int[] rightColors) {
        this.rightColors = rightColors;
    }

    public void setRightIcons(int[] rightIcons) {
        this.rightIcons = rightIcons;
    }

    public void setRightIconColors(int[] rightIconColors) {
        this.rightIconColors = rightIconColors;
    }

    public void setRightTextColors(int[] rightTextColors) {
        this.rightTextColors = rightTextColors;
    }

    public void setLeftTextColors(int[] leftTextColors) {
        this.leftTextColors = leftTextColors;
    }

    public void setLeftTexts(String[] leftTexts) {
        this.leftTexts = leftTexts;
    }

    public void setRightTexts(String[] rightTexts) {
        this.rightTexts = rightTexts;
    }

    private int[] fillDrawables(TypedArray ta) {
        int[] drawableArr = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            drawableArr[i] = ta.getResourceId(i, NO_ID);
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

        if (rightLinear != null)
            rightLinear.clearAnimation();

        if (leftLinear != null)
            leftLinear.clearAnimation();

        if (rightLinearWithoutLast != null)
            rightLinearWithoutLast.clearAnimation();

        if (leftLinearWithoutFirst != null)
            leftLinearWithoutFirst.clearAnimation();
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

    private View[] getCollapsibleViews() {
        return invokedFromLeft ? leftViews : rightViews;
    }

    private Animation.AnimationListener collapseListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            clickBySwipe();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    private void clickBySwipe() {
        if (onSwipeItemClickListener != null) {
            onSwipeItemClickListener.onSwipeItemClick(invokedFromLeft, invokedFromLeft ? 0 : rightIcons.length - 1);
        }
    }

    //Set LayoutWithout to weight 0
    private WeightAnimation collapseAnim;
    //Set LayoutWithout to weight rightIcons.length - 1
    private WeightAnimation expandAnim;


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (swipeEnabled && (leftIcons != null || rightIcons != null)) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    downTime = lastTime = System.currentTimeMillis();
                    downRawX = prevRawX = event.getRawX();

                    if (ViewCompat.getTranslationX(mainLayout) == 0) {
                        if (rightLinearWithoutLast != null) {
                            Utils.setViewWeight(rightLinearWithoutLast, rightViews.length - 1);
                        }
                        if (leftLinearWithoutFirst != null) {
                            Utils.setViewWeight(leftLinearWithoutFirst, leftViews.length - 1);
                        }
                    }

                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(prevRawX - event.getRawX()) < 20 && !movementStarted) {
                        if (System.currentTimeMillis() - lastTime >= 50 && !isPressed() && !isExpanding() && !longClickPerformed) {
                            view.setPressed(true);

                            if (!shouldPerformLongClick) {
                                shouldPerformLongClick = true;
                                longClickHandler.postDelayed(longClickRunnable, ViewConfiguration.getLongPressTimeout());
                            }
                        }

                        return false;
                    }

                    if (isPressed()) view.setPressed(false);

                    shouldPerformLongClick = false;
                    movementStarted = true;
                    collapseOthersIfNeeded();

                    clearAnimations();

                    directionLeft = prevRawX - event.getRawX() > 0;
                    float delta = Math.abs(prevRawX - event.getRawX());
                    speed = (System.currentTimeMillis() - lastTime) / delta;

                    int rightLayoutWidth = 0;
                    int leftLayoutWidth = 0;

                    if (directionLeft) {
                        float left = ViewCompat.getTranslationX(mainLayout) - delta;

                        if (left < -rightLayoutMaxWidth) {
                            if (!canFullSwipeFromRight) {
                                left = -rightLayoutMaxWidth;
                            } else if (left < -getWidth()) {
                                left = -getWidth();
                            }
                        }

                        if (canFullSwipeFromRight) {
                            if (ViewCompat.getTranslationX(mainLayout) <= -(getWidth() - fullSwipeEdgePadding)) {
                                if (getViewWeight(rightLinearWithoutLast) > 0 &&
                                        (collapseAnim == null || collapseAnim.hasEnded())) {

                                    view.setPressed(false);
                                    rightLinearWithoutLast.clearAnimation();

                                    if (expandAnim != null) expandAnim = null;

                                    collapseAnim = new WeightAnimation(0, rightLinearWithoutLast);
                                    Log.d("WeightAnim", "onTouch - Collapse");
                                    startAnimation(collapseAnim);
                                }
                            } else {
                                if (getViewWeight(rightLinearWithoutLast) < rightIcons.length - 1F &&
                                        (expandAnim == null || expandAnim.hasEnded())) {

                                    Log.d("WeightAnim", "onTouch - Expand");

                                    view.setPressed(false);
                                    rightLinearWithoutLast.clearAnimation();

                                    if (collapseAnim != null) collapseAnim = null;

                                    expandAnim = new WeightAnimation(rightIcons.length - 1, rightLinearWithoutLast);
                                    startAnimation(expandAnim);
                                }
                            }
                        }

                        ViewCompat.setTranslationX(mainLayout, left);

                        if (rightLinear != null) {
                            rightLayoutWidth = (int) Math.abs(left);
                            LayoutParams params = (LayoutParams) rightLinear.getLayoutParams();
                            params.width = rightLayoutWidth;
                            rightLinear.setLayoutParams(params);

                        }

                        if (leftLinear != null && left > 0) {
                            leftLayoutWidth = (int) Math.abs(ViewCompat.getTranslationX(mainLayout));
                            LayoutParams params = (LayoutParams) leftLinear.getLayoutParams();
                            params.width = leftLayoutWidth;
                            leftLinear.setLayoutParams(params);
                        }

                    } else {
                        float right = ViewCompat.getTranslationX(mainLayout) + delta;

                        if (right > leftLayoutMaxWidth) {
                            if (!canFullSwipeFromLeft) {
                                right = leftLayoutMaxWidth;
                            } else if (right >= getWidth()) {
                                right = getWidth();
                            }
                        }

                        if (canFullSwipeFromLeft) {
                            if (ViewCompat.getTranslationX(mainLayout) >= getWidth() - fullSwipeEdgePadding) {
                                if (getViewWeight(leftLinearWithoutFirst) > 0 &&
                                        (collapseAnim == null || collapseAnim.hasEnded())) {

                                    leftLinearWithoutFirst.clearAnimation();

                                    if (expandAnim != null) expandAnim = null;

                                    collapseAnim = new WeightAnimation(0, leftLinearWithoutFirst);

                                    startAnimation(collapseAnim);
                                }
                            } else {
                                if (getViewWeight(leftLinearWithoutFirst) < leftIcons.length - 1F &&
                                        (expandAnim == null || expandAnim.hasEnded())) {

                                    leftLinearWithoutFirst.clearAnimation();

                                    if (collapseAnim != null) collapseAnim = null;

                                    expandAnim = new WeightAnimation(leftIcons.length - 1, leftLinearWithoutFirst);

                                    startAnimation(expandAnim);
                                }
                            }
                        }

                        ViewCompat.setTranslationX(mainLayout, right);

                        if (leftLinear != null && right > 0) {
                            leftLayoutWidth = (int) Math.abs(right);
                            LayoutParams params = (LayoutParams) leftLinear.getLayoutParams();
                            params.width = leftLayoutWidth;
                            leftLinear.setLayoutParams(params);
                        }

                        if (rightLinear != null) {
                            rightLayoutWidth = (int) Math.abs(ViewCompat.getTranslationX(mainLayout));
                            LayoutParams params = (LayoutParams) rightLinear.getLayoutParams();
                            params.width = rightLayoutWidth;
                            rightLinear.setLayoutParams(params);
                        }
                    }

                    if (Math.abs(ViewCompat.getTranslationX(mainLayout)) > itemWidth / 5) {
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
                        view.setPressed(false);
                        if (System.currentTimeMillis() - downTime < ViewConfiguration.getTapTimeout()) {
                            view.setPressed(true);
                            view.performClick();
                            view.setPressed(false);
                        }
                    }

                    return false;
                case MotionEvent.ACTION_CANCEL:
                    finishMotion(event);
                    if (movementStarted)
                        finishSwipeAnimated();
                    return false;
            }

        }
        return false;
    }

    private void collapseOthersIfNeeded() {
        if (!onlyOneSwipe) return;
        ViewParent parent = getParent();
        if (parent != null && parent instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) parent;
            int count = recyclerView.getChildCount();
            for (int i = 0; i < count; i++) {
                View item = recyclerView.getChildAt(i);
                if (item != this && item instanceof SwipeLayout) {
                    SwipeLayout swipeLayout = (SwipeLayout) item;
                    if (ViewCompat.getTranslationX(swipeLayout.getSwipeableView()) != 0 && !swipeLayout.inAnimatedState()) {
                        swipeLayout.setItemState(ITEM_STATE_COLLAPSED, true);
                    }
                }
            }
        }
    }

    public View getSwipeableView() {
        return mainLayout;
    }

    private void finishMotion(MotionEvent event) {
        directionLeft = event.getRawX() - downRawX < 0;

        longClickHandler.removeCallbacks(longClickRunnable);
        shouldPerformLongClick = false;
        longClickPerformed = false;
    }

    boolean invokedFromLeft;

    private void finishSwipeAnimated() {
        shouldPerformLongClick = false;
        setPressed(false);
        getParent().requestDisallowInterceptTouchEvent(false);
        movementStarted = false;
        LinearLayout animateView = null;
        boolean left = false;
        int requiredWidth = 0;

        if (ViewCompat.getTranslationX(mainLayout) > 0) {
            animateView = leftLinear;
            left = true;
            if (leftLinear != null) {
                int reqWidth = directionLeft ? (leftLayoutMaxWidth - (leftLayoutMaxWidth / 3)) : leftLayoutMaxWidth / 3;

                if (rightLinear != null) Utils.setViewWidth(rightLinear, 0);

                if (leftLinear.getWidth() >= reqWidth) {
                    requiredWidth = leftLayoutMaxWidth;
                }

                if (requiredWidth == leftLayoutMaxWidth && !directionLeft) {
                    if (ViewCompat.getTranslationX(mainLayout) >= (getWidth() - fullSwipeEdgePadding)) {
                        requiredWidth = getWidth();
                        invokedFromLeft = true;
                    }
                }

                ViewCompat.setTranslationX(mainLayout, leftLinear.getWidth());
            }

        } else if (ViewCompat.getTranslationX(mainLayout) < 0) {
            left = false;
            animateView = rightLinear;
            if (rightLinear != null) {

                if (leftLinear != null) Utils.setViewWidth(leftLinear, 0);

                int reqWidth = directionLeft ? rightLayoutMaxWidth / 3 : (rightLayoutMaxWidth - (rightLayoutMaxWidth / 3));

                if (rightLinear.getWidth() >= reqWidth) {
                    requiredWidth = rightLayoutMaxWidth;
                }

                if (requiredWidth == rightLayoutMaxWidth && directionLeft) {
                    if (ViewCompat.getTranslationX(mainLayout) <= -(getWidth() - fullSwipeEdgePadding)) {
                        requiredWidth = getWidth();
                        invokedFromLeft = false;
                    }
                }

                ViewCompat.setTranslationX(mainLayout, -rightLinear.getWidth());
            }
        }
        long duration = (long) (100 * speed);

        if (animateView != null) {
            SwipeAnimation swipeAnim = new SwipeAnimation(animateView, requiredWidth, mainLayout, left);

            if (duration < ANIMATION_MIN_DURATION) duration = ANIMATION_MIN_DURATION;
            else if (duration > ANIMATION_MAX_DURATION) duration = ANIMATION_MAX_DURATION;
            swipeAnim.setDuration(duration);

            LinearLayout layoutWithout = animateView == leftLinear ? leftLinearWithoutFirst : rightLinearWithoutLast;
            View[] views = animateView == leftLinear ? leftViews : rightViews;
            invokedFromLeft = animateView == leftLinear;

            if (requiredWidth == getWidth()) {
                if (getViewWeight(layoutWithout) == 0 && getWidth() != Math.abs(ViewCompat.getTranslationX(mainLayout)))
                    swipeAnim.setAnimationListener(collapseListener);
                else if (collapseAnim != null && !collapseAnim.hasEnded()) {
                    collapseAnim.setAnimationListener(collapseListener);
                } else if (getViewWeight(layoutWithout) == 0 || getWidth() == Math.abs(ViewCompat.getTranslationX(mainLayout))) {
                    clickBySwipe();
                } else {
                    layoutWithout.clearAnimation();
                    if (collapseAnim != null) collapseAnim.cancel();

                    collapseAnim = new WeightAnimation(0, layoutWithout);
                    collapseAnim.setAnimationListener(collapseListener);
                    layoutWithout.startAnimation(collapseAnim);
                }
            } else {
                WeightAnimation weightAnimation = new WeightAnimation(views.length - 1, layoutWithout);
                layoutWithout.startAnimation(weightAnimation);
            }

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
        if (leftLinear != null && leftLinear.getWidth() > 0) {

            Utils.setViewWidth(leftLinearWithoutFirst, leftViews.length - 1);

            if (animated) {
                SwipeAnimation swipeAnim = new SwipeAnimation(leftLinear, 0, mainLayout, true);
                leftLinear.startAnimation(swipeAnim);
            } else {
                ViewCompat.setTranslationX(mainLayout, 0);
                ViewGroup.LayoutParams params = leftLinear.getLayoutParams();
                params.width = 0;
                leftLinear.setLayoutParams(params);
            }
        } else if (rightLinear != null && rightLinear.getWidth() > 0) {
            Utils.setViewWidth(rightLinearWithoutLast, rightViews.length - 1);

            if (animated) {
                SwipeAnimation swipeAnim = new SwipeAnimation(rightLinear, 0, mainLayout, false);
                rightLinear.startAnimation(swipeAnim);
            } else {
                ViewCompat.setTranslationX(mainLayout, 0);
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
                    SwipeAnimation swipeAnim = new SwipeAnimation(leftLinear, requiredWidthLeft, mainLayout, true);
                    leftLinear.startAnimation(swipeAnim);
                } else {
                    ViewCompat.setTranslationX(mainLayout, requiredWidthLeft);
                    ViewGroup.LayoutParams params = leftLinear.getLayoutParams();
                    params.width = requiredWidthLeft;
                    leftLinear.setLayoutParams(params);
                }
                break;
            case ITEM_STATE_RIGHT_EXPAND:
                int requiredWidthRight = rightIcons.length * itemWidth;
                if (animated) {
                    SwipeAnimation swipeAnim = new SwipeAnimation(rightLinear, requiredWidthRight, mainLayout, false);
                    rightLinear.startAnimation(swipeAnim);
                } else {
                    ViewCompat.setTranslationX(mainLayout, -requiredWidthRight);
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

    public boolean inAnimatedState() {
        if (leftLinear != null) {
            Animation anim = leftLinear.getAnimation();
            if (anim != null && !anim.hasEnded()) return true;
        }
        if (rightLinear != null) {
            Animation anim = rightLinear.getAnimation();
            if (anim != null && !anim.hasEnded()) return true;
        }
        return false;
    }

    public void setAutoHideSwipe(boolean autoHideSwipe) {
        this.autoHideSwipe = autoHideSwipe;
        ViewParent parent = getParent();
        if (parent != null && parent instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) parent;
            if (onScrollListener != null) recyclerView.removeOnScrollListener(onScrollListener);
            if (autoHideSwipe) recyclerView.addOnScrollListener(onScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING && ViewCompat.getTranslationX(mainLayout) != 0) {
                        setItemState(ITEM_STATE_COLLAPSED, true);
                    }
                }
            });
        } else {
            Log.e(TAG, "For autoHideSwipe parent must be a RecyclerView");
        }
    }

    public void setOnlyOneSwipe(boolean onlyOneSwipe) {
        this.onlyOneSwipe = onlyOneSwipe;
    }

    public boolean isLeftExpanding() {
        return ViewCompat.getTranslationX(mainLayout) > 0;
    }

    public boolean isRightExpanding() {
        return ViewCompat.getTranslationX(mainLayout) < 0;
    }

    public boolean isExpanding() {
        return isRightExpanding() || isLeftExpanding();
    }

    public boolean isRightExpanded() {
        return rightLinear != null && rightLinear.getWidth() >= rightLayoutMaxWidth;
    }

    public boolean isLeftExpanded() {
        return leftLinear != null && leftLinear.getWidth() >= leftLayoutMaxWidth;
    }

    public boolean isExpanded() {
        return isLeftExpanded() || isRightExpanded();
    }

    @Override
    public void onClick(View view) {
        if (onSwipeItemClickListener != null) {
            if (leftViews != null) {
                for (int i = 0; i < leftViews.length; i++) {
                    View v = leftViews[i];
                    if (v == view) {
                        if (leftViews.length == 1 || getViewWeight(leftLinearWithoutFirst) > 0)
                            onSwipeItemClickListener.onSwipeItemClick(true, i);
                        return;
                    }
                }
            }
            if (rightViews != null) {
                for (int i = 0; i < rightViews.length; i++) {
                    View v = rightViews[i];
                    if (v == view) {
                        if (rightViews.length == 1 || getViewWeight(rightLinearWithoutLast) > 0)
                            onSwipeItemClickListener.onSwipeItemClick(false, i);
                        break;
                    }
                }
            }
        }
    }

    public void collapseAll(boolean animated) {
        ViewParent parent = getParent();
        if (parent != null && parent instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) parent;
            int count = recyclerView.getChildCount();
            for (int i = 0; i < count; i++) {
                View item = recyclerView.getChildAt(i);
                if (item instanceof SwipeLayout) {
                    SwipeLayout swipeLayout = (SwipeLayout) item;
                    if (ViewCompat.getTranslationX(swipeLayout.getSwipeableView()) != 0) {
                        swipeLayout.setItemState(ITEM_STATE_COLLAPSED, animated);
                    }
                }
            }
        }
    }

    public interface OnSwipeItemClickListener {
        void onSwipeItemClick(boolean left, int index);
    }
}
