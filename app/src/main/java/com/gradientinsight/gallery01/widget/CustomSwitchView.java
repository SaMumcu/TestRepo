package com.gradientinsight.gallery01.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.interfaces.OnCheckedChangedListener;

import static com.gradientinsight.gallery01.util.Constants.COLOR_ANIMATION_DURATION;
import static com.gradientinsight.gallery01.util.Constants.KEY_CHECKED;
import static com.gradientinsight.gallery01.util.Constants.ON_CLICK_RADIUS_OFFSET;
import static com.gradientinsight.gallery01.util.Constants.STATE;
import static com.gradientinsight.gallery01.util.Constants.SWITCHER_ANIMATION_DURATION;
import static com.gradientinsight.gallery01.util.Constants.TRANSLATE_ANIMATION_DURATION;

public class CustomSwitchView extends View {

    private float iconRadius = 0f;
    private float iconClipRadius = 0f;
    private float iconCollapsedWidth = 0f;
    private float defHeight = 0;
    private float defWidth = 0;
    private boolean checked = true;
    private float switcherCornerRadius = 0f;
    private float iconHeight = 0f;
    private float iconTranslateX = 0f;
    // from rounded rect to circle and back
    private float iconProgress = 0f;
    private float onClickRadiusOffset = 0f;
    @ColorInt
    private int currentColor = 0;
    @ColorInt
    private int onColor = 0;
    @ColorInt
    private int offColor = 0;
    @ColorInt
    private int iconColor = 0;
    int toColor;

    private RectF switcherRect = new RectF(0f, 0f, 0f, 0f);
    private Paint switcherPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF iconRect = new RectF(0f, 0f, 0f, 0f);
    private RectF iconClipRect = new RectF(0f, 0f, 0f, 0f);

    private Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint iconClipPaint = new Paint(Paint.HINTING_OFF);

    private AnimatorSet animatorSet = new AnimatorSet();
    private OnCheckedChangedListener onCheckedChangedListener;

    public CustomSwitchView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void setOnClickRadiusOffset(float value) {
        onClickRadiusOffset = value;
        switcherRect.left = value;
        switcherRect.top = value;
        switcherRect.right = getWidth() - value;
        switcherRect.bottom = getHeight() - value;
        invalidate();
    }

    public void setCurrentColor(@ColorInt int currentColor) {
        this.currentColor = currentColor;
        switcherPaint.setColor(currentColor);
        iconClipPaint.setColor(currentColor);
    }

    public void setIconProgress(float iconProgress) {
        this.iconProgress = iconProgress;
        float iconOffset = lerp(0f, iconRadius - iconCollapsedWidth / 2, iconProgress);
        iconRect.left = getWidth() - switcherCornerRadius - iconCollapsedWidth / 2 - iconOffset;
        iconRect.right = getWidth() - switcherCornerRadius + iconCollapsedWidth / 2 + iconOffset;

        float clipOffset = lerp(0f, iconClipRadius, iconProgress);

        iconRectSet(iconClipRect,
                iconRect.centerX() - clipOffset,
                iconRect.centerY() - clipOffset,
                iconRect.centerX() + clipOffset,
                iconRect.centerY() + clipOffset);

        postInvalidateOnAnimation();
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Switcher,
                defStyleAttr, R.style.Switcher);

        onColor = typedArray.getColor(R.styleable.Switcher_switcher_on_color, 0);
        offColor = typedArray.getColor(R.styleable.Switcher_switcher_off_color, 0);
        iconColor = typedArray.getColor(R.styleable.Switcher_switcher_icon_color, 0);

        checked = typedArray.getBoolean(R.styleable.Switcher_android_checked, true);

        if (!checked) {
            setIconProgress(1f);
        }

        if (checked) {
            setCurrentColor(onColor);
        } else {
            setCurrentColor(offColor);
        }

        iconPaint.setColor(iconColor);

        defHeight = typedArray.getDimensionPixelOffset(R.styleable.Switcher_switcher_height, 0);
        defWidth = typedArray.getDimensionPixelOffset(R.styleable.Switcher_switcher_width, 0);

        typedArray.recycle();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomSwitchView.this.animateSwitch();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        switcherRect.right = getWidth();
        switcherRect.bottom = getHeight();

        switcherCornerRadius = getHeight() / 2f;

        iconRadius = switcherCornerRadius * 0.6f;
        iconClipRadius = iconRadius / 1.20f;
        iconCollapsedWidth = iconRadius - iconClipRadius;

        iconHeight = iconRadius * 2f;

        iconRectSet(iconRect,
                getWidth() - switcherCornerRadius - iconCollapsedWidth / 2,
                (getHeight() - iconHeight) / 2f,
                getWidth() - switcherCornerRadius + iconCollapsedWidth / 2,
                getHeight() - (getHeight() - iconHeight) / 2f);

        if (!checked) {
            iconRect.left = getWidth() - switcherCornerRadius - iconCollapsedWidth / 2 - (iconRadius - iconCollapsedWidth / 2);
            iconRect.right = getWidth() - switcherCornerRadius + iconCollapsedWidth / 2 + (iconRadius - iconCollapsedWidth / 2);

            iconRectSet(iconClipRect,
                    iconRect.centerX() - iconClipRadius,
                    iconRect.centerY() - iconClipRadius,
                    iconRect.centerX() + iconClipRadius,
                    iconRect.centerY() + iconClipRadius);

            iconTranslateX = -(getWidth() - switcherCornerRadius * 2);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            width = (int) defWidth;
            height = (int) defHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        // switcher
        canvas.drawRoundRect(switcherRect, switcherCornerRadius, switcherCornerRadius, switcherPaint);

        // icon
        int count = canvas.getSaveCount();
        try {
            canvas.translate(iconTranslateX, 0);
            canvas.drawRoundRect(iconRect, switcherCornerRadius, switcherCornerRadius, iconPaint);
            // don't draw clip path if icon is collapsed state (to prevent drawing small circle
            // on rounded rect when switch is checked)
            if (iconClipRect.width() > iconCollapsedWidth)
                canvas.drawRoundRect(iconClipRect, 120, 120, iconClipPaint);
        } finally {
            canvas.restoreToCount(count);
        }
    }

    private void animateSwitch() {
        animatorSet.cancel();
        animatorSet = new AnimatorSet();

        setOnClickRadiusOffset(ON_CLICK_RADIUS_OFFSET);

        float iconTranslateA = 0f;
        float iconTranslateB = -(getWidth() - switcherCornerRadius * 2);
        float newProgress = 1f;

        if (!checked) {
            iconTranslateA = iconTranslateB;
            iconTranslateB = 0f;
            newProgress = 0f;
        }

        ValueAnimator switcherAnimator = ValueAnimator.ofFloat(iconProgress, newProgress);
        switcherAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CustomSwitchView.this.setIconProgress((float) animation.getAnimatedValue());
            }
        });

        switcherAnimator.setDuration(SWITCHER_ANIMATION_DURATION);

        ValueAnimator translateAnimator = ValueAnimator.ofFloat(0f, 1f);
        final float finalIconTranslateA = iconTranslateA;
        final float finalIconTranslateB = iconTranslateB;
        translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                iconTranslateX = CustomSwitchView.this.lerp(finalIconTranslateA, finalIconTranslateB, value);
            }
        });
        translateAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setOnClickRadiusOffset(0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        translateAnimator.setDuration(TRANSLATE_ANIMATION_DURATION);

        if (!checked) {
            toColor = onColor;
        } else {
            toColor = offColor;
        }

        iconClipPaint.setColor(toColor);

        ValueAnimator colorAnimator = new ValueAnimator();
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CustomSwitchView.this.setCurrentColor((int) animation.getAnimatedValue());
            }
        });
        colorAnimator.setIntValues(currentColor, toColor);
        colorAnimator.setEvaluator(new ArgbEvaluator());
        colorAnimator.setDuration(COLOR_ANIMATION_DURATION);


        animatorSet.addListener(new AnimatorSet.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                checked = !checked;
                if (onCheckedChangedListener != null) {
                    onCheckedChangedListener.onCheckedChanged(checked);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.playTogether(switcherAnimator, translateAnimator, colorAnimator);
        animatorSet.start();
    }

    private void forceCheck() {
        currentColor = offColor;
        iconProgress = 1f;
    }

    public void setOnCheckedChangedListener(OnCheckedChangedListener onCheckedChangedListener) {
        this.onCheckedChangedListener = onCheckedChangedListener;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_CHECKED, checked);
        bundle.putParcelable(STATE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            super.onRestoreInstanceState(((Bundle) state).getParcelable(STATE));
            checked = ((Bundle) state).getBoolean(KEY_CHECKED);
            if (!checked) forceCheck();
        }
    }

    private void iconRectSet(RectF icon, float left, float top, float right, float bottom) {
        icon.set(left, top, right, bottom);
    }

}
